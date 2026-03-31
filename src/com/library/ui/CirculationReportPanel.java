package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class CirculationReportPanel extends JPanel {
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"All", "ISSUED", "RETURNED"});
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

        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Circulation Report"));

        ModuleTheme.styleInput(txtSearch);
        ModuleTheme.styleCombo(cmbStatus);
        ModuleTheme.styleSubtleButton(btnRefresh);
        ModuleTheme.stylePrimaryButton(btnPdf);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        top.add(new JLabel("Search Records:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        top.add(txtSearch, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        top.add(new JLabel("Status:"), gbc);

        gbc.gridx = 3;
        top.add(cmbStatus, gbc);

        gbc.gridx = 4;
        top.add(btnRefresh, gbc);

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
            || (status != null && !"All".equals(String.valueOf(status)));
    }

    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCmbStatus() { return cmbStatus; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnPdf() { return btnPdf; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }

    public void updateTotalCount(int count) {
        lblTotal.setText("Total Records: " + count);
    }
}
