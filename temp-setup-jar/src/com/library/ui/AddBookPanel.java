package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddBookPanel extends JPanel {
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtTitle = new JTextField();
    private final JTextField txtVolume = new JTextField();
    private final JTextField txtEdition = new JTextField();
    private final JTextField txtPublication = new JTextField();
    private final JTextField txtPubYear = new JTextField();
    private final JTextField txtPages = new JTextField();
    private final JComboBox<String> cmbSource = new JComboBox<>(new String[]{"PURCHASE", "DONATION", "GIFT", "EXCHANGE"});
    private final JTextField txtClassNo = new JTextField();
    private final JTextField txtBookNo = new JTextField();
    private final JTextField txtCost = new JTextField();
    private final JTextField txtBillNo = new JTextField();
    private final JTextField txtBillDate = new JTextField();
    private final JTextField txtTags = new JTextField();
    private final JTextField txtWithdrawnDate = new JTextField();
    private final JTextArea txtRemarks = new JTextArea(3, 20);
    private final JTextField txtAccessionPreview = new JTextField();
    private final JTextField txtStatus = new JTextField();

    private final JButton btnSave = new JButton("Add Book Copy");
    private final JButton btnReset = new JButton("Reset");

    public AddBookPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ModuleTheme.WHITE);

        JLabel title = new JLabel("ENTER BOOK COPY DETAILS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ModuleTheme.BLUE_DARK);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(ModuleTheme.WHITE);
        top.add(title);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ModuleTheme.WHITE);
        form.setBorder(ModuleTheme.sectionBorder("Add Book"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        styleAllInputs();

        int r = 0;
        addRow(form, gbc, r++, "Accession No (auto)", txtAccessionPreview, "Author Name", txtAuthor);
        addRow(form, gbc, r++, "Book Title", txtTitle, "Volume", txtVolume);
        addRow(form, gbc, r++, "Edition", txtEdition, "Publication Year", txtPubYear);
        addWideRow(form, gbc, r++, "Publication (Publisher, Place)", txtPublication);
        addRow(form, gbc, r++, "Pages", txtPages, "Source", cmbSource);
        addRow(form, gbc, r++, "Class No", txtClassNo, "Book No (auto)", txtBookNo);
        addRow(form, gbc, r++, "Cost", txtCost, "Bill No", txtBillNo);
        addRow(form, gbc, r++, "Bill Date (dd/MM/yyyy)", txtBillDate, "Withdrawn Date (optional)", txtWithdrawnDate);
        addRow(form, gbc, r++, "Tags (comma separated)", txtTags, "Status (auto)", txtStatus);
        gbc.gridy = r++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label("Remarks"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        form.add(new JScrollPane(txtRemarks), gbc);
        gbc.gridwidth = 1; // reset

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleSubtleButton(btnReset);
        ModuleTheme.stylePrimaryButton(btnSave);
        actions.add(btnReset);
        actions.add(btnSave);

        add(top, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        setStatus("ACTIVE");
    }

    private void styleAllInputs() {
        ModuleTheme.styleInput(txtAuthor);
        ModuleTheme.styleInput(txtTitle);
        ModuleTheme.styleInput(txtVolume);
        ModuleTheme.styleInput(txtEdition);
        ModuleTheme.styleInput(txtPublication);
        ModuleTheme.styleInput(txtPubYear);
        ModuleTheme.styleInput(txtPages);
        ModuleTheme.styleInput(txtClassNo);
        ModuleTheme.styleInput(txtBookNo);
        ModuleTheme.styleInput(txtCost);
        ModuleTheme.styleInput(txtBillNo);
        ModuleTheme.styleInput(txtBillDate);
        ModuleTheme.styleInput(txtTags);
        ModuleTheme.styleInput(txtWithdrawnDate);
        ModuleTheme.addDatePicker(txtBillDate);
        ModuleTheme.addDatePicker(txtWithdrawnDate);

        txtBillDate.setToolTipText("Click to select Bill Date (dd/MM/yyyy)");
        txtWithdrawnDate.setToolTipText("Click to select Withdrawn Date (optional)");
        txtPublication.setToolTipText("Enter as: Publisher, Place");

        setupValidation();
    }

    private void setupValidation() {
        ModuleTheme.addValidation(txtAuthor, s -> !s.trim().isEmpty());
        ModuleTheme.addValidation(txtTitle, s -> !s.trim().isEmpty());
        ModuleTheme.addValidation(txtPublication, s -> !s.trim().isEmpty());
        ModuleTheme.addValidation(txtCost, s -> s.trim().matches("\\d+(\\.\\d+)?"));
        ModuleTheme.addValidation(txtBillNo, s -> !s.trim().isEmpty());
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

    private void addWideRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        form.add(component, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    public JTextField getTxtAuthor() { return txtAuthor; }
    public JTextField getTxtTitle() { return txtTitle; }
    public JTextField getTxtVolume() { return txtVolume; }
    public JTextField getTxtEdition() { return txtEdition; }
    public JTextField getTxtPublication() { return txtPublication; }
    public JTextField getTxtPubYear() { return txtPubYear; }
    public JTextField getTxtPages() { return txtPages; }
    public JComboBox<String> getCmbSource() { return cmbSource; }
    public JTextField getTxtClassNo() { return txtClassNo; }
    public JTextField getTxtBookNo() { return txtBookNo; }
    public JTextField getTxtCost() { return txtCost; }
    public JTextField getTxtBillNo() { return txtBillNo; }
    public JTextField getTxtBillDate() { return txtBillDate; }
    public JTextField getTxtTags() { return txtTags; }
    public JTextField getTxtWithdrawnDate() { return txtWithdrawnDate; }
    public JTextArea getTxtRemarks() { return txtRemarks; }
    public JTextField getTxtAccessionPreview() { return txtAccessionPreview; }
    public JTextField getTxtStatus() { return txtStatus; }

    public JButton getBtnSave() { return btnSave; }
    public JButton getBtnReset() { return btnReset; }

    public void setBookNo(String bookNo) { txtBookNo.setText(bookNo); }
    public void setAccessionPreview(String accessionNo) { txtAccessionPreview.setText(accessionNo); }
    public void setStatus(String status) { txtStatus.setText(status); }

    public void clearForm() {
        txtAuthor.setText("");
        txtTitle.setText("");
        txtVolume.setText("");
        txtEdition.setText("");
        txtPublication.setText("");
        txtPubYear.setText("");
        txtPages.setText("");
        cmbSource.setSelectedIndex(0);
        txtClassNo.setText("");
        txtBookNo.setText("");
        txtCost.setText("");
        txtBillNo.setText("");
        txtBillDate.setText("");
        txtTags.setText("");
        txtWithdrawnDate.setText("");
        txtRemarks.setText("");
        txtStatus.setText("ACTIVE");
    }
}
