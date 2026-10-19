package vending.automata;

/** Los seis estados exigidos por el apartado 5 del proyecto. */
public enum Estado {
    E0("Esperando dinero"),
    E1("Dinero ingresado"),
    E2("Producto seleccionado"),
    E3("Procesando compra"),
    E4("Entregando producto"),
    E5("Devolviendo dinero");

    private final String descripcion;

    Estado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return name() + " - " + descripcion;
    }
}
