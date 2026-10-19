package vending.automata;

import java.util.EnumMap;
import java.util.Map;

/**
 * Implementación de referencia del autómata (tabla de transiciones explícita).
 * Sirve para probar la lógica; la versión final la define la parte
 * "Autómata finito".
 *
 *   E0 --MONEDA------> E1        E0 --CANCELAR--> E0 (nada que devolver)
 *   E1 --MONEDA------> E1        E1 --CANCELAR--> E5
 *   E1 --SELECCIONAR-> E2        E2 --CANCELAR--> E5
 *   E2 --MONEDA------> E2
 *   E2 --SELECCIONAR-> E2        (cambiar de producto)
 *   E2 --COMPRAR-----> E3
 *   E3 --AUTORIZAR---> E4
 *   E4 --CON_CAMBIO--> E5
 *   E4 --SIN_CAMBIO--> E0
 *   E5 --DEVUELTO----> E0
 */
public class AutomataMaquina implements ControlEstados {

    private final Map<Estado, Map<Entrada, Estado>> tabla = new EnumMap<>(Estado.class);
    private Estado actual = Estado.E0;

    public AutomataMaquina() {
        for (Estado e : Estado.values()) {
            tabla.put(e, new EnumMap<Entrada, Estado>(Entrada.class));
        }
        t(Estado.E0, Entrada.MONEDA,      Estado.E1);
        t(Estado.E0, Entrada.CANCELAR,    Estado.E0);
        t(Estado.E1, Entrada.MONEDA,      Estado.E1);
        t(Estado.E1, Entrada.SELECCIONAR, Estado.E2);
        t(Estado.E1, Entrada.CANCELAR,    Estado.E5);
        t(Estado.E2, Entrada.MONEDA,      Estado.E2);
        t(Estado.E2, Entrada.SELECCIONAR, Estado.E2);
        t(Estado.E2, Entrada.COMPRAR,     Estado.E3);
        t(Estado.E2, Entrada.CANCELAR,    Estado.E5);
        t(Estado.E3, Entrada.AUTORIZAR,   Estado.E4);
        t(Estado.E4, Entrada.CON_CAMBIO,  Estado.E5);
        t(Estado.E4, Entrada.SIN_CAMBIO,  Estado.E0);
        t(Estado.E5, Entrada.DEVUELTO,    Estado.E0);
    }

    private void t(Estado origen, Entrada entrada, Estado destino) {
        tabla.get(origen).put(entrada, destino);
    }

    @Override
    public Estado getEstado() {
        return actual;
    }

    @Override
    public boolean puedeTransitar(Entrada entrada) {
        return tabla.get(actual).containsKey(entrada);
    }

    @Override
    public Estado transitar(Entrada entrada) {
        Estado destino = tabla.get(actual).get(entrada);
        if (destino == null) {
            throw new IllegalStateException(
                "Transicion no permitida: " + actual.name() + " --" + entrada + "-->");
        }
        actual = destino;
        return actual;
    }

    @Override
    public void reiniciar() {
        actual = Estado.E0;
    }
}
