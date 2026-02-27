package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class OrderViewPanel extends JPanel {
    private final JTextField fltOrderId = new JTextField();
    private final JTextField fltOrderDate = new JTextField();

    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnUpdateOrder = new JButton("Update Order");
    private final JButton btnDownloadPdf = new JButton("Download PDF");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public OrderViewPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel filter = new JPanel(new GridBagLayout());
        filter.setBackground(ModuleTheme.WHITE);
        filter.setBorder(ModuleTheme.sectionBorder("Search & Filters"));

        ModuleTheme.styleInput(fltOrderId);
        ModuleTheme.styleInput(fltOrderDate);
        ModuleTheme.addDatePicker(fltOrderDate);
        ModuleTheme.styleSubtleButton(btnRefresh);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; filter.add(new JLabel("Order ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; filter.add(fltOrderId, gbc);
        gbc.gridx = 2; gbc.weightx = 0; filter.add(new JLabel("Order Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; filter.add(fltOrderDate, gbc);
        gbc.gridx = 4; gbc.weightx = 0; filter.add(btnRefresh, gbc);

        tableModel = new DefaultTableModel(new String[]{"S.No", "Order ID", "Order Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleAccentButton(btnUpdateOrder);
        ModuleTheme.stylePrimaryButton(btnDownloadPdf);
        footer.add(btnUpdateOrder);
        footer.add(btnDownloadPdf);

        add(filter, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public JTextField getFltOrderId() { return fltOrderId; }
    public JTextField getFltOrderDate() { return fltOrderDate; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnUpdateOrder() { return btnUpdateOrder; }
    public JButton getBtnDownloadPdf() { return btnDownloadPdf; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }
}
