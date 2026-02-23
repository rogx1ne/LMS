package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class UserManagementPanel extends JPanel {
    private final JTextField txtUserId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtPhone = new JTextField();

    private final JButton btnCreateUser = new JButton("Create User");
    private final JButton btnDeleteUser = new JButton("Deactivate User");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnClear = new JButton("Clear");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public UserManagementPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ModuleTheme.WHITE);
        form.setBorder(ModuleTheme.sectionBorder("Create / Manage Users"));

        ModuleTheme.styleInput(txtUserId);
        ModuleTheme.styleInput(txtName);
        ModuleTheme.styleInput(txtPassword);
        ModuleTheme.styleInput(txtEmail);
        ModuleTheme.styleInput(txtPhone);

        txtUserId.setEditable(false);
        txtUserId.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "User ID", txtUserId, "Name", txtName);
        addRow(form, gbc, row++, "Password", txtPassword, "Email", txtEmail);
        addRow(form, gbc, row++, "Phone", txtPhone, "", new JLabel(""));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnCreateUser);
        ModuleTheme.styleAccentButton(btnDeleteUser);
        ModuleTheme.styleSubtleButton(btnRefresh);
        ModuleTheme.styleSubtleButton(btnClear);
        actions.add(btnRefresh);
        actions.add(btnClear);
        actions.add(btnDeleteUser);
        actions.add(btnCreateUser);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        form.add(actions, gbc);

        tableModel = new DefaultTableModel(
            new String[]{"User ID", "Name", "Email", "Phone", "Status"},
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

        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String l1, JComponent c1, String l2, JComponent c2) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(label(l1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(c1, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(label(l2), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        panel.add(c2, gbc);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    public JTextField getTxtUserId() { return txtUserId; }
    public JTextField getTxtName() { return txtName; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtPhone() { return txtPhone; }
    public JButton getBtnCreateUser() { return btnCreateUser; }
    public JButton getBtnDeleteUser() { return btnDeleteUser; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }

    public void clearForm() {
        txtName.setText("");
        txtPassword.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
    }
}
