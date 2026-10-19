package vending.automata;

/** Contrato del automata para la logica y para un controlador de la GUI. */
public interface ControlEstados {
    Estado getEstado();

    boolean puedeTransitar(Entrada entrada);

    /** @throws IllegalStateException si la entrada no es valida en el estado actual. */
    Estado transitar(Entrada entrada);

    /** Vuelve al estado inicial para iniciar otra simulacion o restauracion. */
    void reiniciar();
}
