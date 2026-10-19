package vending.automata;

/**
 * Contrato entre la lógica de operaciones y el autómata finito.
 *
 * NOTA DE INTEGRACIÓN: el compañero del "Autómata finito" puede implementar
 * esta interfaz en su clase (o adaptar la suya con estos tres métodos) y
 * pasarla al constructor de MaquinaExpendedora. AutomataMaquina es una
 * implementación de referencia para poder probar.
 */
public interface ControlEstados {

    /** Estado actual. */
    Estado getEstado();

    /** true si desde el estado actual existe transición con esa entrada. */
    boolean puedeTransitar(Entrada entrada);

    /**
     * Ejecuta la transición y devuelve el nuevo estado.
     * @throws IllegalStateException si la transición no está permitida.
     */
    Estado transitar(Entrada entrada);

    /** Regresa al estado inicial E0. */
    void reiniciar();
}
