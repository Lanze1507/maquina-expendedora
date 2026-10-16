package expendedora.datos;

import java.math.BigDecimal;
import java.util.*;

/** Almacena productos en orden de registro. Cada mutación es indivisible. */
public final class Inventario {
    private final Map<String, Producto> productos = new LinkedHashMap<>();
    public synchronized Producto registrar(String codigo, String nombre, BigDecimal precio, int cantidad) {
        Producto p = new Producto(codigo, nombre, precio, cantidad);
        if (productos.containsKey(codigo)) throw new DatosException("CODIGO_DUPLICADO", "Ya existe el producto " + codigo + ".");
        productos.put(codigo, p);
        return p;
    }
    public synchronized Producto buscar(String codigo) {
        Producto.texto(codigo, "Código");
        Producto p = productos.get(codigo);
        if (p == null) throw new DatosException("PRODUCTO_INEXISTENTE", "No existe el producto " + codigo + ".");
        return p;
    }
    public synchronized Producto cargar(String codigo, int unidades) {
        Producto anterior = buscar(codigo);
        if (unidades <= 0) throw new DatosException("RECARGA_INVALIDA", "La recarga debe ser mayor que cero.");
        if (unidades > Integer.MAX_VALUE - anterior.getCantidad())
            throw new DatosException("DESBORDAMIENTO", "La recarga excede el máximo de existencias.");
        Producto nuevo = anterior.conCantidad(anterior.getCantidad() + unidades);
        productos.put(codigo, nuevo);
        return nuevo;
    }
    /** La Parte 2 llama este método SOLO después de autorizar la compra. */
    public synchronized Producto descontarUnidad(String codigo) {
        Producto anterior = buscar(codigo);
        if (!anterior.tieneExistencias()) throw new DatosException("SIN_EXISTENCIAS", "El producto " + codigo + " está agotado.");
        Producto nuevo = anterior.conCantidad(anterior.getCantidad() - 1);
        productos.put(codigo, nuevo);
        return nuevo;
    }
    public synchronized List<Producto> listar() {
        return Collections.unmodifiableList(new ArrayList<>(productos.values()));
    }
    /** Valida TODO antes de sustituir. Un error deja el inventario original intacto. */
    public synchronized void reemplazar(Collection<Producto> nuevos) {
        if (nuevos == null) throw new DatosException("DATOS_INVALIDOS", "Falta la lista de productos.");
        Map<String, Producto> copia = new LinkedHashMap<>();
        for (Producto p : nuevos) {
            if (p == null) throw new DatosException("DATOS_INVALIDOS", "La lista contiene un producto nulo.");
            if (copia.put(p.getCodigo(), p) != null)
                throw new DatosException("CODIGO_DUPLICADO", "Código repetido: " + p.getCodigo());
        }
        productos.clear();
        productos.putAll(copia);
    }
}
