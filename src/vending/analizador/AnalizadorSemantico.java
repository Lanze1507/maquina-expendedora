package vending.analizador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalizadorSemantico {

    private static class Producto {
        String nombre;
        int precio;
        int existencias;

        Producto(String nombre, int precio, int existencias) {
            this.nombre = nombre;
            this.precio = precio;
            this.existencias = existencias;
        }
    }

    private final List<String> errores = new ArrayList<>();
    private final Map<String, Producto> inventario = new HashMap<>();

    private int saldo;
    private String seleccionado;

    public void analizar(String codigo) {

        errores.clear();
        inventario.clear();
        saldo = 0;
        seleccionado = null;

        String[] lineas = codigo.split("\\R", -1);

        for (int i = 0; i < lineas.length; i++) {

            String instruccion = lineas[i].trim();
            int numeroLinea = i + 1;

            if (instruccion.isEmpty()) {
                continue;
            }

            if (!instruccion.endsWith(";")) {
                continue;
            }

            int apertura = instruccion.indexOf('(');
            int cierre = instruccion.lastIndexOf(')');

            if (apertura < 0 || cierre < apertura) {
                continue;
            }

            String comando = instruccion.substring(0, apertura).trim();

            String contenido = instruccion.substring(
                    apertura + 1, cierre
            ).trim();

            String[] parametros = contenido.isEmpty()
                    ? new String[0]
                    : contenido.split(",", -1);

            try {

                switch (comando) {

                    case "PRODUCTO" -> {

                        if (parametros.length != 4) break;

                        String codigoProducto = parametros[0].trim();
                        String nombre = parametros[1].trim();

                        int precio = Integer.parseInt(
                                parametros[2].trim()
                        );

                        int cantidad = Integer.parseInt(
                                parametros[3].trim()
                        );

                        if (inventario.containsKey(codigoProducto)) {
                            error(numeroLinea,
                                    "Codigo de producto duplicado: "
                                    + codigoProducto);
                        } else if (precio <= 0) {
                            error(numeroLinea,
                                    "El precio debe ser mayor que cero.");
                        } else if (cantidad < 0) {
                            error(numeroLinea,
                                    "La cantidad no puede ser negativa.");
                        } else {
                            inventario.put(
                                    codigoProducto,
                                    new Producto(nombre, precio, cantidad)
                            );
                        }
                    }

                    case "CARGAR" -> {

                        if (parametros.length != 2) break;

                        String codigoProducto = parametros[0].trim();
                        int cantidad = Integer.parseInt(
                                parametros[1].trim()
                        );

                        Producto producto = inventario.get(codigoProducto);

                        if (producto == null) {
                            error(numeroLinea,
                                    "No existe el producto "
                                    + codigoProducto + " para recargar.");
                        } else if (cantidad <= 0) {
                            error(numeroLinea,
                                    "La cantidad de recarga debe ser positiva.");
                        } else {
                            producto.existencias += cantidad;
                        }
                    }

                    case "MONEDA" -> {

                        if (parametros.length != 1) break;

                        int moneda = Integer.parseInt(
                                parametros[0].trim()
                        );

                        if (moneda != 1 && moneda != 5 && moneda != 10) {
                            error(numeroLinea,
                                    "Moneda no permitida: Q" + moneda);
                        } else {
                            saldo += moneda;
                        }
                    }

                    case "SELECCIONAR" -> {

                        if (parametros.length != 1) break;

                        String codigoProducto = parametros[0].trim();
                        Producto producto = inventario.get(codigoProducto);

                        if (saldo <= 0) {
                            error(numeroLinea,
                                    "Debe ingresar dinero antes de seleccionar.");
                        } else if (producto == null) {
                            error(numeroLinea,
                                    "Producto inexistente: "
                                    + codigoProducto);
                        } else if (producto.existencias <= 0) {
                            error(numeroLinea,
                                    "El producto " + codigoProducto
                                    + " no tiene existencias.");
                        } else {
                            seleccionado = codigoProducto;
                        }
                    }

                    case "COMPRAR" -> {

                        if (seleccionado == null) {
                            error(numeroLinea,
                                    "No existe un producto seleccionado.");
                            break;
                        }

                        Producto producto = inventario.get(seleccionado);

                        if (producto == null || producto.existencias <= 0) {
                            error(numeroLinea,
                                    "Producto no disponible.");
                        } else if (saldo < producto.precio) {
                            error(numeroLinea,
                                    "Dinero insuficiente. Faltan Q"
                                    + (producto.precio - saldo));
                        } else {
                            producto.existencias--;
                            saldo = 0;
                            seleccionado = null;
                        }
                    }

                    case "CANCELAR" -> {
                        saldo = 0;
                        seleccionado = null;
                    }

                    case "MOSTRAR" -> {
                        // No modifica el estado.
                    }

                    default -> {
                        // Las instrucciones desconocidas
                        // corresponden al analizador sintactico.
                    }
                }

            } catch (NumberFormatException ex) {
                error(numeroLinea,
                        "Se esperaba un valor numerico valido.");
            }
        }
    }

    private void error(int linea, String mensaje) {
        errores.add(
                "[ERROR SEMANTICO] Linea "
                + linea + ": " + mensaje
        );
    }

    public List<String> getErrores() {
        return new ArrayList<>(errores);
    }

public void analizarTokens(List<Token> tokens) {

    StringBuilder codigoNormalizado = new StringBuilder();
    List<Integer> lineasOriginales = new ArrayList<>();

    int lineaActual = -1;

    for (Token token : tokens) {

        if (lineaActual == -1) {
            lineaActual = token.getLinea();
        }

        codigoNormalizado.append(token.getValor());

        if (token.getTipo().equals("PUNTO_COMA")) {

            codigoNormalizado.append("\n");
            lineasOriginales.add(lineaActual);
            lineaActual = -1;
        }
    }

    analizar(codigoNormalizado.toString());

    List<String> erroresCorregidos = new ArrayList<>();

    for (String error : errores) {

        String corregido = error;

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile("Linea (\\d+):")
                        .matcher(error);

        if (matcher.find()) {

            int lineaNormalizada =
                    Integer.parseInt(matcher.group(1));

            if (lineaNormalizada >= 1
                    && lineaNormalizada <= lineasOriginales.size()) {

                int lineaOriginal =
                        lineasOriginales.get(lineaNormalizada - 1);

                corregido = error.substring(0, matcher.start())
                        + "Linea " + lineaOriginal + ":"
                        + error.substring(matcher.end());
            }
        }

        erroresCorregidos.add(corregido);
    }

    errores.clear();
    errores.addAll(erroresCorregidos);
}
}
