
package vending.analizador;

import java.util.List;

public class AnalizadorCompleto {

    public String analizar(String codigo) {

        StringBuilder resultado = new StringBuilder();

        AnalizadorLexico lexico = new AnalizadorLexico();
        List<Token> tokens = lexico.analizar(codigo);

        resultado.append("=== TABLA DE TOKENS ===\n");

        resultado.append(String.format(
                "%-18s %-18s %-8s %-8s%n",
                "TIPO", "VALOR", "LINEA", "COLUMNA"
        ));

        resultado.append(
                "------------------------------------------------------\n"
        );

        for (Token token : tokens) {
            resultado.append(String.format(
                    "%-18s %-18s %-8d %-8d%n",
                    token.getTipo(),
                    token.getValor(),
                    token.getLinea(),
                    token.getColumna()
            ));
        }

        resultado.append("\n=== ANALISIS LEXICO ===\n");

        if (!lexico.getErrores().isEmpty()) {
            for (String error : lexico.getErrores()) {
                resultado.append(error).append("\n");
            }
            return resultado.toString();
        }

        resultado.append("[OK] Sin errores lexicos.\n");

        AnalizadorSintactico sintactico =
                new AnalizadorSintactico();

        sintactico.analizar(tokens);

        resultado.append("\n=== ANALISIS SINTACTICO ===\n");

        if (!sintactico.getErrores().isEmpty()) {
            for (String error : sintactico.getErrores()) {
                resultado.append(error).append("\n");
            }
            return resultado.toString();
        }

        resultado.append("[OK] Sin errores sintacticos.\n");

        AnalizadorSemantico semantico =
                new AnalizadorSemantico();

        semantico.analizarTokens(tokens);

        resultado.append("\n=== ANALISIS SEMANTICO ===\n");

        if (!semantico.getErrores().isEmpty()) {
            for (String error : semantico.getErrores()) {
                resultado.append(error).append("\n");
            }
        } else {
            resultado.append("[OK] Sin errores semanticos.\n");
        }

        return resultado.toString();
    }
}
