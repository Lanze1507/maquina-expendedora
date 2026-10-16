
package vending.analizador;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorLexico {

    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errores = new ArrayList<>();

    public List<Token> analizar(String codigo) {

        tokens.clear();
        errores.clear();

        int linea = 1;
        int columna = 1;
        int i = 0;

        while (i < codigo.length()) {

            char c = codigo.charAt(i);

            if (c == '\n') {
                linea++;
                columna = 1;
                i++;
                continue;
            }

            if (Character.isWhitespace(c)) {
                columna++;
                i++;
                continue;
            }

            if (Character.isLetter(c) || c == '_') {

                int inicio = i;
                int columnaInicio = columna;

                while (i < codigo.length() &&
                        (Character.isLetterOrDigit(codigo.charAt(i))
                        || codigo.charAt(i) == '_')) {
                    i++;
                    columna++;
                }

                String palabra = codigo.substring(inicio, i);
                String tipo = esInstruccion(palabra)
                        ? "INSTRUCCION" : "IDENTIFICADOR";

                tokens.add(new Token(
                        tipo, palabra, linea, columnaInicio
                ));

                continue;
            }

            if (Character.isDigit(c)) {

                int inicio = i;
                int columnaInicio = columna;

                while (i < codigo.length()
                        && Character.isDigit(codigo.charAt(i))) {
                    i++;
                    columna++;
                }

                tokens.add(new Token(
                        "NUMERO",
                        codigo.substring(inicio, i),
                        linea,
                        columnaInicio
                ));

                continue;
            }

            String tipo = switch (c) {
                case '(' -> "PARENTESIS_ABRE";
                case ')' -> "PARENTESIS_CIERRA";
                case ',' -> "COMA";
                case ';' -> "PUNTO_COMA";
                default -> null;
            };

            if (tipo != null) {
                tokens.add(new Token(
                        tipo, String.valueOf(c), linea, columna
                ));
            } else {
                errores.add(
                        "[ERROR LEXICO] Linea " + linea
                        + ", columna " + columna
                        + ": Caracter no reconocido: " + c
                );
            }

            i++;
            columna++;
        }

        return new ArrayList<>(tokens);
    }

    private boolean esInstruccion(String palabra) {
        return switch (palabra) {
            case "PRODUCTO", "CARGAR", "MONEDA",
                 "SELECCIONAR", "COMPRAR",
                 "CANCELAR", "MOSTRAR" -> true;
            default -> false;
        };
    }

    public List<String> getErrores() {
        return new ArrayList<>(errores);
    }
}
