package vending.logica;

import javax.swing.SwingUtilities;

import vending.gui.MainFrame;

/**
 * Abre la interfaz Swing conectada a la LÓGICA REAL de la máquina
 * (en lugar del DemoController de vending.gui.Main).
 *
 * Cuando se integren todas las partes, vending.gui.Main puede usar
 * directamente:  new MainFrame(new ControladorMaquina(inventario))
 */
public class MainConLogica {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var inventario =
                    ControladorMaquina.inventarioDePrueba();
            MainFrame frame = new MainFrame(new ControladorMaquina(inventario));
            frame.setVisible(true);
        });
    }
}
