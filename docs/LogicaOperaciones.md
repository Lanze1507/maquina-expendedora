# Parte: Lógica de operaciones de la máquina

Clase principal: `vending.logica.MaquinaExpendedora`

## Qué hace
| Instrucción | Método | Validaciones |
|---|---|---|
| `MONEDA(v);` | `insertarMoneda(v, linea)` | Solo Q1, Q5, Q10. Acumula el saldo. |
| `SELECCIONAR(c);` | `seleccionar(c, linea)` | Producto inexistente, sin existencias, sin dinero ingresado. |
| `COMPRAR();` | `comprar(linea)` | Sin selección, sin existencias, dinero insuficiente, compra confirmada dos veces. Descuenta 1 unidad y calcula el cambio. |
| `CANCELAR();` | `cancelar(linea)` | Devuelve el saldo y limpia la selección. |
| `MOSTRAR();` | `mostrar(linea)` | Devuelve la tabla del inventario. |

Cada método devuelve un `Resultado` con:
- `getTexto()` → mensajes `[OK] ...` / `[ERROR SEMANTICO] Linea n: ...`
- `getRecorrido()` / `getRecorridoTexto()` → estados recorridos (ej. `E2 -> E3 -> E4 -> E5 -> E0`)
- `getProductoEntregado()`, `getCambio()`, `getDineroDevuelto()`, `getTablaInventario()`

## Cómo se conecta con cada compañero (integrado)

`vending.logica.ControladorMaquina` implementa `VendingController` de la GUI y une todas las partes:

| Acción en la GUI | Qué llama |
|---|---|
| Q1 / Q5 / Q10, Seleccionar producto, COMPRAR, CANCELAR | `MaquinaExpendedora` (esta parte) |
| Analizar | `AnalizadorCompleto` (léxico, sintáctico y semántico) y prepara las instrucciones |
| Ejecutar siguiente | `PRODUCTO`/`CARGAR` → `expendedora.datos.Inventario`; el resto → `maquina.ejecutar(...)` |

**Productos, inventario y JSON (`expendedora.datos`):** la lógica usa `Inventario.buscar(codigo)`,
`listar()` y `descontarUnidad(codigo)` (solo después de autorizar la compra), y de `Producto`:
`getCodigo()`, `getNombre()`, `getPrecio()` (BigDecimal), `getCantidad()`, `tieneExistencias()`.
Para el JSON del estado: `getSaldo()`, `getCodigoSeleccionado()`, `getEstadoActual()`; al importar,
`restaurarOperacion(saldo, codigo)`.

**Analizador:** cada instrucción de operación se ejecuta con
```java
Resultado r = maquina.ejecutar("MONEDA", Arrays.asList("5"), numeroLinea);
```

**Autómata finito:** la máquina usa la interfaz `vending.automata.ControlEstados`
(`getEstado()`, `puedeTransitar(Entrada)`, `transitar(Entrada)`, `reiniciar()`).
Por defecto usa `AutomataFinito` (parte de Jesser Cetino, ver `docs/AutomataFinito.md`). Se puede inyectar otro:
```java
new MaquinaExpendedora(inventario, automataDelCompanero);
```

## Cómo probar
- GUI completa: ejecutar `vending.logica.MainConLogica`.
- Consola: `vending.logica.PruebaLogica` (3 ejemplos del enunciado + casos extra).
