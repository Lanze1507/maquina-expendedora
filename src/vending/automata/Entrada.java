package vending.automata;

/**
 * Símbolos de entrada del autómata.
 * Los cinco primeros vienen del usuario; los demás son eventos internos que
 * genera la lógica de la máquina mientras procesa una compra o cancelación.
 */
public enum Entrada {
    MONEDA,        // usuario: MONEDA(v)
    SELECCIONAR,   // usuario: SELECCIONAR(c)
    COMPRAR,       // usuario: COMPRAR()  (con selección y saldo suficiente)
    CANCELAR,      // usuario: CANCELAR()
    AUTORIZAR,     // interno: compra validada -> entregar
    CON_CAMBIO,    // interno: producto entregado y hay cambio
    SIN_CAMBIO,    // interno: producto entregado, sin cambio
    DEVUELTO       // interno: dinero devuelto, fin de operación
}
