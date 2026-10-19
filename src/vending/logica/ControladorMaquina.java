package vending.logica;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import expendedora.datos.Inventario;
import expendedora.datos.Producto;
import vending.analizador.AnalizadorCompleto;
import vending.analizador.AnalizadorLexico;
import vending.analizador.AnalizadorSintactico;
import vending.analizador.Token;
import vending.automata.Estado;
import vending.gui.ProductView;
import vending.gui.VendingController;
import vending.gui.VendingViewModel;

/**
 * Puente entre la interfaz Swing (vending.gui) y el resto del proyecto:
 *
 *   botones de la GUI  -> MaquinaExpendedora (Parte 2: lógica de operaciones)
 *   Analizar           -> AnalizadorCompleto (léxico, sintáctico y semántico)
 *   Ejecutar siguiente -> PRODUCTO / CARGAR en Inventario (Parte 1)
 *                         MONEDA / SELECCIONAR / COMPRAR / CANCELAR / MOSTRAR en MaquinaExpendedora
 *
 * Implementa el contrato VendingController definido por la GUI.
 */
public class ControladorMaquina implements VendingController {

    /** Una instrucción ya validada por el analizador, lista para ejecutarse. */
    private static final class Instruccion {
        final String nombre;
        final List<String> parametros;
        final int linea;

        Instruccion(String nombre, List<String> parametros, int linea) {
            this.nombre = nombre;
            this.parametros = parametros;
            this.linea = linea;
        }

        @Override
        public String toString() {
            return nombre + "(" + String.join(",", parametros) + ");";
        }
    }

    private Inventario inventario;
    private MaquinaExpendedora maquina;
    private final Deque<Instruccion> pendientes = new ArrayDeque<>();
    private VendingViewModel vista;

    public ControladorMaquina(Inventario inventario) {
        this.inventario = inventario;
        this.maquina = new MaquinaExpendedora(inventario);
        this.vista = construirVista("[OK] Sistema listo.", null);
    }

    public MaquinaExpendedora getMaquina() {
        return maquina;
    }

    // ------------------------------------------------------------ botones de la GUI

    @Override
    public void insertMoney(int amount) {
        actualizarVista(maquina.insertarMoneda(amount, 0));
    }

    @Override
    public void selectProduct(String code) {
        actualizarVista(maquina.seleccionar(code, 0));
    }

    @Override
    public void buy() {
        actualizarVista(maquina.comprar(0));
    }

    @Override
    public void cancel() {
        actualizarVista(maquina.cancelar(0));
    }

    // ------------------------------------------------------------ analizador + ejecución

    /**
     * Ejecuta el análisis completo del compañero (tabla de tokens, léxico, sintáctico y semántico).
     * Si no hay errores léxicos ni sintácticos, prepara las instrucciones para "Ejecutar siguiente"
     * sobre una máquina nueva (el programa .vm define sus propios productos).
     */
    @Override
    public void analyzeSource(String source) {
        String texto = source == null ? "" : source;
        String reporte = new AnalizadorCompleto().analizar(texto);
        pendientes.clear();

        AnalizadorLexico lexico = new AnalizadorLexico();
        List<Token> tokens = lexico.analizar(texto);
        AnalizadorSintactico sintactico = new AnalizadorSintactico();
        if (lexico.getErrores().isEmpty()) {
            sintactico.analizar(tokens);
        }

        if (lexico.getErrores().isEmpty() && sintactico.getErrores().isEmpty()) {
            pendientes.addAll(agrupar(tokens));
            inventario = new Inventario();
            maquina = new MaquinaExpendedora(inventario);
            reporte += "\n=== EJECUCION ===\n"
                    + "[OK] " + pendientes.size() + " instrucciones listas. "
                    + "Presione \"Ejecutar siguiente\" para ejecutarlas una por una.\n";
        } else {
            reporte += "\n=== EJECUCION ===\n"
                    + "[INFO] Corrija los errores léxicos/sintácticos para poder ejecutar.\n";
        }
        vista = construirVista(reporte, null);
    }

    @Override
    public void executeNextInstruction() {
        Instruccion ins = pendientes.poll();
        if (ins == null) {
            vista = construirVista("[INFO] No hay instrucciones pendientes. Presione \"Analizar\" primero.", null);
            return;
        }

        String encabezado = "Linea " + ins.linea + ": " + ins + "\n";
        switch (ins.nombre) {
            case "PRODUCTO":
                vista = construirVista(encabezado + registrarProducto(ins), null);
                break;
            case "CARGAR":
                vista = construirVista(encabezado + cargarProducto(ins), null);
                break;
            default:
                Resultado r = maquina.ejecutar(ins.nombre, ins.parametros, ins.linea);
                if (r == null) {
                    vista = construirVista(encabezado + "[ERROR] Instrucción no reconocida.", null);
                } else {
                    actualizarVista(r, encabezado);
                }
        }
        if (pendientes.isEmpty()) {
            vista = construirVista(vista.message() + "\n[OK] Fin del programa.", null);
        }
    }

