
package vending.analizador;

import java.util.List;

public class PruebaLexico {

    public static void main(String[] args) {

        String codigo =
                "PRODUCTO(A1,AGUA,5,10);\n" +
                "MONEDA(@);\n" +
                "SELECCIONAR(A1);\n" +
                "COMPRAR();";

        AnalizadorLexico analizador =
                new AnalizadorLexico();

        List<Token> tokens = analizador.analizar(codigo);

        System.out.println("=== ANALISIS LEXICO ===");

        for (Token token : tokens) {
            System.out.println(
                    "Tipo: " + token.getTipo() +
                    " | Valor: " + token.getValor() +
                    " | Linea: " + token.getLinea()
            );
        }

        System.out.println("\n=== ERRORES ===");

        if (analizador.getErrores().isEmpty()) {
            System.out.println("No se encontraron errores lexicos.");
        } else {
            for (String error : analizador.getErrores()) {
                System.out.println(error);
            }
        }
    }
}
