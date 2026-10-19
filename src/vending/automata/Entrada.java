package vending.automata;

/**
 * Entradas externas y eventos internos reconocidos por el automata.
 * MOSTRAR, PRODUCTO y CARGAR no alteran el estado de la compra.
 * Las validaciones de saldo/stock ocurren antes de enviar eventos.
 */
public enum Entrada {
    MONEDA,       // Ingreso de moneda previamente validada.
    SELECCIONAR,  // Producto existente y con existencias.
    COMPRAR,      // Compra previamente validada con saldo suficiente.
    CANCELAR,     // Solicitud de cancelar la operacion.
    AUTORIZAR,    // Evento interno: compra validada -> despacho.
    CON_CAMBIO,   // Evento interno: se debe devolver cambio.
    SIN_CAMBIO,   // Evento interno: compra exacta.
    DEVUELTO      // Evento interno: devolucion terminada.
}
