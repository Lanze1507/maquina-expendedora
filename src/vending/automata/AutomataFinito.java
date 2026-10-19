package vending.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Modulo de automata finito de la maquina expendedora.
 *
 * Responsabilidad: controlar de forma explicita E0..E5 y las transiciones.
 * NO analiza .vm, NO administra saldo/inventario, NO modifica la GUI ni JSON.
 * La logica de operaciones valida monedas, saldo, producto y stock ANTES de
 * llamar a transitar; si hay un error semantico, no debe enviar ese evento.
 */
public final class AutomataFinito implements ControlEstados {

    /** Transicion inmutable. */
    public static final class Transicion {
        private final Estado origen;
        private final Entrada entrada;
        private final Estado destino;

        private Transicion(Estado origen, Entrada entrada, Estado destino) {
            this.origen = origen;
            this.entrada = entrada;
            this.destino = destino;
        }

        public Estado getOrigen() { return origen; }
        public Entrada getEntrada() { return entrada; }
        public Estado getDestino() { return destino; }

        @Override
        public String toString() {
            return origen.name() + " --" + entrada.name() + "--> " + destino.name();
        }
    }

    /** Notifica el recorrido a la logica, al historial o a una futura GUI. */
    public interface Observador {
        void alTransitar(Transicion transicion);
    }

    private final EnumMap<Estado, EnumMap<Entrada, Estado>> tabla =
            new EnumMap<Estado, EnumMap<Entrada, Estado>>(Estado.class);
    private final List<Transicion> historial = new ArrayList<Transicion>();
    private final List<Observador> observadores = new ArrayList<Observador>();
    private Estado actual = Estado.E0;

    public AutomataFinito() {
        for (Estado estado : Estado.values()) {
            tabla.put(estado, new EnumMap<Entrada, Estado>(Entrada.class));
        }

        // E0: sin saldo y listo para empezar.
        registrar(Estado.E0, Entrada.MONEDA, Estado.E1);
        registrar(Estado.E0, Entrada.CANCELAR, Estado.E0);

        // E1: recibe mas monedas, selecciona o cancela.
        registrar(Estado.E1, Entrada.MONEDA, Estado.E1);
        registrar(Estado.E1, Entrada.SELECCIONAR, Estado.E2);
        registrar(Estado.E1, Entrada.CANCELAR, Estado.E5);

        // E2: acepta mas saldo sin perder seleccion, permite cambiarla,
        // comprar si las guardas ya pasaron, o cancelar.
        registrar(Estado.E2, Entrada.MONEDA, Estado.E2);
        registrar(Estado.E2, Entrada.SELECCIONAR, Estado.E2);
        registrar(Estado.E2, Entrada.COMPRAR, Estado.E3);
        registrar(Estado.E2, Entrada.CANCELAR, Estado.E5);

        // E3: procesamiento confirmado por la logica -> despacho.
        registrar(Estado.E3, Entrada.AUTORIZAR, Estado.E4);

        // E4: con cambio se pasa por E5; sin cambio finaliza en E0.
        registrar(Estado.E4, Entrada.CON_CAMBIO, Estado.E5);
        registrar(Estado.E4, Entrada.SIN_CAMBIO, Estado.E0);

        // E5: se devolvio el saldo o cambio y se termina la operacion.
        registrar(Estado.E5, Entrada.DEVUELTO, Estado.E0);
    }

    private void registrar(Estado origen, Entrada entrada, Estado destino) {
        tabla.get(origen).put(entrada, destino);
    }

    @Override
    public Estado getEstado() {
        return actual;
    }

    /** Alias util para construir VendingViewModel sin transformar los estados. */
    public Estado getEstadoActual() {
        return getEstado();
    }

    public String getCodigoEstado() {
        return actual.name();
    }

    public String getDescripcionEstado() {
        return actual.getDescripcion();
    }

    @Override
    public boolean puedeTransitar(Entrada entrada) {
        return entrada != null && tabla.get(actual).containsKey(entrada);
    }

    @Override
    public Estado transitar(Entrada entrada) {
        if (!puedeTransitar(entrada)) {
            throw new IllegalStateException("Transicion no permitida: "
                    + actual.name() + " --" + entrada + "--> ?");
        }
        Estado origen = actual;
        Estado destino = tabla.get(origen).get(entrada);
        actual = destino;
        Transicion paso = new Transicion(origen, entrada, destino);
        historial.add(paso);
        // Copia defensiva por si un observador altera la suscripcion.
        for (Observador observador : new ArrayList<Observador>(observadores)) {
            observador.alTransitar(paso);
        }
        return actual;
    }

    @Override
    public void reiniciar() {
        actual = Estado.E0;
        historial.clear();
    }

    /** Recorrido completo desde el ultimo reinicio (copia de solo lectura). */
    public List<Transicion> getHistorial() {
        return Collections.unmodifiableList(new ArrayList<Transicion>(historial));
    }

    /** E0 -> E1 -> E2, en formato util para informes o pruebas. */
    public String getRecorridoTexto() {
        if (historial.isEmpty()) {
            return actual.name();
        }
        StringBuilder sb = new StringBuilder(historial.get(0).getOrigen().name());
        for (Transicion transicion : historial) {
            sb.append(" -> ").append(transicion.getDestino().name());
        }
        return sb.toString();
    }

    /** Tabla de transiciones inmutable, apta para mostrar/explicar el automata. */
    public Map<Estado, Map<Entrada, Estado>> getTablaTransiciones() {
        EnumMap<Estado, Map<Entrada, Estado>> copia =
                new EnumMap<Estado, Map<Entrada, Estado>>(Estado.class);
        for (Estado estado : Estado.values()) {
            EnumMap<Entrada, Estado> fila = new EnumMap<Entrada, Estado>(Entrada.class);
            fila.putAll(tabla.get(estado));
            copia.put(estado, Collections.unmodifiableMap(fila));
        }
        return Collections.unmodifiableMap(copia);
    }

    public void agregarObservador(Observador observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void quitarObservador(Observador observador) {
        observadores.remove(observador);
    }
}
