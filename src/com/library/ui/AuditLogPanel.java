package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class AuditLogPanel extends JPanel {
    private final JTextField txtUserId = new JTextField();
    private final JComboBox<String> cmbModule = new JComboBox<>();
    private final JTextField txtFromDate = new JTextField();
    private final JTextField txtToDate = new JTextField();
    private final JButton btnSearch = new JButton("Search");
    private final JButton btnReset = new JButton("Reset");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public AuditLogPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel filter = new JPanel(new GridBagLayout());
        filter.setBackground(ModuleTheme.WHITE);
        filter.setBorder(ModuleTheme.sectionBorder("Audit Filters"));

        ModuleTheme.styleInput(txtUserId);
        ModuleTheme.styleCombo(cmbModule);
        ModuleTheme.styleInput(txtFromDate);
        ModuleTheme.styleInput(txtToDate);
        ModuleTheme.stylePrimaryButton(btnSearch);
        ModuleTheme.styleSubtleButton(btnReset);

        txtFromDate.setToolTipText("yyyy-MM-dd");
        txtToDate.setToolTipText("yyyy-MM-dd");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; filter.add(label("User ID"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; filter.add(txtUserId, gbc);
        gbc.gridx = 2; gbc.weightx = 0; filter.add(label("Module"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; filter.add(cmbModule, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; filter.add(label("From Date"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; filter.add(txtFromDate, gbc);
        gbc.gridx = 2; gbc.weightx = 0; filter.add(label("To Date"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; filter.add(txtToDate, gbc);

        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionWrap.setBackground(ModuleTheme.WHITE);
        actionWrap.add(btnReset);
        actionWrap.add(btnSearch);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        filter.add(actionWrap, gbc);

        tableModel = new DefaultTableModel(
            new String[]{"Log ID", "User ID", "Module", "Action Description", "Timestamp"},
            0
        ) {
            @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        };

        table = new JTable(tableModel);
        ModuleTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);

        add(filter, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    public JTextField getTxtUserId() { return txtUserId; }
    public JComboBox<String> getCmbModule() { return cmbModule; }
    public JTextField getTxtFromDate() { return txtFromDate; }
    public JTextField getTxtToDate() { return txtToDate; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnReset() { return btnReset; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }
}
