package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddBookPanel extends JPanel {
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtTitle = new JTextField();
    private final JTextField txtVolume = new JTextField();
    private final JTextField txtEdition = new JTextField();
    private final JTextField txtPublisher = new JTextField();
    private final JTextField txtPubPlace = new JTextField();
    private final JTextField txtPubYear = new JTextField();
    private final JTextField txtPages = new JTextField();
    private final JComboBox<String> cmbSource = new JComboBox<>(new String[]{"PURCHASE", "DONATION", "GIFT", "EXCHANGE"});
    private final JTextField txtClassNo = new JTextField();
    private final JTextField txtBookNo = new JTextField();
    private final JTextField txtCost = new JTextField();
    private final JTextField txtBillNo = new JTextField();
    private final JTextField txtBillDate = new JTextField();
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
        addRow(form, gbc, r++, "Edition", txtEdition, "Publisher", txtPublisher);
        addRow(form, gbc, r++, "Publisher Place", txtPubPlace, "Publication Year", txtPubYear);
        addRow(form, gbc, r++, "Pages", txtPages, "Source", cmbSource);
        addRow(form, gbc, r++, "Class No", txtClassNo, "Book No (auto)", txtBookNo);
        addRow(form, gbc, r++, "Cost", txtCost, "Bill No", txtBillNo);
        addRow(form, gbc, r++, "Bill Date (yyyy-MM-dd)", txtBillDate, "Withdrawn Date (optional)", txtWithdrawnDate);
        addRow(form, gbc, r++, "Status (auto)", txtStatus, "Remarks", new JScrollPane(txtRemarks));

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
        ModuleTheme.styleInput(txtPublisher);
        ModuleTheme.styleInput(txtPubPlace);
        ModuleTheme.styleInput(txtPubYear);
        ModuleTheme.styleInput(txtPages);
        ModuleTheme.styleInput(txtClassNo);
        ModuleTheme.styleInput(txtBookNo);
        ModuleTheme.styleInput(txtCost);
        ModuleTheme.styleInput(txtBillNo);
        ModuleTheme.styleInput(txtBillDate);
        ModuleTheme.styleInput(txtWithdrawnDate);
        ModuleTheme.addDatePicker(txtBillDate);
        ModuleTheme.addDatePicker(txtWithdrawnDate);

        txtBillDate.setToolTipText("Click to select Bill Date (yyyy-MM-dd)");
        txtWithdrawnDate.setToolTipText("Click to select Withdrawn Date (optional)");
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
    public JTextField getTxtPublisher() { return txtPublisher; }
    public JTextField getTxtPubPlace() { return txtPubPlace; }
    public JTextField getTxtPubYear() { return txtPubYear; }
    public JTextField getTxtPages() { return txtPages; }
    public JComboBox<String> getCmbSource() { return cmbSource; }
    public JTextField getTxtClassNo() { return txtClassNo; }
    public JTextField getTxtBookNo() { return txtBookNo; }
    public JTextField getTxtCost() { return txtCost; }
    public JTextField getTxtBillNo() { return txtBillNo; }
    public JTextField getTxtBillDate() { return txtBillDate; }
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
        txtPublisher.setText("");
        txtPubPlace.setText("");
        txtPubYear.setText("");
        txtPages.setText("");
        cmbSource.setSelectedIndex(0);
        txtClassNo.setText("");
        txtBookNo.setText("");
        txtCost.setText("");
        txtBillNo.setText("");
        txtBillDate.setText("");
        txtWithdrawnDate.setText("");
        txtRemarks.setText("");
        txtStatus.setText("ACTIVE");
    }
}
