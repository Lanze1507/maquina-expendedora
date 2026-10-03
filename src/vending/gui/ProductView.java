package vending.gui;

/**
 * DTO utilizado por la GUI para mostrar un producto.
 * La lógica real del producto puede vivir en el módulo model del equipo.
 */
public record ProductView(
        String code,
        String name,
        double price,
        int stock
) {}
