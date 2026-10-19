package vending.logica;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vending.automata.AutomataMaquina;
import vending.automata.ControlEstados;
import vending.automata.Entrada;
import vending.automata.Estado;
import expendedora.datos.DatosException;
import expendedora.datos.Inventario;
import expendedora.datos.Producto;

/**
 * LÓGICA DE OPERACIONES DE LA MÁQUINA EXPENDEDORA
 *
 * Procesa las instrucciones de operación:
 *   MONEDA(valor);  SELECCIONAR(codigo);  COMPRAR();  CANCELAR();  MOSTRAR();
 *
 * Reglas que aplica (validaciones semánticas de operación):
 *   - Solo acepta monedas de Q1, Q5 y Q10 y acumula el saldo.
 *   - SELECCIONAR: el producto debe existir y tener existencias.
 *   - COMPRAR: debe haber selección, existencias y saldo suficiente;
 *              descuenta una unidad, calcula el cambio y finaliza la operación.
 *   - CANCELAR: devuelve el saldo y restablece la selección.
 *   - Impide comprar sin selección, sin dinero suficiente y entregar dos veces.
 *
 * Cada operación pide al autómata (ControlEstados) la transición correspondiente,
 * de modo que el estado actual siempre refleja el proceso de compra.
 *
 * Uso típico:
 *   MaquinaExpendedora m = new MaquinaExpendedora(inventario);
 *   Resultado r = m.insertarMoneda(5, numeroLinea);
 *   System.out.print(r.getTexto());
 */
public class MaquinaExpendedora {

    /** Denominaciones aceptadas, en quetzales. */
    public static final int[] MONEDAS_VALIDAS = {1, 5, 10};

    private final Inventario inventario;
    private final ControlEstados automata;
    private final List<MaquinaListener> listeners = new ArrayList<>();
    private final List<Mensaje> historial = new ArrayList<>();

    private int saldo = 0;
    private String seleccionado = null;            // código del producto seleccionado
    private boolean compraRecienConfirmada = false;   // evita la doble entrega
    private boolean huboVentas = false;               // para "Inventario actualizado mostrado."

    private Producto ultimoEntregado = null;
    private BigDecimal ultimoCambio = BigDecimal.ZERO;
    private int ultimoDevuelto = 0;

    // ------------------------------------------------------------------ constructores

    public MaquinaExpendedora(Inventario inventario) {
        this(inventario, new AutomataMaquina());
    }

    public MaquinaExpendedora(Inventario inventario, ControlEstados automata) {
        if (inventario == null || automata == null) {
            throw new IllegalArgumentException("Inventario y autómata son obligatorios");
        }
        this.inventario = inventario;
        this.automata = automata;
    }

    // ------------------------------------------------------------------ MONEDA

    public Resultado insertarMoneda(int valor, int linea) {
        Resultado r = nuevo("MONEDA");

        if (!esMonedaValida(valor)) {
            r.error(linea, "Moneda no permitida: Q" + valor + ".\n"
                         + "Solo se aceptan monedas de Q1, Q5 y Q10.");
            return terminar(r);
        }
        if (!automata.puedeTransitar(Entrada.MONEDA)) {
            r.error(linea, errorEstado("ingresar dinero"));
            return terminar(r);
        }

        saldo += valor;
        transitar(r, Entrada.MONEDA);
        compraRecienConfirmada = false;

        r.ok("Moneda recibida: Q" + valor);
        r.ok("Saldo disponible: Q" + saldo);
        return terminar(r);
    }

    // ------------------------------------------------------------------ SELECCIONAR

    public Resultado seleccionar(String codigo, int linea) {
        Resultado r = nuevo("SELECCIONAR");
        String cod = codigo == null ? "" : codigo.trim().toUpperCase();

        Producto p = buscarSeguro(cod);
        if (p == null) {
            r.error(linea, "El producto " + cod + " no existe.");
            return terminar(r);
        }
        if (!p.tieneExistencias()) {
            r.error(linea, "El producto " + cod + " no tiene existencias.\n"
                         + "Existencias disponibles: " + p.getCantidad());
            return terminar(r);
        }
        if (!automata.puedeTransitar(Entrada.SELECCIONAR)) {
            if (automata.getEstado() == Estado.E0) {
                r.error(linea, "Debe ingresar dinero antes de seleccionar un producto.\n"
                             + "Estado actual: " + Estado.E0);
            } else {
                r.error(linea, errorEstado("seleccionar un producto"));
            }
            return terminar(r);
        }

        seleccionado = p.getCodigo();
        transitar(r, Entrada.SELECCIONAR);
        compraRecienConfirmada = false;

        r.ok("Producto seleccionado: " + p.getCodigo());
        r.ok("Producto disponible: " + p.getNombre());
        r.ok("Precio: Q" + p.getPrecio().toPlainString());
        return terminar(r);
    }

    // ------------------------------------------------------------------ COMPRAR

