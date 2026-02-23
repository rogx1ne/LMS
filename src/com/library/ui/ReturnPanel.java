package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ReturnPanel extends JPanel {
    private final JTextField txtCardIdSearch = new JTextField();
    private final JButton btnSearch = new JButton("Search");
    private final JButton btnReset = new JButton("Reset");
    private final JButton btnProcessReturn = new JButton("Process Return");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JLabel lblCount = new JLabel("Issued Books: 0");

    public ReturnPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(ModuleTheme.WHITE);
        searchPanel.setBorder(ModuleTheme.sectionBorder("Search Issued Books"));

        ModuleTheme.styleInput(txtCardIdSearch);
        ModuleTheme.styleSubtleButton(btnReset);
        ModuleTheme.stylePrimaryButton(btnSearch);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        searchPanel.add(label("Card ID"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        searchPanel.add(txtCardIdSearch, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        searchPanel.add(btnReset, gbc);

        gbc.gridx = 3;
        searchPanel.add(btnSearch, gbc);

        tableModel = new DefaultTableModel(
            new String[]{"S.No", "Issue ID", "Accession No", "Book Title", "Author", "Issue Date", "Due Date", "Issued By"},
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(ModuleTheme.TABLE_BG);
        scrollPane.setBorder(ModuleTheme.sectionBorder("Current Issues"));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(ModuleTheme.WHITE);
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(lblCount, BorderLayout.WEST);

        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionWrap.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleAccentButton(btnProcessReturn);
        actionWrap.add(btnProcessReturn);
        footer.add(actionWrap, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    public JTextField getTxtCardIdSearch() { return txtCardIdSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnReset() { return btnReset; }
    public JButton getBtnProcessReturn() { return btnProcessReturn; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }
    public JLabel getLblCount() { return lblCount; }
}
