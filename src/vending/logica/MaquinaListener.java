package vending.logica;

/**
 * La GUI implementa esta interfaz para refrescarse (saldo, estado, inventario,
 * producto entregado) cada vez que la máquina ejecuta una operación.
 */
public interface MaquinaListener {
    void maquinaActualizada(MaquinaExpendedora maquina, Resultado ultimoResultado);
}
