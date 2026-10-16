
package vending.analizador;

import java.util.List;

public class PruebaSintactico {

    public static void main(String[] args) {

        String codigo =
            "PRODUCTO(A1,AGUA,5,10);\n" +
            "MONEDA(5);\n" +
            "SELECCIONAR(A1);\n" +
            "COMPRAR();";

        AnalizadorLexico lexico =
                new AnalizadorLexico();

        List<Token> tokens = lexico.analizar(codigo);

        AnalizadorSintactico sintactico =
                new AnalizadorSintactico();

        sintactico.analizar(tokens);

        System.out.println("=== ANALISIS SINTACTICO ===");

        for (String error : lexico.getErrores()) {
            System.out.println(error);
        }

        for (String error : sintactico.getErrores()) {
            System.out.println(error);
        }

        if (lexico.getErrores().isEmpty()
                && sintactico.getErrores().isEmpty()) {
            System.out.println(
                    "Todas las instrucciones son correctas."
            );
        }
    }
}
