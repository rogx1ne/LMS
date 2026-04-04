package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class CirculationReportPanel extends JPanel {
    private final JTextField txtSearch = new JTextField();
    private final JCheckBox chkAdvancedSearch = new JCheckBox("Advanced Search");
    private final JPanel advancedPanel = new JPanel(new GridBagLayout());
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"All", "ISSUED", "RETURNED"});
    private final JTextField txtFromDate = new JTextField(10);
    private final JTextField txtToDate = new JTextField(10);
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnPdf = new JButton("Download PDF");
    private final JLabel lblTotal = new JLabel("Total Records: 0");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public CirculationReportPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new BorderLayout(0, 5));
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Circulation Report"));

        // Simple Search Panel (Always visible)
        JPanel simplePanel = new JPanel(new GridBagLayout());
        simplePanel.setBackground(ModuleTheme.WHITE);
        
        ModuleTheme.styleInput(txtSearch);
        ModuleTheme.styleSubtleButton(btnRefresh);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        simplePanel.add(new JLabel("Search Records:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        simplePanel.add(txtSearch, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        chkAdvancedSearch.setBackground(ModuleTheme.WHITE);
        chkAdvancedSearch.setForeground(ModuleTheme.BLUE_DARK);
        chkAdvancedSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        simplePanel.add(chkAdvancedSearch, gbc);

        gbc.gridx = 3;
        simplePanel.add(btnRefresh, gbc);

        // Advanced Search Panel (Collapsible)
        advancedPanel.setBackground(ModuleTheme.WHITE);
        advancedPanel.setVisible(false);
        
        ModuleTheme.styleCombo(cmbStatus);
        ModuleTheme.styleInput(txtFromDate);
        ModuleTheme.styleInput(txtToDate);
        ModuleTheme.addDatePicker(txtFromDate);
        ModuleTheme.addDatePicker(txtToDate);
        
        txtFromDate.setToolTipText("From Date (dd/MM/yyyy)");
        txtToDate.setToolTipText("To Date (dd/MM/yyyy)");

        GridBagConstraints agbc = new GridBagConstraints();
        agbc.insets = new Insets(5, 7, 5, 7);
        agbc.fill = GridBagConstraints.HORIZONTAL;

        agbc.gridx = 0;
        agbc.gridy = 0;
        agbc.weightx = 0;
        advancedPanel.add(new JLabel("Status:"), agbc);

        agbc.gridx = 1;
        advancedPanel.add(cmbStatus, agbc);

        agbc.gridx = 2;
        advancedPanel.add(new JLabel("From Date:"), agbc);

        agbc.gridx = 3;
        advancedPanel.add(txtFromDate, agbc);

        agbc.gridx = 4;
        advancedPanel.add(new JLabel("To Date:"), agbc);

        agbc.gridx = 5;
        agbc.weightx = 1;
        advancedPanel.add(txtToDate, agbc);

        // Toggle advanced search visibility
        chkAdvancedSearch.addActionListener(e -> {
            advancedPanel.setVisible(chkAdvancedSearch.isSelected());
            top.revalidate();
            top.repaint();
        });

        top.add(simplePanel, BorderLayout.NORTH);
        top.add(advancedPanel, BorderLayout.SOUTH);

        ModuleTheme.stylePrimaryButton(btnPdf);

        String[] cols = {
            "Issue ID", "Borrower Type", "Card ID", "Borrower Name", "Borrower Contact", "Accession No",
            "Book Title", "Author", "Issue Date", "Due Date", "Return Date", "Return Condition", "Fine", "Issued By", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        ModuleTheme.applyStatusRenderer(table, 14);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(ModuleTheme.createEmptyStateLayer(
            table,
            () -> hasActiveFilters() ? "Record Not Found" : "No circulation records available."
        ));
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(ModuleTheme.WHITE);
        lblTotal.setForeground(ModuleTheme.BLACK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(ModuleTheme.WHITE);
        actions.add(btnPdf);

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private boolean hasActiveFilters() {
        Object status = cmbStatus.getSelectedItem();
        return !txtSearch.getText().trim().isEmpty()
            || (status != null && !"All".equals(String.valueOf(status)))
            || !txtFromDate.getText().trim().isEmpty()
            || !txtToDate.getText().trim().isEmpty();
    }

    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCmbStatus() { return cmbStatus; }
    public JTextField getTxtFromDate() { return txtFromDate; }
    public JTextField getTxtToDate() { return txtToDate; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnPdf() { return btnPdf; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }

    public void updateTotalCount(int count) {
        lblTotal.setText("Total Records: " + count);
    }
}