    public Resultado comprar(int linea) {
        Resultado r = nuevo("COMPRAR");

        // 1. Debe existir una selección (y no repetir una compra ya entregada)
        if (seleccionado == null) {
            if (compraRecienConfirmada) {
                r.error(linea, "La compra ya fue confirmada y el producto fue entregado.\n"
                             + "No se puede entregar el producto dos veces.");
            } else {
                r.error(linea, "No existe un producto seleccionado.");
            }
            return terminar(r);
        }

        Producto p = buscarSeguro(seleccionado);

        // 2. El producto debe seguir teniendo existencias
        if (p == null || !p.tieneExistencias()) {
            r.error(linea, "El producto " + seleccionado + " no tiene existencias.\n"
                         + "Existencias disponibles: 0");
            return terminar(r);
        }

        // 3. Saldo suficiente
        BigDecimal precio = p.getPrecio();
        BigDecimal disponible = BigDecimal.valueOf(saldo);
        if (disponible.compareTo(precio) < 0) {
            r.error(linea, "Dinero insuficiente para realizar la compra.\n"
                         + "Saldo disponible: Q" + saldo + "\n"
                         + "Precio del producto: Q" + precio.toPlainString() + "\n"
                         + "Dinero faltante: Q" + precio.subtract(disponible).toPlainString());
            return terminar(r);
        }

        // 4. El autómata debe permitir la compra
        if (!automata.puedeTransitar(Entrada.COMPRAR)) {
            r.error(linea, errorEstado("confirmar la compra"));
            return terminar(r);
        }

        // ---- compra válida: E2 -> E3 -> E4 -> (E5) -> E0
        transitar(r, Entrada.COMPRAR);                // E3 procesando
        transitar(r, Entrada.AUTORIZAR);              // E4 entregando
        r.ok("Compra autorizada.");

        Producto entregado = inventario.descontarUnidad(p.getCodigo());   // Parte 1: solo tras autorizar
        r.ok("Producto despachado: " + entregado.getNombre());
        r.setProductoEntregado(entregado);

        BigDecimal cambio = disponible.subtract(precio);
        r.setCambio(cambio);
        if (cambio.signum() > 0) {
            transitar(r, Entrada.CON_CAMBIO);         // E5 devolviendo
            r.ok("Cambio devuelto: Q" + cambio.toPlainString());
            transitar(r, Entrada.DEVUELTO);           // E0
        } else {
            r.ok("Sin cambio.");
            transitar(r, Entrada.SIN_CAMBIO);         // E0
        }
        r.ok("Existencias restantes " + entregado.getCodigo() + ": " + entregado.getCantidad());

        ultimoEntregado = entregado;
        ultimoCambio = cambio;
        ultimoDevuelto = 0;

        // Finalizar operación
        saldo = 0;
        seleccionado = null;
        compraRecienConfirmada = true;
        huboVentas = true;

        r.ok("Operación finalizada.");
        r.ok("Estado actual: " + automata.getEstado().name());
        r.ok("Saldo actual: Q" + saldo);
        return terminar(r);
    }

    // ------------------------------------------------------------------ CANCELAR

    public Resultado cancelar(int linea) {
        Resultado r = nuevo("CANCELAR");

        if (!automata.puedeTransitar(Entrada.CANCELAR)) {
            r.error(linea, errorEstado("cancelar"));
            return terminar(r);
        }

        int devuelto = saldo;
        transitar(r, Entrada.CANCELAR);               // E5 (o E0 si no había operación)

        if (devuelto > 0) {
            r.ok("Operacion cancelada.");
            r.ok("Dinero devuelto: Q" + devuelto);
        } else {
            r.ok("Operacion cancelada. No hay dinero que devolver.");
        }
        if (automata.getEstado() == Estado.E5) {
            transitar(r, Entrada.DEVUELTO);           // E0
        }

        r.setDineroDevuelto(devuelto);
        ultimoDevuelto = devuelto;
        ultimoEntregado = null;
        ultimoCambio = BigDecimal.ZERO;

        saldo = 0;
        seleccionado = null;
        compraRecienConfirmada = false;

        r.ok("Estado actual: " + automata.getEstado().name());
        r.ok("Saldo actual: Q" + saldo);
        return terminar(r);
    }

    // ------------------------------------------------------------------ MOSTRAR

    public Resultado mostrar(int linea) {
        Resultado r = nuevo("MOSTRAR");
        r.setTablaInventario(getTablaInventario());
        r.ok(huboVentas ? "Inventario actualizado mostrado." : "Inventario mostrado.");
        return terminar(r);
    }

    // ------------------------------------------------------------------ despachador

