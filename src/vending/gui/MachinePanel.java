package vending.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Representacion visual de la maquina expendedora.
 * No contiene logica del automata.
 */
public class MachinePanel extends JPanel {

    private static final Color BODY_TOP = new Color(42, 49, 78);
    private static final Color BODY_BOTTOM = new Color(22, 27, 45);
    private static final Color GLASS = new Color(225, 244, 255, 145);
    private static final Color TEAL = new Color(18, 220, 181);
    private static final Color PINK = new Color(241, 91, 135);
    private static final Color AMBER = new Color(247, 186, 75);
    private static final Color BLUE = new Color(87, 126, 255);

    private String selectedProduct = "-";
    private float tick;

    public MachinePanel() {
        setPreferredSize(new Dimension(320, 430));
        setOpaque(false);

        Timer timer = new Timer(33, e -> {
            tick += 0.045f;
            repaint();
        });
        timer.start();
    }

    public void setSelectedProduct(String selectedProduct) {
        this.selectedProduct = selectedProduct;
        repaint();
    }

    public void setDeliveredProduct(String deliveredProduct) {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int machineX = 30;
        int machineY = 18;
        int machineW = w - 60;
        int machineH = h - 36;

        paintAura(g2, w, h);
        paintBody(g2, machineX, machineY, machineW, machineH);
        paintScreen(g2, machineX, machineY, machineW);
        paintProductWindow(g2, machineX, machineY, machineW, machineH);
        paintControlRail(g2, machineX, machineY, machineW);

        g2.dispose();
    }

    private void paintAura(Graphics2D g2, int w, int h) {
        float shimmer = (float) ((Math.sin(tick) + 1) / 2);
        g2.setColor(new Color(87, 126, 255, 26 + (int) (30 * shimmer)));
        g2.fillOval(w / 2 - 150, 14, 300, 84);
        g2.setColor(new Color(18, 220, 181, 28));
        g2.fillOval(w / 2 - 135, h - 66, 270, 42);
    }

    private void paintBody(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(18, 22, 35, 50));
        g2.fillRoundRect(x + 10, y + 12, w - 4, h - 2, 36, 36);
        g2.setPaint(new GradientPaint(x, y, BODY_TOP, x + w, y + h, BODY_BOTTOM));
        g2.fillRoundRect(x, y, w, h, 34, 34);

        g2.setColor(new Color(255, 255, 255, 36));
        g2.drawRoundRect(x + 2, y + 2, w - 5, h - 5, 32, 32);

        g2.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 44), x + w, y, new Color(255, 255, 255, 0)));
        g2.fillRoundRect(x + 12, y + 12, 52, h - 24, 24, 24);
    }

    private void paintScreen(Graphics2D g2, int x, int y, int w) {
        int screenX = x + 24;
        int screenY = y + 26;
        int screenW = w - 48;
        int screenH = 72;
        float glow = (float) ((Math.sin(tick * 1.7) + 1) / 2);

        g2.setColor(new Color(10, 18, 28));
        g2.fillRoundRect(screenX, screenY, screenW, screenH, 18, 18);
        g2.setColor(new Color(18, 220, 181, 50 + (int) (80 * glow)));
        g2.drawRoundRect(screenX + 2, screenY + 2, screenW - 4, screenH - 4, 16, 16);

        g2.setColor(new Color(141, 255, 221));
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString("VEND-VM  READY", screenX + 18, screenY + 29);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        String selected = selectedProduct == null ? "-" : selectedProduct;
        g2.drawString("SEL: " + selected, screenX + 18, screenY + 53);

        int scanY = screenY + 12 + (int) ((Math.sin(tick * 2.3) + 1) * 24);
        g2.setColor(new Color(141, 255, 221, 48));
        g2.drawLine(screenX + 15, scanY, screenX + screenW - 15, scanY);
    }

    private void paintProductWindow(Graphics2D g2, int x, int y, int w, int h) {
        int glassX = x + 24;
        int glassY = y + 116;
        int glassW = w - 116;
        int glassH = Math.max(176, h - 160);

        g2.setColor(new Color(8, 13, 24));
        g2.fillRoundRect(glassX - 5, glassY - 5, glassW + 10, glassH + 10, 22, 22);
        g2.setColor(GLASS);
        g2.fillRoundRect(glassX, glassY, glassW, glassH, 18, 18);

        g2.setColor(new Color(255, 255, 255, 70));
        for (int i = -40; i < glassW + glassH; i += 32) {
            g2.drawLine(glassX + i, glassY, glassX + i - glassH, glassY + glassH);
        }

        Color[] colors = {TEAL, PINK, AMBER, BLUE, new Color(139, 93, 255), new Color(255, 126, 73)};
        int index = 0;
        int startX = glassX + 18;
        int startY = glassY + 18;
        int rowGap = Math.max(40, (glassH - 38) / 3);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int px = startX + col * 82;
                int py = startY + row * rowGap;
                paintProduct(g2, px, py, colors[index], index);
                index++;
            }
        }
    }

    private void paintProduct(Graphics2D g2, int x, int y, Color color, int index) {
        float bob = (float) Math.sin(tick * 1.4 + index) * 2.0f;
        int yy = y + Math.round(bob);

        g2.setColor(new Color(20, 24, 38, 42));
        g2.fillRoundRect(x + 3, yy + 4, 58, 34, 12, 12);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, yy, 58, 34, 12, 12);

        g2.setPaint(new GradientPaint(x, yy, color.brighter(), x + 58, yy + 34, color.darker()));
        g2.fillRoundRect(x + 7, yy + 7, 44, 20, 10, 10);
        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillOval(x + 14, yy + 10, 9, 7);
    }

    private void paintControlRail(Graphics2D g2, int x, int y, int w) {
        int railX = x + w - 76;
        int railY = y + 118;
        int railW = 48;
        int railH = 174;

        g2.setColor(new Color(12, 16, 28));
        g2.fillRoundRect(railX, railY, railW, railH, 18, 18);
        g2.setColor(new Color(255, 255, 255, 34));
        g2.drawRoundRect(railX + 2, railY + 2, railW - 4, railH - 4, 16, 16);

        int lightY = railY + 22;
        Color[] lights = {TEAL, AMBER, PINK};
        for (int i = 0; i < lights.length; i++) {
            float active = (float) ((Math.sin(tick * 2 + i * 1.8) + 1) / 2);
            g2.setColor(new Color(lights[i].getRed(), lights[i].getGreen(), lights[i].getBlue(), 55 + (int) (80 * active)));
            g2.fillOval(railX + 13, lightY + i * 36, 22, 22);
            g2.setColor(lights[i]);
            g2.fillOval(railX + 17, lightY + 4 + i * 36, 14, 14);
        }

        g2.setColor(new Color(230, 236, 247));
        g2.fillRoundRect(railX + 12, railY + 132, 24, 9, 8, 8);
        g2.setColor(new Color(89, 99, 124));
        g2.fillRoundRect(railX + 16, railY + 146, 16, 11, 7, 7);
    }
}
