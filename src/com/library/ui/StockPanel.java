package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StockPanel extends JPanel {
    private final JButton btnRefresh = new JButton("Refresh Stock");
    private final JButton btnCheckAlerts = new JButton("Check Low Stock");
    private final JLabel lblAlert = new JLabel(" ");

    private final DefaultTableModel tableModel;
    private final JTable table;

    public StockPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Stock Overview"));

        lblAlert.setForeground(new Color(180, 30, 30));
        lblAlert.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAlert.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleSubtleButton(btnRefresh);
        ModuleTheme.stylePrimaryButton(btnCheckAlerts);
        actions.add(btnRefresh);
        actions.add(btnCheckAlerts);

        top.add(lblAlert, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        String[] cols = {"Book Title", "Author", "Edition", "Publication", "Quantity"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnCheckAlerts() { return btnCheckAlerts; }
    public JLabel getLblAlert() { return lblAlert; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
}
