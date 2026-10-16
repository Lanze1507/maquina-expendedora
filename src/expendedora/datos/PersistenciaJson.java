package expendedora.datos;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/** Esquema versión 1. La importación entrega una instantánea; nunca muta la máquina. */
public final class PersistenciaJson {
    public static final int MAX_BYTES = 1024 * 1024;
    private PersistenciaJson() {}
    public static ConfiguracionMaquina importar(Path archivo) throws IOException {
        Objects.requireNonNull(archivo, "archivo");
        byte[] datos;
        try (InputStream in = Files.newInputStream(archivo); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] bloque = new byte[8192]; int n;
            while ((n = in.read(bloque)) != -1) {
                if (out.size() + n > MAX_BYTES) throw new DatosException("JSON_DEMASIADO_GRANDE", "El archivo excede 1 MiB.");
                out.write(bloque, 0, n);
            }
            datos = out.toByteArray();
        }
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        return desdeJson(decoder.decode(ByteBuffer.wrap(datos)).toString());
    }
    public static ConfiguracionMaquina desdeJson(String texto) {
        if (texto != null && (texto.length() > MAX_BYTES || texto.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES))
            throw new DatosException("JSON_DEMASIADO_GRANDE", "El texto excede 1 MiB en UTF-8.");
        Map<?,?> raiz = objeto(Json.leer(texto), "raíz");
        campos(raiz, "version", "productos", "saldo", "seleccion", "estado");
        if (entero(raiz.get("version"), "version") != 1)
            throw new DatosException("VERSION_INVALIDA", "Solo se admite la versión 1.");
        Object lista = raiz.get("productos");
        if (!(lista instanceof List<?>)) throw invalido("productos debe ser una lista.");
        List<Producto> productos = new ArrayList<>();
        for (Object valor : (List<?>)lista) {
            Map<?,?> p = objeto(valor, "producto");
            campos(p, "codigo", "nombre", "precio", "cantidad");
            productos.add(new Producto(cadena(p.get("codigo"), "codigo"), cadena(p.get("nombre"), "nombre"),
                numero(p.get("precio"), "precio"), entero(p.get("cantidad"), "cantidad")));
        }
        Object sel = raiz.get("seleccion");
        String seleccion = sel == null ? null : cadena(sel, "seleccion");
        EstadoPersistido estado;
        try { estado = EstadoPersistido.valueOf(cadena(raiz.get("estado"), "estado")); }
        catch (IllegalArgumentException ex) { throw new DatosException("ESTADO_INVALIDO", "El estado debe ser E0, E1, E2, E3, E4 o E5."); }
        return new ConfiguracionMaquina(productos, numero(raiz.get("saldo"), "saldo"), seleccion, estado);
    }
    public static String aJson(ConfiguracionMaquina config) {
        Objects.requireNonNull(config, "config");
        StringBuilder b = new StringBuilder("{\n  \"version\": 1,\n  \"productos\": [");
        boolean primero = true;
        for (Producto p : config.getProductos()) {
            if (!primero) b.append(','); primero = false;
            b.append("\n    {\"codigo\": ").append(Json.citar(p.getCodigo()))
                .append(", \"nombre\": ").append(Json.citar(p.getNombre()))
                .append(", \"precio\": ").append(p.getPrecio().toString())
                .append(", \"cantidad\": ").append(p.getCantidad()).append('}');
        }
        return b.append("\n  ],\n  \"saldo\": ").append(config.getSaldo().toString())
            .append(",\n  \"seleccion\": ").append(config.getSeleccion() == null ? "null" : Json.citar(config.getSeleccion()))
            .append(",\n  \"estado\": ").append(Json.citar(config.getEstado().name())).append("\n}\n").toString();
    }
    /** Escribe primero a un temporal; no trunca el archivo destino ante un error de serialización. */
    public static void exportar(Path archivo, ConfiguracionMaquina config) throws IOException {
        Objects.requireNonNull(archivo, "archivo");
        byte[] datos = aJson(config).getBytes(StandardCharsets.UTF_8);
        if (datos.length > MAX_BYTES) throw new DatosException("JSON_DEMASIADO_GRANDE", "El resultado excede 1 MiB.");
        Path destino = archivo.toAbsolutePath();
        Path temporal = Files.createTempFile(destino.getParent(), "expendedora-", ".tmp");
        try {
            Files.write(temporal, datos);
            try { Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporal); }
    }
    private static DatosException invalido(String mensaje) { return new DatosException("ESQUEMA_INVALIDO", mensaje); }
    private static Map<?,?> objeto(Object valor, String campo) {
        if (!(valor instanceof Map<?,?>)) throw invalido(campo + " debe ser un objeto.");
        return (Map<?,?>)valor;
    }
    private static void campos(Map<?,?> objeto, String... nombres) {
        Set<String> esperados = new HashSet<>(Arrays.asList(nombres));
        if (!objeto.keySet().equals(esperados)) throw invalido("Campos obligatorios exactos: " + esperados);
    }
    private static String cadena(Object valor, String campo) {
        if (!(valor instanceof String)) throw invalido(campo + " debe ser texto.");
        return (String)valor;
    }
    private static BigDecimal numero(Object valor, String campo) {
        if (!(valor instanceof BigDecimal)) throw invalido(campo + " debe ser un número JSON.");
        return (BigDecimal)valor;
    }
    private static int entero(Object valor, String campo) {
        try { return numero(valor, campo).intValueExact(); }
        catch (ArithmeticException ex) { throw invalido(campo + " debe ser un entero de 32 bits."); }
    }
}
