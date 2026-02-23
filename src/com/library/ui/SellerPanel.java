package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class SellerPanel extends JPanel {
    private final JTextField txtSellerId = new JTextField();
    private final JTextField txtCompanyName = new JTextField();
    private final JTextField txtCompanyContact = new JTextField();
    private final JTextField txtCompanyMail = new JTextField();
    private final JTextField txtContactPerson = new JTextField();
    private final JTextField txtContactNo = new JTextField();
    private final JTextField txtContactMail = new JTextField();
    private final JTextArea txtAddress = new JTextArea(3, 20);

    private final JButton btnAdd = new JButton("Add Seller");
    private final JButton btnUpdate = new JButton("Update Seller");
    private final JButton btnClear = new JButton("Clear");
    private final JButton btnPdf = new JButton("Download PDF");

    private final JTextField fltSellerId = new JTextField();
    private final JTextField fltCompany = new JTextField();
    private final JTextField fltContactPerson = new JTextField();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public SellerPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel topWrap = new JPanel(new BorderLayout(0, 8));
        topWrap.setBackground(ModuleTheme.WHITE);
        topWrap.add(buildFormPanel(), BorderLayout.CENTER);
        topWrap.add(buildFilterPanel(), BorderLayout.SOUTH);

        String[] cols = {
            "S-ID", "Company Name", "Company Contact", "Company Mail",
            "Contact Person", "Contact Person No", "Contact Person Mail", "Address"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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
        ModuleTheme.styleSubtleButton(btnClear);
        ModuleTheme.stylePrimaryButton(btnPdf);
        footer.add(btnClear);
        footer.add(btnPdf);

        add(topWrap, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ModuleTheme.WHITE);
        form.setBorder(ModuleTheme.sectionBorder("Add / Update Seller"));

        styleInputs();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "S-ID (Auto)", txtSellerId, "Company Name", txtCompanyName);
        addRow(form, gbc, row++, "Company Contact", txtCompanyContact, "Company Mail", txtCompanyMail);
        addRow(form, gbc, row++, "Contact Person", txtContactPerson, "Contact Person No", txtContactNo);
        addRow(form, gbc, row++, "Contact Person Mail", txtContactMail, "Address", new JScrollPane(txtAddress));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnAdd);
        ModuleTheme.styleAccentButton(btnUpdate);
        actions.add(btnAdd);
        actions.add(btnUpdate);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        form.add(actions, gbc);

        return form;
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModuleTheme.WHITE);
        panel.setBorder(ModuleTheme.sectionBorder("Search & Filters"));

        ModuleTheme.styleInput(fltSellerId);
        ModuleTheme.styleInput(fltCompany);
        ModuleTheme.styleInput(fltContactPerson);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFilter(panel, gbc, "S-ID:", fltSellerId, 0);
        addFilter(panel, gbc, "Company:", fltCompany, 2);
        addFilter(panel, gbc, "Contact Person:", fltContactPerson, 4);

        return panel;
    }

    private void addFilter(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int x) {
        gbc.gridx = x;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String l1, JComponent c1, String l2, JComponent c2) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label(l1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(c1, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(label(l2), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        form.add(c2, gbc);
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    private void styleInputs() {
        ModuleTheme.styleInput(txtSellerId);
        ModuleTheme.styleInput(txtCompanyName);
        ModuleTheme.styleInput(txtCompanyContact);
        ModuleTheme.styleInput(txtCompanyMail);
        ModuleTheme.styleInput(txtContactPerson);
        ModuleTheme.styleInput(txtContactNo);
        ModuleTheme.styleInput(txtContactMail);
        ModuleTheme.styleTextArea(txtAddress);
        txtSellerId.setEditable(false);
        txtSellerId.setBackground(new Color(245, 245, 245));
    }

    public JTextField getTxtSellerId() { return txtSellerId; }
    public JTextField getTxtCompanyName() { return txtCompanyName; }
    public JTextField getTxtCompanyContact() { return txtCompanyContact; }
    public JTextField getTxtCompanyMail() { return txtCompanyMail; }
    public JTextField getTxtContactPerson() { return txtContactPerson; }
    public JTextField getTxtContactNo() { return txtContactNo; }
    public JTextField getTxtContactMail() { return txtContactMail; }
    public JTextArea getTxtAddress() { return txtAddress; }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnPdf() { return btnPdf; }

    public JTextField getFltSellerId() { return fltSellerId; }
    public JTextField getFltCompany() { return fltCompany; }
    public JTextField getFltContactPerson() { return fltContactPerson; }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }

    public void clearForm() {
        txtCompanyName.setText("");
        txtCompanyContact.setText("");
        txtCompanyMail.setText("");
        txtContactPerson.setText("");
        txtContactNo.setText("");
        txtContactMail.setText("");
        txtAddress.setText("");
    }
}
