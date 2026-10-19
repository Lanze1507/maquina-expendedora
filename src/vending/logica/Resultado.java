package vending.logica;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vending.automata.Estado;
import expendedora.datos.Producto;

/**
 * Resultado de ejecutar UNA operación (MONEDA, SELECCIONAR, COMPRAR, CANCELAR o MOSTRAR).
 * La GUI y el analizador lo usan para mostrar mensajes, el recorrido de estados,
 * el producto entregado y el cambio.
 */
public class Resultado {

    private final String operacion;
    private final List<Mensaje> mensajes = new ArrayList<>();
    private final List<Estado> recorrido = new ArrayList<>();   // estados visitados, en orden
    private Producto productoEntregado;   // != null solo en una compra exitosa
    private BigDecimal cambio = BigDecimal.ZERO;   // cambio entregado en la compra
    private int dineroDevuelto;           // dinero devuelto al cancelar
    private String tablaInventario;       // != null solo en MOSTRAR

    public Resultado(String operacion, Estado estadoInicial) {
        this.operacion = operacion;
        this.recorrido.add(estadoInicial);
    }

    // ---- uso interno de MaquinaExpendedora ----
    void ok(String texto)                 { mensajes.add(Mensaje.ok(texto)); }
    void error(int linea, String texto)   { mensajes.add(Mensaje.error(linea, texto)); }
    void visitar(Estado e)                { recorrido.add(e); }
    void setProductoEntregado(Producto p) { this.productoEntregado = p; }
    void setCambio(BigDecimal c)          { this.cambio = c; }
    void setDineroDevuelto(int d)         { this.dineroDevuelto = d; }
    void setTablaInventario(String t)     { this.tablaInventario = t; }

    // ---- consultas ----
    public String getOperacion()          { return operacion; }
    public List<Mensaje> getMensajes()    { return Collections.unmodifiableList(mensajes); }
    public List<Estado> getRecorrido()    { return Collections.unmodifiableList(recorrido); }
    public Estado getEstadoFinal()        { return recorrido.get(recorrido.size() - 1); }
    public Producto getProductoEntregado(){ return productoEntregado; }
    public BigDecimal getCambio()         { return cambio; }
    public int getDineroDevuelto()        { return dineroDevuelto; }
    public String getTablaInventario()    { return tablaInventario; }

    public boolean isExitoso() {
        for (Mensaje m : mensajes) {
            if (m.esError()) return false;
        }
        return true;
    }

    /** Recorrido como texto, p. ej. "E2 -> E3 -> E4 -> E5 -> E0". */
    public String getRecorridoTexto() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recorrido.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(recorrido.get(i).name());
        }
        return sb.toString();
    }

    /** Todos los mensajes en el formato de salida del enunciado. */
    public String getTexto() {
        StringBuilder sb = new StringBuilder();
        for (Mensaje m : mensajes) {
            sb.append(m).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getTexto();
    }
}
