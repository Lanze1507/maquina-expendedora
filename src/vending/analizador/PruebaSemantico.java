
package vending.analizador;

public class PruebaSemantico {

    public static void main(String[] args) {

        String codigo =
                "PRODUCTO(A1,AGUA,5,10);\n" +
                "PRODUCTO(A1,GASEOSA,8,5);\n" +
                "MONEDA(100);\n" +
                "MONEDA(1);\n" +
                "SELECCIONAR(A1);\n" +
                "COMPRAR();\n" +
                "SELECCIONAR(A9);\n";

        AnalizadorSemantico semantico =
                new AnalizadorSemantico();

        semantico.analizar(codigo);

        System.out.println("=== ANALISIS SEMANTICO ===");

        if (semantico.getErrores().isEmpty()) {
            System.out.println(
                    "No se encontraron errores semanticos."
            );
        } else {
            for (String error : semantico.getErrores()) {
                System.out.println(error);
            }
        }
    }
}
