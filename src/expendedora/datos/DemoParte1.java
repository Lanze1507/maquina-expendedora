package expendedora.datos;

import java.math.BigDecimal;
import java.nio.file.*;

/** Demuestra exclusivamente datos y persistencia, sin interpretar instrucciones .vm. */
public final class DemoParte1 {
    public static void main(String[] args) throws Exception {
        Path archivo = Paths.get(args.length == 0 ? "estado-demo.json" : args[0]);
        Inventario inventario = new Inventario();
        inventario.registrar("A1", "AGUA", new BigDecimal("5"), 10);
        inventario.registrar("A2", "GASEOSA", new BigDecimal("8"), 5);
        inventario.registrar("A3", "GALLETA", new BigDecimal("3"), 0);
        inventario.cargar("A1", 5);
        inventario.cargar("A2", 10);
        System.out.println("Inventario registrado y recargado:");
        for (Producto p : inventario.listar()) System.out.println(p);
        ConfiguracionMaquina config = new ConfiguracionMaquina(inventario.listar(), new BigDecimal("10"), "A2", EstadoPersistido.E2);
        PersistenciaJson.exportar(archivo, config);
        ConfiguracionMaquina recuperada = PersistenciaJson.importar(archivo);
        System.out.println("JSON recuperado: " + archivo.toAbsolutePath());
        System.out.println("Saldo: Q" + recuperada.getSaldo().toPlainString() + " | Selección: " + recuperada.getSeleccion() + " | Estado: " + recuperada.getEstado());
        try { inventario.registrar("A1", "OTRO", BigDecimal.ONE, 1); }
        catch (DatosException ex) { System.out.println("Rechazo esperado: " + ex.getCodigo() + " - " + ex.getMessage()); }
    }
}
