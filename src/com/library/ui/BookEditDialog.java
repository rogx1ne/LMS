package com.library.ui;

import com.library.model.BookCopy;

import javax.swing.*;
import java.awt.*;

public class BookEditDialog extends JDialog {
    private final JTextField txtAccessNo = new JTextField();
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtTitle = new JTextField();
    private final JTextField txtVolume = new JTextField();
    private final JTextField txtEdition = new JTextField();
    private final JTextField txtPublisher = new JTextField();
    private final JTextField txtPubPlace = new JTextField();
    private final JTextField txtPubYear = new JTextField();
    private final JTextField txtPages = new JTextField();
    private final JTextField txtSource = new JTextField();
    private final JTextField txtClassNo = new JTextField();
    private final JTextField txtBookNo = new JTextField();
    private final JTextField txtCost = new JTextField();
    private final JTextField txtBillNo = new JTextField();
    private final JTextField txtBillDate = new JTextField();
    private final JTextField txtWithdrawn = new JTextField();
    private final JTextArea txtRemarks = new JTextArea(3, 20);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "WITHDRAWN", "LOST", "DAMAGED"});

    private boolean saved = false;

    public BookEditDialog(Window owner, BookCopy copy) {
        super(owner, "Update Book Copy", ModalityType.APPLICATION_MODAL);
        setSize(720, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        txtAccessNo.setEditable(false);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addRow(form, gbc, r++, "Accession No", txtAccessNo, "Author", txtAuthor);
        addRow(form, gbc, r++, "Title", txtTitle, "Volume", txtVolume);
        addRow(form, gbc, r++, "Edition", txtEdition, "Publisher", txtPublisher);
        addRow(form, gbc, r++, "Pub Place", txtPubPlace, "Pub Year", txtPubYear);
        addRow(form, gbc, r++, "Pages", txtPages, "Source", txtSource);
        addRow(form, gbc, r++, "Class No", txtClassNo, "Book No", txtBookNo);
        addRow(form, gbc, r++, "Cost", txtCost, "Bill No", txtBillNo);
        addRow(form, gbc, r++, "Bill Date (yyyy-MM-dd)", txtBillDate, "Withdrawn Date", txtWithdrawn);
        addRow(form, gbc, r++, "Status", cmbStatus, "Remarks", new JScrollPane(txtRemarks));

        JButton btnSave = new JButton("Save Changes");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> { saved = true; dispose(); });
        btnCancel.addActionListener(e -> dispose());

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        foot.add(btnCancel);
        foot.add(btnSave);

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(foot, BorderLayout.SOUTH);

        bind(copy);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String l1, JComponent c1, String l2, JComponent c2) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(l1), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(c1, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel(l2), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1;
        panel.add(c2, gbc);
    }

    private void bind(BookCopy b) {
        txtAccessNo.setText(b.getAccessionNo());
        txtAuthor.setText(b.getAuthorName());
        txtTitle.setText(b.getTitle());
        txtVolume.setText(b.getVolume() == null ? "" : String.valueOf(b.getVolume()));
        txtEdition.setText(String.valueOf(b.getEdition()));
        txtPublisher.setText(b.getPublisher());
        txtPubPlace.setText(b.getPublicationPlace());
        txtPubYear.setText(String.valueOf(b.getPublicationYear()));
        txtPages.setText(String.valueOf(b.getPages()));
        txtSource.setText(b.getSource());
        txtClassNo.setText(b.getClassNo());
        txtBookNo.setText(b.getBookNo());
        txtCost.setText(String.valueOf(b.getCost()));
        txtBillNo.setText(b.getBillNo());
        txtBillDate.setText(b.getBillDate() == null ? "" : String.valueOf(b.getBillDate()));
        txtWithdrawn.setText(b.getWithdrawnDate() == null ? "" : String.valueOf(b.getWithdrawnDate()));
        txtRemarks.setText(b.getRemarks());
        cmbStatus.setSelectedItem(b.getStatus());
    }

    public boolean isSaved() { return saved; }

    public String getAccessNo() { return txtAccessNo.getText().trim(); }
    public String getAuthor() { return txtAuthor.getText().trim(); }
    public String getTitle() { return txtTitle.getText().trim(); }
    public String getVolume() { return txtVolume.getText().trim(); }
    public String getEdition() { return txtEdition.getText().trim(); }
    public String getPublisher() { return txtPublisher.getText().trim(); }
    public String getPubPlace() { return txtPubPlace.getText().trim(); }
    public String getPubYear() { return txtPubYear.getText().trim(); }
    public String getPages() { return txtPages.getText().trim(); }
    public String getSource() { return txtSource.getText().trim(); }
    public String getClassNo() { return txtClassNo.getText().trim(); }
    public String getBookNo() { return txtBookNo.getText().trim(); }
    public String getCost() { return txtCost.getText().trim(); }
    public String getBillNo() { return txtBillNo.getText().trim(); }
    public String getBillDate() { return txtBillDate.getText().trim(); }
    public String getWithdrawn() { return txtWithdrawn.getText().trim(); }
    public String getRemarks() { return txtRemarks.getText().trim(); }
    public String getStatus() { return String.valueOf(cmbStatus.getSelectedItem()); }
}
