package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class StockPanel extends JPanel {
    private final JButton btnRefresh = new JButton("Refresh Stock");
    private final JButton btnCheckAlerts = new JButton("Check Low Stock");
    private final JLabel lblAlert = new JLabel(" ");
    private final JTextField txtSearch = new JTextField(20);

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public StockPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Stock Overview"));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchBar.setBackground(ModuleTheme.WHITE);
        JLabel lblSearch = new JLabel("Search Stock:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModuleTheme.styleInput(txtSearch);
        searchBar.add(lblSearch);
        searchBar.add(txtSearch);

        lblAlert.setForeground(new Color(180, 30, 30));
        lblAlert.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAlert.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleSubtleButton(btnRefresh);
        ModuleTheme.stylePrimaryButton(btnCheckAlerts);
        actions.add(btnRefresh);
        actions.add(btnCheckAlerts);

        top.add(searchBar, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ModuleTheme.WHITE);
        centerPanel.add(lblAlert, BorderLayout.NORTH);

        String[] cols = {"Book Title", "Author", "Edition", "Publication", "Quantity"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnCheckAlerts() { return btnCheckAlerts; }
    public JLabel getLblAlert() { return lblAlert; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }
}
