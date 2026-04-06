package com.library.ui;

import com.library.model.BookCopy;
import com.library.service.BookLogic;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class BookEditDialog extends JDialog {
    private final JTextField txtAccessNo = new JTextField();
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtTitle = new JTextField();
    private final JTextField txtVolume = new JTextField();
    private final JTextField txtEdition = new JTextField();
    private final JTextField txtPublication = new JTextField();
    private final JTextField txtPubYear = new JTextField();
    private final JTextField txtPages = new JTextField();
    private final JTextField txtSource = new JTextField();
    private final JTextField txtClassNo = new JTextField();
    private final JTextField txtBookNo = new JTextField();
    private final JTextField txtCost = new JTextField();
    private final JTextField txtBillNo = new JTextField();
    private final JTextField txtBillDate = new JTextField();
    private final JTextField txtTags = new JTextField();
    private final JTextField txtWithdrawn = new JTextField();
    private final JTextField txtRemarks = new JTextField();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "WITHDRAWN", "LOST", "DAMAGED"});


    private boolean saved = false;

    public BookEditDialog(Window owner, BookCopy copy) {
        super(owner, "Update Book Copy", ModalityType.APPLICATION_MODAL);
        setSize(720, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        txtAccessNo.setEditable(false);
        ModuleTheme.styleInput(txtBillDate);
        ModuleTheme.styleInput(txtWithdrawn);
        ModuleTheme.addDatePicker(txtBillDate);
        ModuleTheme.addDatePicker(txtWithdrawn);
        txtPublication.setToolTipText("Enter as: Publisher, Place");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addRow(form, gbc, r++, "Accession No", txtAccessNo, "Author", txtAuthor);
        addRow(form, gbc, r++, "Title", txtTitle, "Volume", txtVolume);
        addRow(form, gbc, r++, "Edition", txtEdition, "Pub Year", txtPubYear);
        addWideRow(form, gbc, r++, "Publication (Publisher, Place)", txtPublication);
        addRow(form, gbc, r++, "Pages", txtPages, "Source", txtSource);
        addRow(form, gbc, r++, "Class No", txtClassNo, "Book No", txtBookNo);
        addRow(form, gbc, r++, "Cost", txtCost, "Bill No", txtBillNo);
        addRow(form, gbc, r++, "Bill Date (dd/MM/yyyy)", txtBillDate, "Withdrawn Date", txtWithdrawn);
        addRow(form, gbc, r++, "Tags (comma separated)", txtTags, "Status", cmbStatus);
        
        gbc.gridy = r++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Remarks"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        form.add(txtRemarks, gbc);
        gbc.gridwidth = 1;

        styleAllInputs();

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

    private void styleAllInputs() {
        ModuleTheme.styleInput(txtAuthor);
        ModuleTheme.styleInput(txtTitle);
        ModuleTheme.styleInput(txtVolume);
        ModuleTheme.styleInput(txtEdition);
        ModuleTheme.styleInput(txtPublication);
        ModuleTheme.styleInput(txtPubYear);
        ModuleTheme.styleInput(txtPages);
        ModuleTheme.styleInput(txtSource);
        ModuleTheme.styleInput(txtClassNo);
        ModuleTheme.styleInput(txtBookNo);
        ModuleTheme.styleInput(txtCost);
        ModuleTheme.styleInput(txtBillNo);
        ModuleTheme.styleInput(txtBillDate);
        ModuleTheme.styleInput(txtTags);
        ModuleTheme.styleInput(txtWithdrawn);
        ModuleTheme.styleInput(txtRemarks);
        ModuleTheme.styleCombo(cmbStatus);
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

    private void addWideRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent component) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        panel.add(component, gbc);
        gbc.gridwidth = 1;
    }

    private void bind(BookCopy b) {
        txtAccessNo.setText(b.getAccessionNo());
        txtAuthor.setText(b.getAuthorName());
        txtTitle.setText(b.getTitle());
        txtVolume.setText(b.getVolume() == null ? "" : String.valueOf(b.getVolume()));
        txtEdition.setText(String.valueOf(b.getEdition()));
        txtPublication.setText(BookLogic.formatPublication(b.getPublisher(), b.getPublicationPlace()));
        txtPubYear.setText(String.valueOf(b.getPublicationYear()));
        txtPages.setText(String.valueOf(b.getPages()));
        txtSource.setText(b.getSource());
        txtClassNo.setText(b.getClassNo());
        txtBookNo.setText(b.getBookNo());
        txtCost.setText(String.valueOf(b.getCost()));
        txtBillNo.setText(b.getBillNo());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtBillDate.setText(b.getBillDate() == null ? "" : sdf.format(b.getBillDate()));
        txtTags.setText(b.getTags());
        txtWithdrawn.setText(b.getWithdrawnDate() == null ? "" : sdf.format(b.getWithdrawnDate()));
        txtRemarks.setText(b.getRemarks());
        cmbStatus.setSelectedItem(b.getStatus());
    }

    public boolean isSaved() { return saved; }

    public String getAccessNo() { return txtAccessNo.getText().trim(); }
    public String getAuthor() { return txtAuthor.getText().trim(); }
    public String getTitle() { return txtTitle.getText().trim(); }
    public String getVolume() { return txtVolume.getText().trim(); }
    public String getEdition() { return txtEdition.getText().trim(); }
    public String getPublication() { return txtPublication.getText().trim(); }
    public String getPubYear() { return txtPubYear.getText().trim(); }
    public String getPages() { return txtPages.getText().trim(); }
    public String getSource() { return txtSource.getText().trim(); }
    public String getClassNo() { return txtClassNo.getText().trim(); }
    public String getBookNo() { return txtBookNo.getText().trim(); }
    public String getCost() { return txtCost.getText().trim(); }
    public String getBillNo() { return txtBillNo.getText().trim(); }
    public String getBillDate() { return txtBillDate.getText().trim(); }
    public String getTags() { return txtTags.getText().trim(); }
    public String getWithdrawn() { return txtWithdrawn.getText().trim(); }
    public String getRemarks() { return txtRemarks.getText().trim(); }
    public String getStatus() { return String.valueOf(cmbStatus.getSelectedItem()); }
}