    /**
     * Punto de entrada único para el analizador / ejecutor secuencial.
     * Recibe el nombre de la instrucción ya validada sintácticamente, sus
     * parámetros y el número de línea.
     *
     * @return el Resultado, o null si la instrucción no es de operación
     *         (PRODUCTO y CARGAR los ejecuta la parte de inventario).
     */
    public Resultado ejecutar(String instruccion, List<String> parametros, int linea) {
        if (instruccion == null) return null;
        List<String> p = parametros == null ? Collections.<String>emptyList() : parametros;

        switch (instruccion.trim().toUpperCase()) {
            case "MONEDA":
                int valor;
                try {
                    valor = Integer.parseInt(p.get(0).trim());
                } catch (RuntimeException ex) {
                    Resultado r = nuevo("MONEDA");
                    r.error(linea, "El valor de la moneda debe ser un número entero.");
                    return terminar(r);
                }
                return insertarMoneda(valor, linea);
            case "SELECCIONAR":
                return seleccionar(p.isEmpty() ? "" : p.get(0), linea);
            case "COMPRAR":
                return comprar(linea);
            case "CANCELAR":
                return cancelar(linea);
            case "MOSTRAR":
                return mostrar(linea);
            default:
                return null;
        }
    }

    // ------------------------------------------------------------------ utilidades

    public static boolean esMonedaValida(int valor) {
        for (int m : MONEDAS_VALIDAS) {
            if (m == valor) return true;
        }
        return false;
    }

    /** Tabla de texto con el inventario (para consola o un JTextArea). */
    public String getTablaInventario() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-15s %8s %11s%n", "CODIGO", "NOMBRE", "PRECIO", "EXISTENCIAS"));
        sb.append("---------------------------------------------\n");
        for (Producto p : inventario.listar()) {
            sb.append(String.format("%-8s %-15s %8s %11d%n",
                    p.getCodigo(), p.getNombre(), "Q" + p.getPrecio().toPlainString(), p.getCantidad()));
        }
        return sb.toString();
    }

    /** Reinicia saldo, selección y autómata (por ejemplo, antes de ejecutar un nuevo archivo .vm). */
    public void reiniciar() {
        saldo = 0;
        seleccionado = null;
        compraRecienConfirmada = false;
        huboVentas = false;
        ultimoEntregado = null;
        ultimoCambio = BigDecimal.ZERO;
        ultimoDevuelto = 0;
        historial.clear();
        automata.reiniciar();
        notificar(null);
    }

    /**
     * Restaura una operación en curso (para la importación JSON del estado).
     * Lleva al autómata por las transiciones válidas: E0 -> E1 (saldo) -> E2 (selección).
     * @return false si los datos no son coherentes (no se modifica nada).
     */
    public boolean restaurarOperacion(int saldoGuardado, String codigoSeleccionado) {
        if (saldoGuardado < 0) return false;
        Producto p = null;
        if (codigoSeleccionado != null && !codigoSeleccionado.trim().isEmpty()) {
            p = buscarSeguro(codigoSeleccionado.trim().toUpperCase());
            if (p == null || saldoGuardado == 0) return false;
        }
        automata.reiniciar();
        saldo = saldoGuardado;
        seleccionado = p == null ? null : p.getCodigo();
        compraRecienConfirmada = false;
        if (saldo > 0) automata.transitar(Entrada.MONEDA);
        if (p != null) automata.transitar(Entrada.SELECCIONAR);
        notificar(null);
        return true;
    }

    // ------------------------------------------------------------------ getters para GUI / JSON

    public int getSaldo()                     { return saldo; }
    public Producto getProductoSeleccionado() { return buscarSeguro(seleccionado); }
    public String getCodigoSeleccionado()     { return seleccionado; }
    public Estado getEstadoActual()           { return automata.getEstado(); }
    public Producto getUltimoEntregado()      { return ultimoEntregado; }
    public BigDecimal getUltimoCambio()       { return ultimoCambio; }
    public int getUltimoDevuelto()            { return ultimoDevuelto; }
    public Inventario getInventario()         { return inventario; }
    public ControlEstados getAutomata()       { return automata; }
    public List<Mensaje> getHistorial()       { return Collections.unmodifiableList(historial); }

    public void addListener(MaquinaListener l)    { if (l != null) listeners.add(l); }
    public void removeListener(MaquinaListener l) { listeners.remove(l); }

    // ------------------------------------------------------------------ privados

    /** Busca en el inventario de la Parte 1; devuelve null si no existe (en vez de lanzar excepción). */
    private Producto buscarSeguro(String codigo) {
        if (codigo == null || codigo.isEmpty()) return null;
        try {
            return inventario.buscar(codigo);
        } catch (DatosException ex) {
            return null;
        }
    }

    private Resultado nuevo(String operacion) {
        return new Resultado(operacion, automata.getEstado());
    }

    private void transitar(Resultado r, Entrada e) {
        r.visitar(automata.transitar(e));
    }

    private Resultado terminar(Resultado r) {
        historial.addAll(r.getMensajes());
        notificar(r);
        return r;
    }

    private String errorEstado(String accion) {
        Estado e = automata.getEstado();
        return "No se puede " + accion + " en el estado actual.\nEstado actual: " + e;
    }

    private void notificar(Resultado r) {
        for (MaquinaListener l : listeners) {
            l.maquinaActualizada(this, r);
        }
    }
}
