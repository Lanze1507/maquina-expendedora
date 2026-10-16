package expendedora.datos;

import java.math.BigDecimal;
import java.util.*;

/** Instantánea validada. Construirla no cambia la máquina en ejecución. */
public final class ConfiguracionMaquina {
    private final List<Producto> productos;
    private final BigDecimal saldo;
    private final String seleccion;
    private final EstadoPersistido estado;
    public ConfiguracionMaquina(Collection<Producto> productos, BigDecimal saldo,
                               String seleccion, EstadoPersistido estado) {
        Inventario copia = new Inventario();
        copia.reemplazar(productos);
        if (saldo == null || saldo.signum() < 0)
            throw new DatosException("SALDO_INVALIDO", "El saldo no puede ser nulo ni negativo.");
        if (estado == null) throw new DatosException("ESTADO_INVALIDO", "Falta el estado de la máquina.");
        if (seleccion != null) copia.buscar(seleccion);
        if (estado == EstadoPersistido.E0 && (saldo.signum() != 0 || seleccion != null))
            throw new DatosException("ESTADO_INCONSISTENTE", "E0 requiere saldo cero y ninguna selección.");
        if (estado == EstadoPersistido.E1 && (saldo.signum() <= 0 || seleccion != null))
            throw new DatosException("ESTADO_INCONSISTENTE", "E1 requiere saldo positivo y ninguna selección.");
        if (estado == EstadoPersistido.E2 || estado == EstadoPersistido.E3 || estado == EstadoPersistido.E4) {
            if (saldo.signum() <= 0 || seleccion == null)
                throw new DatosException("ESTADO_INCONSISTENTE", "E2/E3/E4 requieren saldo positivo y un producto seleccionado.");
            Producto p = copia.buscar(seleccion);
            if (estado != EstadoPersistido.E4 && !p.tieneExistencias())
                throw new DatosException("ESTADO_INCONSISTENTE", "La selección no tiene existencias.");
            if (estado != EstadoPersistido.E2 && saldo.compareTo(p.getPrecio()) < 0)
                throw new DatosException("ESTADO_INCONSISTENTE", "E3/E4 requieren saldo suficiente para la compra.");
        }
        this.productos = copia.listar();
        Producto.validarNumero(saldo);
        this.saldo = saldo.stripTrailingZeros();
        this.seleccion = seleccion;
        this.estado = estado;
    }
    public static ConfiguracionMaquina inicial(Inventario inventario) {
        return new ConfiguracionMaquina(inventario.listar(), BigDecimal.ZERO, null, EstadoPersistido.E0);
    }
    public List<Producto> getProductos() { return productos; }
    public BigDecimal getSaldo() { return saldo; }
    public String getSeleccion() { return seleccion; }
    public EstadoPersistido getEstado() { return estado; }
    public Inventario crearInventario() { Inventario i = new Inventario(); i.reemplazar(productos); return i; }
}