    @Override
    public VendingViewModel getViewModel() {
        return vista;
    }

    // ------------------------------------------------------------ PRODUCTO / CARGAR (Parte 1)

    private String registrarProducto(Instruccion ins) {
        try {
            List<String> p = ins.parametros;
            Producto prod = inventario.registrar(p.get(0), p.get(1), new BigDecimal(p.get(2)), Integer.parseInt(p.get(3)));
            return "[OK] Producto " + prod.getCodigo() + " registrado: " + prod.getNombre();
        } catch (RuntimeException ex) {
            return "[ERROR SEMANTICO] Linea " + ins.linea + ": " + ex.getMessage();
        }
    }

    private String cargarProducto(Instruccion ins) {
        try {
            List<String> p = ins.parametros;
            int antes = inventario.buscar(p.get(0)).getCantidad();
            int unidades = Integer.parseInt(p.get(1));
            Producto prod = inventario.cargar(p.get(0), unidades);
            return "[OK] Recarga " + prod.getCodigo() + ": " + antes + " + " + unidades
                    + " = " + prod.getCantidad() + " unidades";
        } catch (RuntimeException ex) {
            return "[ERROR SEMANTICO] Linea " + ins.linea + ": " + ex.getMessage();
        }
    }

    /** Convierte la lista de tokens en instrucciones: INSTRUCCION ( p1 , p2 ... ) ; */
    private static List<Instruccion> agrupar(List<Token> tokens) {
        List<Instruccion> lista = new ArrayList<>();
        String nombre = null;
        int linea = 0;
        List<String> params = new ArrayList<>();
        for (Token t : tokens) {
            switch (t.getTipo()) {
                case "INSTRUCCION":
                    nombre = t.getValor();
                    linea = t.getLinea();
                    params = new ArrayList<>();
                    break;
                case "IDENTIFICADOR":
                case "NUMERO":
                    params.add(t.getValor());
                    break;
                case "PUNTO_COMA":
                    if (nombre != null) lista.add(new Instruccion(nombre, params, linea));
                    nombre = null;
                    break;
                default:
                    break;
            }
        }
        return lista;
    }

    // ------------------------------------------------------------ conversión a la vista

    /** Refresca la vista con el resultado de una operación. */
    public void actualizarVista(Resultado r) {
        actualizarVista(r, "");
    }

    private void actualizarVista(Resultado r, String prefijo) {
        String texto = prefijo + r.getTexto().trim();
        if (r.getTablaInventario() != null) {
            texto += "\n" + r.getTablaInventario();
        }
        if (r.getRecorrido().size() > 1) {
            texto += "\n      (estados: " + r.getRecorridoTexto() + ")";
        }
        vista = construirVista(texto, r);
    }

    private VendingViewModel construirVista(String mensaje, Resultado r) {
        List<ProductView> productos = new ArrayList<>();
        for (Producto p : inventario.listar()) {
            productos.add(new ProductView(p.getCodigo(), p.getNombre(),
                    p.getPrecio().doubleValue(), p.getCantidad()));
        }

        Estado estado = maquina.getEstadoActual();
        Producto sel = maquina.getProductoSeleccionado();

        String entregado = "-";
        double cambio = 0;
        if (r != null && r.getProductoEntregado() != null) {
            entregado = r.getProductoEntregado().getNombre();
            cambio = r.getCambio().doubleValue();
        } else if (r != null && r.getDineroDevuelto() > 0) {
            cambio = r.getDineroDevuelto();
        }

        return new VendingViewModel(
                productos,
                maquina.getSaldo(),
                estado.name(),
                estado.getDescripcion(),
                sel == null ? "-" : sel.getNombre(),
                entregado,
                cambio,
                mensaje
        );
    }

    // ------------------------------------------------------------ inventario de prueba

    /** Inventario inicial para usar los botones sin cargar un archivo .vm. */
    public static Inventario inventarioDePrueba() {
        Inventario inv = new Inventario();
        inv.registrar("A1", "AGUA", new BigDecimal("5"), 10);
        inv.registrar("A2", "GASEOSA", new BigDecimal("8"), 5);
        inv.registrar("A3", "GALLETA", new BigDecimal("3"), 15);
        return inv;
    }
}
