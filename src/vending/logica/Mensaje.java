package vending.logica;

/**
 * Un mensaje de salida de la máquina, con el formato del enunciado:
 *   [OK] Moneda recibida: Q5
 *   [ERROR SEMANTICO] Linea 8:
 *   Dinero insuficiente para realizar la compra.
 */
public class Mensaje {

    public enum Tipo { OK, ERROR_SEMANTICO }

    private final Tipo tipo;
    private final int linea;      // <= 0 cuando la acción viene de un botón de la GUI
    private final String texto;   // puede tener varias líneas separadas por \n

    public Mensaje(Tipo tipo, int linea, String texto) {
        this.tipo = tipo;
        this.linea = linea;
        this.texto = texto;
    }

    public static Mensaje ok(String texto) {
        return new Mensaje(Tipo.OK, 0, texto);
    }

    public static Mensaje error(int linea, String texto) {
        return new Mensaje(Tipo.ERROR_SEMANTICO, linea, texto);
    }

    public Tipo getTipo()     { return tipo; }
    public int getLinea()     { return linea; }
    public String getTexto()  { return texto; }
    public boolean esError()  { return tipo == Tipo.ERROR_SEMANTICO; }

    @Override
    public String toString() {
        if (tipo == Tipo.OK) {
            return "[OK] " + texto;
        }
        String cabecera = linea > 0 ? "[ERROR SEMANTICO] Linea " + linea + ":" : "[ERROR SEMANTICO]:";
        return cabecera + "\n" + texto;
    }
}
