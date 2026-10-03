package vending.gui;

import javax.swing.*;
import java.util.List;

/**
 * Punto de entrada temporal.
 *
 * DemoController permite probar la GUI antes de integrar el backend real.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VendingController controller = new DemoController();
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }

    private static class DemoController implements VendingController {

        private VendingViewModel model = new VendingViewModel(
                List.of(
                        new ProductView("A1", "AGUA", 5, 10),
                        new ProductView("A2", "GASEOSA", 8, 5),
                        new ProductView("A3", "GALLETA", 3, 15)
                ),
                0,
                "E0",
                "Esperando dinero",
                "-",
                "-",
                0,
                "[OK] Sistema listo."
        );

        @Override
        public void insertMoney(int amount) {
            model = new VendingViewModel(
                    model.products(),
                    model.balance() + amount,
                    "E1",
                    "Dinero ingresado",
                    model.selectedProduct(),
                    "-",
                    0,
                    "[OK] Moneda recibida: Q" + amount
            );
        }

        @Override
        public void selectProduct(String code) {
            ProductView product = model.products().stream()
                    .filter(p -> p.code().equals(code))
                    .findFirst()
                    .orElse(null);

            if (product == null) {
                model = new VendingViewModel(
                        model.products(),
                        model.balance(),
                        "E1",
                        "Dinero ingresado",
                        "-",
                        "-",
                        0,
                        "[ERROR] Producto inexistente: " + code
                );
                return;
            }

            if (product.stock() <= 0) {
                model = new VendingViewModel(
                        model.products(),
                        model.balance(),
                        "E1",
                        "Dinero ingresado",
                        "-",
                        "-",
                        0,
                        "[ERROR] El producto " + code + " no tiene existencias."
                );
                return;
            }

            model = new VendingViewModel(
                    model.products(),
                    model.balance(),
                    "E2",
                    "Producto seleccionado",
                    product.name(),
                    "-",
                    0,
                    "[OK] Producto seleccionado: " + code
            );
        }

        @Override
        public void buy() {
            if (model.selectedProduct().equals("-")) {
                model = new VendingViewModel(
                        model.products(),
                        model.balance(),
                        "E1",
                        "Dinero ingresado",
                        "-",
                        "-",
                        0,
                        "[ERROR] No existe un producto seleccionado."
                );
                return;
            }

            double price = model.products().stream()
                    .filter(p -> p.name().equals(model.selectedProduct()))
                    .mapToDouble(ProductView::price)
                    .findFirst()
                    .orElse(8);

            if (model.balance() < price) {
                model = new VendingViewModel(
                        model.products(),
                        model.balance(),
                        "E2",
                        "Producto seleccionado",
                        model.selectedProduct(),
                        "-",
                        0,
                        String.format(
                                "[ERROR] Dinero insuficiente. Falta Q%.2f",
                                price - model.balance()
                        )
                );
                return;
            }

            double change = model.balance() - price;

            List<ProductView> updated = model.products().stream()
                    .map(p -> p.name().equals(model.selectedProduct())
                            ? new ProductView(
                                    p.code(), p.name(), p.price(), p.stock() - 1)
                            : p)
                    .toList();

            model = new VendingViewModel(
                    updated,
                    0,
                    "E0",
                    "Esperando dinero",
                    "-",
                    model.selectedProduct(),
                    change,
                    String.format(
                            "[OK] Producto despachado: %s | Cambio: Q%.2f",
                            model.selectedProduct(), change
                    )
            );
        }

        @Override
        public void cancel() {
            double refund = model.balance();

            model = new VendingViewModel(
                    model.products(),
                    0,
                    "E0",
                    "Esperando dinero",
                    "-",
                    "-",
                    refund,
                    String.format("[OK] Operación cancelada. Dinero devuelto: Q%.2f", refund)
            );
        }

        @Override
        public void analyzeSource(String source) {
            model = new VendingViewModel(
                    model.products(),
                    model.balance(),
                    model.stateCode(),
                    model.stateDescription(),
                    model.selectedProduct(),
                    model.deliveredProduct(),
                    model.change(),
                    "[GUI] Código enviado al analizador."
            );
        }

        @Override
        public void executeNextInstruction() {
            model = new VendingViewModel(
                    model.products(),
                    model.balance(),
                    model.stateCode(),
                    model.stateDescription(),
                    model.selectedProduct(),
                    model.deliveredProduct(),
                    model.change(),
                    "[GUI] Solicitud de ejecución enviada al backend."
            );
        }

        @Override
        public VendingViewModel getViewModel() {
            return model;
        }
    }
}
