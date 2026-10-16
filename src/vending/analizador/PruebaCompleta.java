
package vending.analizador;

public class PruebaCompleta {

    public static void main(String[] args) {

        String codigo =
                "PRODUCTO(A1,AGUA,5,10);\n" +
                "MONEDA(5);\n" +
                "SELECCIONAR(A1);\n" +
                "COMPRAR();";

        AnalizadorCompleto analizador =
                new AnalizadorCompleto();

        System.out.println(analizador.analizar(codigo));
    }
}
