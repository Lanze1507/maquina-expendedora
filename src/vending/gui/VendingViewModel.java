package vending.gui;

import java.util.List;

/**
 * Estado que el backend entrega a la GUI.
 * La GUI solamente representa estos datos.
 */
public record VendingViewModel(
        List<ProductView> products,
        double balance,
        String stateCode,
        String stateDescription,
        String selectedProduct,
        String deliveredProduct,
        double change,
        String message
) {
    public static VendingViewModel empty() {
        return new VendingViewModel(
                List.of(),
                0,
                "E0",
                "Esperando dinero",
                "-",
                "-",
                0,
                "Sistema listo."
        );
    }
}
