package expendedora.datos;

/** Error de datos que el analizador puede asociar a la línea de la instrucción. */
public final class DatosException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    private final String codigo;
    public DatosException(String codigo, String mensaje) { super(mensaje); this.codigo = codigo; }
    public String getCodigo() { return codigo; }
}
