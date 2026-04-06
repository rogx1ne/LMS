package com.library.ui;

import com.library.model.AvailableBookRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class AvailableBookSearchDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private String selectedAccessionNo;

    public AvailableBookSearchDialog(Window owner, List<AvailableBookRow> rows) {
        super(owner, "Search Available Books", ModalityType.APPLICATION_MODAL);
        setSize(760, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 8));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleInput(txtSearch);
        top.add(new JLabel("Search by Accession / Title / Author:"), BorderLayout.WEST);
        top.add(txtSearch, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new String[]{"Accession No", "Book Title", "Author"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (AvailableBookRow row : rows) {
            tableModel.addRow(new Object[]{row.getAccessionNo(), row.getTitle(), row.getAuthorName()});
        }

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectCurrentRow();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(ModuleTheme.createEmptyStateLayer(
            table,
            () -> txtSearch.getText().trim().isEmpty() ? "No available books found." : "Record Not Found"
        ));
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);

        JButton btnSelect = new JButton("Use Selected Book");
        JButton btnCancel = new JButton("Cancel");
        ModuleTheme.stylePrimaryButton(btnSelect);
        ModuleTheme.styleSubtleButton(btnCancel);
        btnSelect.addActionListener(e -> selectCurrentRow());
        btnCancel.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(ModuleTheme.WHITE);
        footer.add(btnCancel);
        footer.add(btnSelect);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(search), 0, 1, 2));
    }

    private void selectCurrentRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an available book first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        selectedAccessionNo = String.valueOf(tableModel.getValueAt(modelRow, 0));
        dispose();
    }

    public String getSelectedAccessionNo() {
        return selectedAccessionNo;
    }
}
