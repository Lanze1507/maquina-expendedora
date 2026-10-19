package vending.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainFrame extends JFrame {

    private static final Color INK = new Color(32, 36, 46);
    private static final Color MUTED = new Color(101, 109, 128);
    private static final Color PANEL = new Color(255, 255, 255, 232);
    private static final Color TEAL = new Color(18, 176, 151);
    private static final Color CORAL = new Color(238, 91, 91);
    private static final Color BLUE = new Color(67, 103, 225);
    private static final Color AMBER = new Color(245, 177, 66);

    private final VendingController controller;

    private final DefaultTableModel productTableModel =
            new DefaultTableModel(
                    new Object[]{"Codigo", "Nombre", "Precio", "Existencias"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable productTable = new JTable(productTableModel);

    private final JLabel balanceLabel = new JLabel("Q0.00");
    private final JLabel stateLabel = new JLabel("E0 - Esperando dinero");
    private final JLabel selectedLabel = new JLabel("-");
    private final JLabel deliveredLabel = new JLabel("-");
    private final JLabel changeLabel = new JLabel("Q0.00");
    private final JLabel messageLabel = new JLabel("Sistema listo.");

    private final JLabel stateBadge = new JLabel("E0");
    private final MachinePanel machinePanel = new MachinePanel();
    private final DeliveryTrayPanel deliveryTray = new DeliveryTrayPanel();

    private final JTextArea editor = new JTextArea();
    private final JTextArea results = new JTextArea();
    private float pulse;

    public MainFrame(VendingController controller) {
        this.controller = controller;

        setTitle("Simulador de Maquina Expendedora");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1250, 820));
        setSize(1250, 820);
        setLocationRelativeTo(null);

        configureLookAndFeel();
        buildInterface();
        startAnimations();
        refreshFromController();
    }

    private void configureLookAndFeel() {
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.focus", TEAL);
        UIManager.put("ScrollBar.width", 12);
    }

    private void buildInterface() {
        AnimatedBackgroundPanel root = new AnimatedBackgroundPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildEditorArea(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new HeroPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("MAQUINA EXPENDEDORA");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel subtitle = new JLabel("Simulador visual de automatas y lenguaje .vm");
        subtitle.setForeground(new Color(219, 232, 255));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(subtitle);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        status.setOpaque(false);
        JLabel dot = new GlowDot();
        JLabel online = new JLabel("ONLINE");
        online.setForeground(Color.WHITE);
        online.setFont(new Font("SansSerif", Font.BOLD, 12));
        status.add(dot);
        status.add(online);

        header.add(text, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout(14, 14));
        area.setOpaque(false);

        area.add(buildProductsPanel(), BorderLayout.CENTER);
        area.add(buildMachineCenter(), BorderLayout.EAST);

        return area;
    }

    private JPanel buildProductsPanel() {
        JPanel panel = cardPanel("Inventario");

        styleProductTable();
        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JButton select = new GlowButton("Seleccionar producto", BLUE, TEAL);
        select.addActionListener(e -> selectSelectedProduct());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 2, 2, 2));
        bottom.add(select, BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void styleProductTable() {
        productTable.setRowHeight(38);
        productTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        productTable.setForeground(INK);
        productTable.setGridColor(new Color(232, 236, 245));
        productTable.setShowVerticalLines(false);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setSelectionBackground(new Color(220, 248, 243));
        productTable.setSelectionForeground(INK);
        productTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = productTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(10, 40));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setForeground(new Color(64, 72, 92));
        header.setBackground(new Color(244, 247, 252));

        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                cell.setBorder(new EmptyBorder(0, 14, 0, 14));
                cell.setFont(new Font("SansSerif", column == 0 ? Font.BOLD : Font.PLAIN, 13));
                if (!isSelected) {
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                    cell.setForeground(column == 0 ? BLUE : INK);
                }
                return cell;
            }
        });
    }

    private JPanel buildMachineCenter() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(500, 500));

        JPanel machineCard = cardPanel("Maquina");
        machineCard.add(machinePanel, BorderLayout.CENTER);

        panel.add(machineCard, BorderLayout.CENTER);
        panel.add(buildOperationPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildOperationPanel() {
        JPanel panel = cardPanel("Operaciones");

        JPanel coins = new JPanel(new GridLayout(1, 3, 10, 10));
        coins.setOpaque(false);

        JButton q1 = createCoinButton("Q1");
        JButton q5 = createCoinButton("Q5");
        JButton q10 = createCoinButton("Q10");

        q1.addActionListener(e -> insertMoney(1));
        q5.addActionListener(e -> insertMoney(5));
        q10.addActionListener(e -> insertMoney(10));

        coins.add(q1);
        coins.add(q5);
        coins.add(q10);

        panel.add(coins, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        status.setOpaque(false);
        stateBadge.setOpaque(false);
        stateBadge.setForeground(Color.WHITE);
        stateBadge.setFont(new Font("SansSerif", Font.BOLD, 18));
        stateBadge.setHorizontalAlignment(SwingConstants.CENTER);
        stateBadge.setPreferredSize(new Dimension(58, 42));
        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        stateLabel.setForeground(INK);
        status.add(new BadgeShell(stateBadge));
        status.add(stateLabel);

        JPanel balance = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        balance.setOpaque(false);

        JLabel saldo = new JLabel("SALDO");
        saldo.setFont(new Font("SansSerif", Font.BOLD, 12));
        saldo.setForeground(MUTED);

        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 29));
        balanceLabel.setForeground(TEAL);

        balance.add(saldo);
        balance.add(balanceLabel);

        center.add(status);
        center.add(Box.createVerticalStrut(6));
        center.add(deliveryTray);
        center.add(Box.createVerticalStrut(6));
        center.add(balance);

        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 10));
        actions.setOpaque(false);

        JButton buy = new GlowButton("COMPRAR", TEAL, BLUE);
        JButton cancel = new GlowButton("CANCELAR", CORAL, AMBER);

        buy.addActionListener(e -> {
            controller.buy();
            refreshFromController();
        });

        cancel.addActionListener(e -> {
            controller.cancel();
            refreshFromController();
        });

        actions.add(buy);
        actions.add(cancel);

        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createCoinButton(String text) {
        return new GlowButton(text, AMBER, TEAL);
    }

    private JPanel buildEditorArea() {
        JPanel outer = cardPanel("Editor de instrucciones .vm");
        outer.setPreferredSize(new Dimension(100, 292));

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editor.setTabSize(4);
        editor.setForeground(new Color(38, 43, 58));
        editor.setCaretColor(BLUE);
        editor.setSelectionColor(new Color(198, 238, 232));
        editor.setBorder(new EmptyBorder(10, 12, 10, 12));
        editor.setText(
                "PRODUCTO(A1,AGUA,5,10);\n" +
                "PRODUCTO(A2,GASEOSA,8,5);\n" +
                "PRODUCTO(A3,GALLETA,3,15);\n" +
                "MONEDA(5);\n" +
                "MONEDA(5);\n" +
                "SELECCIONAR(A2);\n" +
                "COMPRAR();\n"
        );

        results.setEditable(false);
        results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        results.setForeground(new Color(38, 43, 58));
        results.setBackground(new Color(250, 252, 255));
        results.setBorder(new EmptyBorder(10, 12, 10, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabs.addTab("Codigo .vm", wrap(editor));
        tabs.addTab("Resultados / analisis", wrap(results));

        outer.add(tabs, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttons.setOpaque(false);

        JButton nuevo = smallButton("Nuevo");
        JButton abrir = smallButton("Abrir .vm");
        JButton guardar = smallButton("Guardar");
        JButton analizar = smallButton("Analizar");
        JButton ejecutar = smallButton("Ejecutar siguiente");

        nuevo.addActionListener(e -> editor.setText(""));

        abrir.addActionListener(e -> openVmFile());
        guardar.addActionListener(e -> saveVmFile());

        analizar.addActionListener(e -> {
            controller.analyzeSource(editor.getText());

            VendingViewModel vm = controller.getViewModel();

            results.setText(vm.message());
            tabs.setSelectedIndex(1);
        });

        ejecutar.addActionListener(e -> {
            controller.executeNextInstruction();
            refreshFromController();
        });

        buttons.add(nuevo);
        buttons.add(abrir);
        buttons.add(guardar);
        buttons.add(analizar);
        buttons.add(ejecutar);

        outer.add(buttons, BorderLayout.SOUTH);

        return outer;
    }

    private JScrollPane wrap(Component component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JButton smallButton(String text) {
        JButton button = new GlowButton(text, new Color(92, 108, 138), BLUE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(Math.max(92, text.length() * 10), 34));
        return button;
    }

    private JPanel buildStatePanel() {
        JPanel panel = cardPanel("Estado actual");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setOpaque(false);

        stateBadge.setOpaque(false);
        stateBadge.setForeground(Color.WHITE);
        stateBadge.setFont(new Font("SansSerif", Font.BOLD, 20));
        stateBadge.setHorizontalAlignment(SwingConstants.CENTER);
        stateBadge.setPreferredSize(new Dimension(58, 42));

        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        stateLabel.setForeground(INK);

        top.add(new BadgeShell(stateBadge));
        top.add(stateLabel);

        panel.add(top, BorderLayout.NORTH);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        details.add(detail("Producto seleccionado", selectedLabel));
        details.add(detail("Producto entregado", deliveredLabel));
        details.add(detail("Cambio", changeLabel));

        details.add(Box.createVerticalStrut(10));

        messageLabel.setOpaque(true);
        messageLabel.setBackground(new Color(247, 250, 255));
        messageLabel.setForeground(INK);
        messageLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 244)),
                        new EmptyBorder(9, 10, 9, 10)
                )
        );
        details.add(messageLabel);

        details.add(Box.createVerticalStrut(12));

        JTextArea states = new JTextArea(
                "E0  Esperando dinero\n" +
                "E1  Dinero ingresado\n" +
                "E2  Producto seleccionado\n" +
                "E3  Procesando compra\n" +
                "E4  Entregando producto\n" +
                "E5  Devolviendo dinero"
        );
        states.setEditable(false);
        states.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        states.setForeground(new Color(61, 69, 88));
        states.setBackground(new Color(248, 250, 253));
        states.setBorder(new EmptyBorder(10, 12, 10, 12));

        details.add(states);

        panel.add(details, BorderLayout.CENTER);

        return panel;
    }

    private JPanel detail(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 3, 6, 3));

        JLabel label = new JLabel(title + ":");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(MUTED);

        value.setFont(new Font("SansSerif", Font.BOLD, 13));
        value.setForeground(INK);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    private JPanel cardPanel(String title) {
        JPanel panel = new GlassCard(title);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(44, 14, 14, 14));
        return panel;
    }

    private void insertMoney(int amount) {
        controller.insertMoney(amount);
        balanceLabel.setForeground(amount >= 10 ? BLUE : TEAL);
        refreshFromController();
    }

    private void selectSelectedProduct() {
        int row = productTable.getSelectedRow();

        if (row == -1) {
            appendResult("[ERROR] Seleccione un producto.\n");
            return;
        }

        String code = String.valueOf(productTableModel.getValueAt(row, 0));

        controller.selectProduct(code);
        refreshFromController();
    }

    private void openVmFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir archivo .vm");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();

            try {
                editor.setText(Files.readString(path));
                appendResult("[OK] Archivo cargado: " + path.getFileName() + "\n");
            } catch (IOException ex) {
                showError("No se pudo abrir el archivo.", ex);
            }
        }
    }

    private void saveVmFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar archivo .vm");

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();

            if (!path.toString().toLowerCase().endsWith(".vm")) {
                path = Path.of(path + ".vm");
            }

            try {
                Files.writeString(path, editor.getText());
                appendResult("[OK] Archivo guardado: " + path.getFileName() + "\n");
            } catch (IOException ex) {
                showError("No se pudo guardar el archivo.", ex);
            }
        }
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void appendResult(String text) {
        results.append(text);
    }

    private void refreshFromController() {
        VendingViewModel vm = controller.getViewModel();

        productTableModel.setRowCount(0);

        for (ProductView p : vm.products()) {
            productTableModel.addRow(new Object[]{
                    p.code(),
                    p.name(),
                    String.format("Q%.2f", p.price()),
                    p.stock()
            });
        }

        balanceLabel.setText(String.format("Q%.2f", vm.balance()));

        stateBadge.setText(vm.stateCode());
        stateLabel.setText(vm.stateCode() + " - " + vm.stateDescription());

        selectedLabel.setText(vm.selectedProduct());
        deliveredLabel.setText(vm.deliveredProduct());
        changeLabel.setText(String.format("Q%.2f", vm.change()));

        messageLabel.setText("<html>" + vm.message() + "</html>");

        machinePanel.setSelectedProduct(vm.selectedProduct());
        deliveryTray.setDelivery(vm.deliveredProduct(), vm.change());

        if (vm.message() != null && !vm.message().isBlank()) {
            appendResult(vm.message() + "\n");
        }
    }

    private void startAnimations() {
        Timer timer = new Timer(40, e -> {
            pulse += 0.045f;
            stateBadge.repaint();
            deliveryTray.repaint();
            getContentPane().repaint();
        });
        timer.start();
    }

    private class AnimatedBackgroundPanel extends JPanel {
        AnimatedBackgroundPanel(LayoutManager layout) {
            super(layout);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            float shift = (float) ((Math.sin(pulse * 0.35) + 1) / 2);
            Color top = blend(new Color(240, 247, 255), new Color(235, 250, 244), shift);
            Color bottom = blend(new Color(246, 241, 255), new Color(255, 248, 235), 1f - shift);
            g2.setPaint(new GradientPaint(0, 0, top, w, h, bottom));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 74));
            for (int i = -120; i < w + h; i += 54) {
                int offset = (int) (Math.sin(pulse + i * 0.01) * 10);
                g2.drawLine(i + offset, 0, i - h + offset, h);
            }

            drawSoftCircle(g2, w - 180, 70, 170, new Color(67, 103, 225, 38));
            drawSoftCircle(g2, 120, h - 110, 210, new Color(18, 176, 151, 34));
            drawSoftCircle(g2, w / 2, h - 40, 150, new Color(245, 177, 66, 32));
            g2.dispose();
        }
    }

    private static void drawSoftCircle(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.fillOval(x - size / 2, y - size / 2, size, size);
    }

    private static Color blend(Color a, Color b, float ratio) {
        float keep = 1f - ratio;
        return new Color(
                (int) (a.getRed() * keep + b.getRed() * ratio),
                (int) (a.getGreen() * keep + b.getGreen() * ratio),
                (int) (a.getBlue() * keep + b.getBlue() * ratio)
        );
    }

    private static class HeroPanel extends JPanel {
        HeroPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(31, 38, 69), getWidth(), 0, new Color(30, 170, 151)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.setColor(new Color(255, 255, 255, 34));
            g2.fillOval(getWidth() - 150, -80, 230, 180);
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(getWidth() - 320, 26, 120, 120);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GlowDot extends JLabel {
        GlowDot() {
            setPreferredSize(new Dimension(16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(129, 255, 209, 80));
            g2.fillOval(0, 0, 16, 16);
            g2.setColor(new Color(129, 255, 209));
            g2.fillOval(4, 4, 8, 8);
            g2.dispose();
        }
    }

    private static class GlassCard extends JPanel {
        private final String title;

        GlassCard(String title) {
            this.title = title;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(30, 38, 62, 18));
            g2.fillRoundRect(5, 8, w - 10, h - 10, 22, 22);
            g2.setColor(PANEL);
            g2.fillRoundRect(0, 0, w - 4, h - 4, 22, 22);
            g2.setColor(new Color(255, 255, 255, 160));
            g2.drawRoundRect(0, 0, w - 5, h - 5, 22, 22);

            g2.setPaint(new GradientPaint(14, 13, new Color(67, 103, 225), 170, 13, new Color(18, 176, 151)));
            g2.fillRoundRect(14, 12, Math.max(118, title.length() * 10 + 34), 26, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(title.toUpperCase(), 29, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class BadgeShell extends JPanel {
        BadgeShell(JLabel label) {
            super(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(58, 42));
            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float glow = (float) ((Math.sin(pulse * 1.8) + 1) / 2);
            g2.setColor(new Color(67, 103, 225, 40 + (int) (70 * glow)));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 18, 18);
            g2.setPaint(new GradientPaint(0, 0, BLUE, getWidth(), getHeight(), TEAL));
            g2.fillRoundRect(6, 5, getWidth() - 12, getHeight() - 10, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class DeliveryTrayPanel extends JPanel {
        private String product = "-";
        private double change;

        DeliveryTrayPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(320, 58));
            setMinimumSize(new Dimension(240, 58));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            setBorder(new EmptyBorder(8, 18, 8, 18));
        }

        void setDelivery(String product, double change) {
            this.product = product == null || product.isBlank() ? "-" : product;
            this.change = change;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            float glow = (float) ((Math.sin(pulse * 1.4) + 1) / 2);

            g2.setColor(new Color(18, 22, 35, 34 + (int) (22 * glow)));
            g2.fillRoundRect(5, 8, w - 10, h - 12, 20, 20);
            g2.setPaint(new GradientPaint(0, 0, new Color(13, 21, 36), w, h, new Color(22, 42, 64)));
            g2.fillRoundRect(0, 2, w - 4, h - 8, 20, 20);
            g2.setColor(new Color(18, 220, 181, 80 + (int) (85 * glow)));
            g2.drawRoundRect(1, 3, w - 7, h - 11, 18, 18);

            int iconX = 18;
            int iconY = 17;
            g2.setPaint(new GradientPaint(iconX, iconY, AMBER, iconX + 28, iconY + 24, TEAL));
            g2.fillRoundRect(iconX, iconY, 34, 24, 13, 13);
            g2.setColor(new Color(255, 255, 255, 155));
            g2.fillOval(iconX + 8, iconY + 6, 8, 6);

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(163, 255, 230));
            g2.drawString("ENTREGA", 66, 24);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            g2.drawString(product, 66, 42);

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(226, 235, 255));
            String changeText = String.format("Cambio Q%.2f", change);
            int textWidth = g2.getFontMetrics().stringWidth(changeText);
            g2.drawString(changeText, Math.max(72, w - textWidth - 22), 35);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GlowButton extends JButton {
        private final Color start;
        private final Color end;
        private float hover;
        private Timer hoverTimer;

        GlowButton(String text, Color start, Color end) {
            super(text);
            this.start = start;
            this.end = end;
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 42));
            setBorder(new EmptyBorder(8, 16, 8, 16));

            hoverTimer = new Timer(16, e -> {
                float target = getModel().isRollover() ? 1f : 0f;
                hover += (target - hover) * 0.25f;
                if (Math.abs(target - hover) < 0.01f) {
                    hover = target;
                    hoverTimer.stop();
                }
                repaint();
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hoverTimer.start();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverTimer.start();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 18;
            int y = getModel().isPressed() ? 3 : 1;
            Color a = brighten(start, hover * 0.14f);
            Color b = brighten(end, hover * 0.14f);

            g2.setColor(new Color(start.getRed(), start.getGreen(), start.getBlue(), 44 + (int) (70 * hover)));
            g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 6, arc, arc);
            g2.setPaint(new GradientPaint(0, y, a, getWidth(), getHeight(), b));
            g2.fillRoundRect(0, y, getWidth(), getHeight() - 5, arc, arc);
            g2.setColor(new Color(255, 255, 255, 70));
            g2.drawRoundRect(1, y + 1, getWidth() - 3, getHeight() - 8, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color brighten(Color color, float amount) {
            return new Color(
                    Math.min(255, (int) (color.getRed() + 255 * amount)),
                    Math.min(255, (int) (color.getGreen() + 255 * amount)),
                    Math.min(255, (int) (color.getBlue() + 255 * amount))
            );
        }
    }
}
