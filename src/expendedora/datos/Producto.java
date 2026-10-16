package expendedora.datos;

import java.math.BigDecimal;
import java.util.Objects;

/** Valor inmutable: los consumidores no pueden modificar el inventario indirectamente. */
public final class Producto {
    private final String codigo, nombre;
    private final BigDecimal precio;
    private final int cantidad;
    public Producto(String codigo, String nombre, BigDecimal precio, int cantidad) {
        this.codigo = texto(codigo, "Código");
        this.nombre = texto(nombre, "Nombre");
        if (precio == null || precio.signum() <= 0)
            throw new DatosException("PRECIO_INVALIDO", "El precio debe ser mayor que cero.");
        if (cantidad < 0) throw new DatosException("CANTIDAD_INVALIDA", "La cantidad no puede ser negativa.");
        validarNumero(precio);
        this.precio = precio.stripTrailingZeros();
        this.cantidad = cantidad;
    }
    static void validarNumero(BigDecimal n) {
        BigDecimal normalizado;
        try { normalizado = n.stripTrailingZeros(); }
        catch (ArithmeticException ex) {
            throw new DatosException("NUMERO_FUERA_DE_RANGO", "El exponente excede el rango numérico admitido.");
        }
        if (normalizado.precision() > 1000 || normalizado.scale() < -1000 || normalizado.scale() > 1000)
            throw new DatosException("NUMERO_FUERA_DE_RANGO", "Máximo 1000 dígitos y escala entre -1000 y 1000.");
    }
    static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty() || !valor.equals(valor.trim()))
            throw new DatosException("TEXTO_INVALIDO", campo + " debe tener texto sin espacios en los extremos.");
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (Character.isISOControl(c)) throw new DatosException("TEXTO_INVALIDO", campo + " contiene caracteres de control.");
            if (Character.isHighSurrogate(c)) {
                if (++i >= valor.length() || !Character.isLowSurrogate(valor.charAt(i)))
                    throw new DatosException("TEXTO_INVALIDO", campo + " contiene Unicode inválido.");
            } else if (Character.isLowSurrogate(c)) {
                throw new DatosException("TEXTO_INVALIDO", campo + " contiene Unicode inválido.");
            }
        }
        return valor;
    }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public boolean tieneExistencias() { return cantidad > 0; }
    Producto conCantidad(int valor) { return new Producto(codigo, nombre, precio, valor); }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Producto)) return false;
        Producto p = (Producto)o;
        return codigo.equals(p.codigo) && nombre.equals(p.nombre)
            && precio.compareTo(p.precio) == 0 && cantidad == p.cantidad;
    }
    @Override public int hashCode() { return Objects.hash(codigo, nombre, precio, cantidad); }
    @Override public String toString() { return codigo + " | " + nombre + " | Q" + precio.toPlainString() + " | " + cantidad; }
}
