package vending.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Representación visual de la máquina expendedora.
 * No contiene lógica del autómata.
 */
public class MachinePanel extends JPanel {

    private String selectedProduct = "-";
    private String deliveredProduct = "-";

    public MachinePanel() {
        setPreferredSize(new Dimension(300, 430));
        setBackground(new Color(245, 247, 250));
    }

    public void setSelectedProduct(String selectedProduct) {
        this.selectedProduct = selectedProduct;
        repaint();
    }

    public void setDeliveredProduct(String deliveredProduct) {
        this.deliveredProduct = deliveredProduct;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int w = getWidth();
        int h = getHeight();

        // Cuerpo
        g2.setColor(new Color(45, 50, 58));
        g2.fillRoundRect(25, 18, w - 50, h - 36, 28, 28);

        // Pantalla
        g2.setColor(new Color(25, 30, 35));
        g2.fillRoundRect(50, 45, w - 100, 62, 12, 12);

        g2.setColor(new Color(110, 220, 170));
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.drawString("VENDING MACHINE", 75, 70);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(
                "SEL: " + selectedProduct,
                75,
                91
        );

        // Ventana de productos
        g2.setColor(new Color(225, 230, 236));
        g2.fillRoundRect(50, 125, w - 100, 180, 12, 12);

        int startX = 72;
        int startY = 150;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int x = startX + col * 90;
                int y = startY + row * 50;

                g2.setColor(new Color(255, 255, 255));
                g2.fillRoundRect(x, y, 70, 36, 8, 8);

                g2.setColor(new Color(90, 100, 110));
                g2.drawRoundRect(x, y, 70, 36, 8, 8);

                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.drawString("●", x + 9, y + 23);
                g2.drawString("ITEM", x + 25, y + 23);
            }
        }

        // Zona de entrega
        g2.setColor(new Color(30, 35, 40));
        g2.fillRoundRect(50, 330, w - 100, 55, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("ENTREGA", 65, 350);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(
                deliveredProduct == null ? "-" : deliveredProduct,
                65,
                370
        );

        g2.dispose();
    }
}
