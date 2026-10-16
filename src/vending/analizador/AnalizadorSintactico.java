
package vending.analizador;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSintactico {

    private final List<String> errores = new ArrayList<>();
    private List<Token> tokens;
    private int posicion;

    public void analizar(List<Token> tokens) {
        this.tokens = tokens;
        this.posicion = 0;
        errores.clear();

        while (posicion < tokens.size()) {
            analizarInstruccion();
        }
    }

    private void analizarInstruccion() {

        int inicio = posicion;
        Token actual = tokens.get(posicion);

        if (!actual.getTipo().equals("INSTRUCCION")) {
            error(actual, "Se esperaba una instruccion valida.");
            recuperar();
            return;
        }

        String instruccion = actual.getValor();
        posicion++;

        if (!consumir("PARENTESIS_ABRE",
                "Falta el parentesis de apertura '('.")) {
            recuperar();
            return;
        }

        String[] tiposEsperados = switch (instruccion) {
            case "PRODUCTO" -> new String[]{
                "IDENTIFICADOR", "IDENTIFICADOR",
                "NUMERO", "NUMERO"
            };
            case "CARGAR" -> new String[]{
                "IDENTIFICADOR", "NUMERO"
            };
            case "MONEDA" -> new String[]{"NUMERO"};
            case "SELECCIONAR" -> new String[]{
                "IDENTIFICADOR"
            };
            case "COMPRAR", "CANCELAR", "MOSTRAR" ->
                new String[]{};
            default -> new String[]{};
        };

        for (int i = 0; i < tiposEsperados.length; i++) {
        if (i > 0) {

            if (posicion < tokens.size()
                    && tokens.get(posicion).getTipo()
                            .equals("PARENTESIS_CIERRA")) {

             error(tokens.get(posicion),
                     "Faltan parametros en " + instruccion + ".");
                recuperar();
             return;
         }

    if (!consumir("COMA",
            "Falta una coma entre parametros.")) {
        recuperar();
        return;
    }
}
String[] nombresParametros = switch (instruccion) {
    case "PRODUCTO" -> new String[]{
        "codigo", "nombre", "precio", "existencias"
    };
    case "CARGAR" -> new String[]{
        "codigo", "cantidad"
    };
    case "MONEDA" -> new String[]{"valor de moneda"};
    case "SELECCIONAR" -> new String[]{"codigo"};
    default -> new String[]{};
};

String nombreParametro = i < nombresParametros.length
        ? nombresParametros[i]
        : "desconocido";

if (!consumir(tiposEsperados[i],
        "Parametro incorrecto o faltante: "
        + nombreParametro + " en " + instruccion + ".")) {
    recuperar();
    return;
}

        }
if (posicion < tokens.size()) {

    Token siguiente = tokens.get(posicion);

    if (!siguiente.getTipo().equals("PARENTESIS_CIERRA")) {

        if (siguiente.getTipo().equals("NUMERO")
                || siguiente.getTipo().equals("IDENTIFICADOR")
                || siguiente.getTipo().equals("COMA")) {

            error(siguiente,
                    "La instruccion " + instruccion
                    + " contiene parametros adicionales.");

        } else {
            error(siguiente,
                    "Falta el parentesis de cierre ')' "
                    + "en " + instruccion + ".");
        }

        recuperar();
        return;
    }

} else {

    error(tokens.get(tokens.size() - 1),
            "Falta el parentesis de cierre ')' "
            + "en " + instruccion + ".");
    return;
}

posicion++;


        if (!consumir("PUNTO_COMA",
                "Falta el punto y coma ';'.")) {
            // Si empieza otra instruccion, no saltarla.
            if (posicion < tokens.size()
                    && tokens.get(posicion).getTipo()
                            .equals("INSTRUCCION")) {
                return;
            }
            recuperar();
        }

        // Evita un ciclo infinito ante errores.
        if (posicion == inicio) {
            posicion++;
        }
    }

    private boolean consumir(String tipo, String mensaje) {

        if (posicion < tokens.size()
                && tokens.get(posicion).getTipo().equals(tipo)) {
            posicion++;
            return true;
        }

        Token referencia;

        if (posicion < tokens.size()) {
            referencia = tokens.get(posicion);
        } else {
            referencia = tokens.get(tokens.size() - 1);
        }

        error(referencia, mensaje);
        return false;
    }

    private void error(Token token, String mensaje) {
        errores.add(
                "[ERROR SINTACTICO] Linea "
                + token.getLinea()
                + ": " + mensaje
        );
    }

    private void recuperar() {
        while (posicion < tokens.size()) {

            String tipo = tokens.get(posicion).getTipo();

            if (tipo.equals("PUNTO_COMA")) {
                posicion++;
                return;
            }

            if (tipo.equals("INSTRUCCION")) {
                return;
            }

            posicion++;
        }
    }

    public List<String> getErrores() {
        return new ArrayList<>(errores);
    }
}
