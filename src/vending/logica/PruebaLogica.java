package vending.logica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import java.math.BigDecimal;

import expendedora.datos.Inventario;
import expendedora.datos.Producto;

/**
 * Prueba de consola de la LÓGICA DE OPERACIONES con los ejemplos del enunciado.
 *
 * El mini-intérprete de abajo es SOLO PARA PRUEBAS: el análisis léxico,
 * sintáctico y semántico real lo hace otra parte del proyecto, que al final
 * llamará a maquina.ejecutar(instruccion, parametros, linea).
 */
public class PruebaLogica {

    private static final Pattern INSTR = Pattern.compile("^\\s*([A-Z]+)\\s*\\((.*)\\)\\s*;\\s*$");

    public static void main(String[] args) {
        correr("EJEMPLO 1: COMPRA EXITOSA",
            "PRODUCTO(A1,AGUA,5,10);\n" +
            "PRODUCTO(A2,GASEOSA,8,5);\n" +
            "PRODUCTO(A3,GALLETA,3,15);\n" +
            "\n" +
            "CARGAR(A1,5);\n" +
            "CARGAR(A2,10);\n" +
            "\n" +
            "MOSTRAR();\n" +
            "\n" +
            "MONEDA(5);\n" +
            "MONEDA(5);\n" +
            "\n" +
            "SELECCIONAR(A2);\n" +
            "COMPRAR();\n" +
            "\n" +
            "MOSTRAR();");

        correr("EJEMPLO 2: SIN STOCK",
            "PRODUCTO(A1,AGUA,5,10);\n" +
            "PRODUCTO(A2,GASEOSA,8,0);\n" +
            "PRODUCTO(A3,GALLETA,3,15);\n" +
            "\n" +
            "MOSTRAR();\n" +
            "\n" +
            "MONEDA(5);\n" +
            "MONEDA(5);\n" +
            "\n" +
            "SELECCIONAR(A2);\n" +
            "COMPRAR();\n" +
            "\n" +
            "CANCELAR();\n" +
            "\n" +
            "MOSTRAR();");

        correr("EJEMPLO 3: DINERO INSUFICIENTE",
            "PRODUCTO(A1,AGUA,5,10);\n" +
            "PRODUCTO(A2,GASEOSA,8,5);\n" +
            "PRODUCTO(A3,GALLETA,3,15);\n" +
            "\n" +
            "MONEDA(5);\n" +
            "\n" +
            "SELECCIONAR(A2);\n" +
            "COMPRAR();\n" +
            "\n" +
            "MONEDA(5);\n" +
            "COMPRAR();\n" +
            "\n" +
            "MOSTRAR();");

        correr("EJEMPLO 4: OTRAS VALIDACIONES SEMANTICAS",
            "PRODUCTO(A1,AGUA,5,10);\n" +
            "MONEDA(100);\n" +          // moneda no permitida
            "SELECCIONAR(A1);\n" +      // sin dinero
            "MONEDA(10);\n" +
            "SELECCIONAR(Z9);\n" +      // producto inexistente
            "COMPRAR();\n" +            // comprar sin selección
            "SELECCIONAR(A1);\n" +
            "COMPRAR();\n" +            // compra exitosa con cambio Q5
            "COMPRAR();\n" +            // compra confirmada dos veces
            "MONEDA(1);\n" +
            "MONEDA(1);\n" +
            "MONEDA(1);\n" +
            "MONEDA(1);\n" +
            "MONEDA(1);\n" +
            "SELECCIONAR(A1);\n" +
            "COMPRAR();\n" +            // compra exacta, sin cambio
            "CANCELAR();");             // cancelar sin operación
    }

    private static void correr(String titulo, String programa) {
        System.out.println("==================== " + titulo + " ====================");
        Inventario inv = new Inventario();
        MaquinaExpendedora maquina = new MaquinaExpendedora(inv);

        String[] lineas = programa.split("\n", -1);
        for (int i = 0; i < lineas.length; i++) {
            int numLinea = i + 1;
            Matcher m = INSTR.matcher(lineas[i]);
            if (!m.matches()) continue;
            String instr = m.group(1);
            List<String> params = m.group(2).trim().isEmpty()
                    ? new ArrayList<String>()
                    : Arrays.asList(m.group(2).split(","));

            // PRODUCTO y CARGAR: corresponden a la parte de inventario (simulado aquí)
            if (instr.equals("PRODUCTO")) {
                Producto p = inv.registrar(params.get(0).trim(), params.get(1).trim(),
                        new BigDecimal(params.get(2).trim()), Integer.parseInt(params.get(3).trim()));
                System.out.println("[OK] Producto " + p.getCodigo() + " registrado: " + p.getNombre());
                continue;
            }
            if (instr.equals("CARGAR")) {
                int antes = inv.buscar(params.get(0).trim()).getCantidad();
                int n = Integer.parseInt(params.get(1).trim());
                Producto p = inv.cargar(params.get(0).trim(), n);
                System.out.println("[OK] Recarga " + p.getCodigo() + ": " + antes + " + " + n
                        + " = " + p.getCantidad() + " unidades");
                continue;
            }

            // Instrucciones de operación -> LÓGICA DE LA MÁQUINA
            Resultado r = maquina.ejecutar(instr, params, numLinea);
            System.out.print(r.getTexto());
            System.out.println("      (estados: " + r.getRecorridoTexto() + ")");
            if (r.getTablaInventario() != null) {
                System.out.print(r.getTablaInventario());
            }
        }
        System.out.println();
    }
}
