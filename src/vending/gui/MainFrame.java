package vending.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainFrame extends JFrame {

    private final VendingController controller;

    private final DefaultTableModel productTableModel =
            new DefaultTableModel(
                    new Object[]{"Código", "Nombre", "Precio", "Existencias"}, 0
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

    private final JTextArea editor = new JTextArea();
    private final JTextArea results = new JTextArea();

    public MainFrame(VendingController controller) {
        this.controller = controller;

        setTitle("Simulador de Máquina Expendedora");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1250, 820));
        setSize(1250, 820);
        setLocationRelativeTo(null);

        buildInterface();
        refreshFromController();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(238, 241, 245));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildEditorArea(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(35, 40, 48));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("MÁQUINA EXPENDEDORA");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Simulador — Autómatas y Lenguajes Formales");
        subtitle.setForeground(new Color(195, 202, 212));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);

        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout(12, 12));
        area.setOpaque(false);

        area.add(buildProductsPanel(), BorderLayout.CENTER);
        area.add(buildMachineCenter(), BorderLayout.EAST);

        return area;
    }

    private JPanel buildProductsPanel() {
        JPanel panel = cardPanel("INVENTARIO");

        productTable.setRowHeight(32);
        productTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        productTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JButton select = new JButton("Seleccionar producto");
        select.addActionListener(e -> selectSelectedProduct());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));
        bottom.add(select, BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMachineCenter() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(480, 500));

        JPanel machineCard = cardPanel("MÁQUINA");
        machineCard.add(machinePanel, BorderLayout.CENTER);

        panel.add(machineCard, BorderLayout.CENTER);
        panel.add(buildOperationPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildOperationPanel() {
        JPanel panel = cardPanel("OPERACIONES");

        JPanel coins = new JPanel(new GridLayout(1, 3, 8, 8));
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

        JPanel balance = new JPanel(new FlowLayout(FlowLayout.CENTER));
        balance.setOpaque(false);

        JLabel saldo = new JLabel("SALDO");
        saldo.setFont(new Font("SansSerif", Font.BOLD, 12));

        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        balanceLabel.setForeground(new Color(35, 110, 75));

        balance.add(saldo);
        balance.add(balanceLabel);

        panel.add(balance, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 8));
        actions.setOpaque(false);

        JButton buy = new JButton("COMPRAR");
        JButton cancel = new JButton("CANCELAR");

        buy.setFont(new Font("SansSerif", Font.BOLD, 13));
        cancel.setFont(new Font("SansSerif", Font.BOLD, 13));

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
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel buildEditorArea() {
        JPanel outer = cardPanel("EDITOR DE INSTRUCCIONES .VM");
        outer.setPreferredSize(new Dimension(100, 285));

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editor.setTabSize(4);
        editor.setText(
                "PRODUCTO(A1,AGUA,5,10);\\n" +
                "PRODUCTO(A2,GASEOSA,8,5);\\n" +
                "PRODUCTO(A3,GALLETA,3,15);\\n" +
                "MONEDA(5);\\n" +
                "MONEDA(5);\\n" +
                "SELECCIONAR(A2);\\n" +
                "COMPRAR();\\n"
        );

        results.setEditable(false);
        results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Código .vm", new JScrollPane(editor));
        tabs.addTab("Resultados / análisis", new JScrollPane(results));

        outer.add(tabs, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        buttons.setOpaque(false);

        JButton nuevo = new JButton("Nuevo");
        JButton abrir = new JButton("Abrir .vm");
        JButton guardar = new JButton("Guardar");
        JButton analizar = new JButton("Analizar");
        JButton ejecutar = new JButton("Ejecutar siguiente");

        nuevo.addActionListener(e -> editor.setText(""));

        abrir.addActionListener(e -> openVmFile());
        guardar.addActionListener(e -> saveVmFile());

        analizar.addActionListener(e -> {
            controller.analyzeSource(editor.getText());
            refreshFromController();
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

    private JPanel buildStatePanel() {
        JPanel panel = cardPanel("ESTADO ACTUAL");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setOpaque(false);

        stateBadge.setOpaque(true);
        stateBadge.setBackground(new Color(70, 125, 200));
        stateBadge.setForeground(Color.WHITE);
        stateBadge.setFont(new Font("SansSerif", Font.BOLD, 20));
        stateBadge.setHorizontalAlignment(SwingConstants.CENTER);
        stateBadge.setPreferredSize(new Dimension(55, 42));

        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        top.add(stateBadge);
        top.add(stateLabel);

        panel.add(top, BorderLayout.NORTH);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        details.add(detail("Producto seleccionado", selectedLabel));
        details.add(detail("Producto entregado", deliveredLabel));
        details.add(detail("Cambio", changeLabel));

        details.add(Box.createVerticalStrut(10));

        messageLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 225, 230)
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );
        details.add(messageLabel);

        details.add(Box.createVerticalStrut(12));

        JTextArea states = new JTextArea(
                "E0  Esperando dinero\\n" +
                "E1  Dinero ingresado\\n" +
                "E2  Producto seleccionado\\n" +
                "E3  Procesando compra\\n" +
                "E4  Entregando producto\\n" +
                "E5  Devolviendo dinero"
        );
        states.setEditable(false);
        states.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        states.setBackground(new Color(248, 249, 251));
        states.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Estados"),
                        new EmptyBorder(4, 4, 4, 4)
                )
        );

        details.add(states);

        panel.add(details, BorderLayout.CENTER);

        return panel;
    }

    private JPanel detail(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 3, 5, 3));

        JLabel label = new JLabel(title + ":");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));

        value.setFont(new Font("SansSerif", Font.PLAIN, 13));

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    private JPanel cardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(218, 222, 228)
                        ),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(title),
                                new EmptyBorder(5, 5, 5, 5)
                        )
                )
        );
        return panel;
    }

    private void insertMoney(int amount) {
        controller.insertMoney(amount);
        refreshFromController();
    }

    private void selectSelectedProduct() {
        int row = productTable.getSelectedRow();

        if (row == -1) {
            appendResult("[ERROR] Seleccione un producto.\\n");
            return;
        }

        String code = String.valueOf(
                productTableModel.getValueAt(row, 0)
        );

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
                appendResult(
                        "[OK] Archivo cargado: " +
                        path.getFileName() + "\\n"
                );
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
                appendResult(
                        "[OK] Archivo guardado: " +
                        path.getFileName() + "\\n"
                );
            } catch (IOException ex) {
                showError("No se pudo guardar el archivo.", ex);
            }
        }
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                message + "\\n" + ex.getMessage(),
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

        messageLabel.setText(
                "<html>" + vm.message() + "</html>"
        );

        machinePanel.setSelectedProduct(vm.selectedProduct());
        machinePanel.setDeliveredProduct(vm.deliveredProduct());

        if (vm.message() != null && !vm.message().isBlank()) {
            appendResult(vm.message() + "\n");
        }
    }
}
