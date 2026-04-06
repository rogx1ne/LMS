package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IssuePanel extends JPanel {
    private final JComboBox<String> cmbBorrowerType = new JComboBox<>(new String[]{"STUDENT", "FACULTY"});
    private final JTextField txtCardId = new JTextField();
    private final JTextField txtFacultyName = new JTextField();
    private final JTextField txtFacultyContact = new JTextField();
    private final JTextField txtAccessionNo = new JTextField();
    private final JTextField txtIssueId = new JTextField();
    private final JTextField txtIssueDate = new JTextField();
    private final JTextField txtDueDate = new JTextField();
    private final JTextField txtIssuedBy = new JTextField();
    private final JButton btnSearchBook = new JButton("Search Available");

    private final JButton btnIssue = new JButton("Issue Book");
    private final JButton btnClear = new JButton("Clear");

    private final JTextArea txtMessage = new JTextArea(8, 20);

    public IssuePanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ModuleTheme.WHITE);
        form.setBorder(ModuleTheme.sectionBorder("Issue Book"));
        styleInputs();

        JPanel accessionWrap = new JPanel(new BorderLayout(6, 0));
        accessionWrap.setOpaque(false);
        accessionWrap.add(txtAccessionNo, BorderLayout.CENTER);
        accessionWrap.add(btnSearchBook, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "Borrower Type", cmbBorrowerType, "Card ID", txtCardId);
        addRow(form, gbc, row++, "Faculty Name", txtFacultyName, "Faculty Contact", txtFacultyContact);
        addRow(form, gbc, row++, "Accession No", accessionWrap, "Issue ID", txtIssueId);
        addRow(form, gbc, row++, "Issued By", txtIssuedBy, "Issue Date", txtIssueDate);
        addRow(form, gbc, row++, "Due Date", txtDueDate, "", new JLabel(""));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleSubtleButton(btnClear);
        ModuleTheme.stylePrimaryButton(btnIssue);
        actions.add(btnClear);
        actions.add(btnIssue);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        form.add(actions, gbc);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(ModuleTheme.WHITE);
        messagePanel.setBorder(ModuleTheme.sectionBorder("Issue Log"));
        txtMessage.setEditable(false);
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMessage.setBackground(new Color(250, 250, 250));
        txtMessage.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        messagePanel.add(new JScrollPane(txtMessage), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
    }

    private void styleInputs() {
        ModuleTheme.styleInput(txtCardId);
        ModuleTheme.styleInput(txtFacultyName);
        ModuleTheme.styleInput(txtFacultyContact);
        ModuleTheme.styleInput(txtAccessionNo);
        ModuleTheme.styleInput(txtIssueId);
        ModuleTheme.styleInput(txtIssueDate);
        ModuleTheme.styleInput(txtDueDate);
        ModuleTheme.styleInput(txtIssuedBy);
        ModuleTheme.styleCombo(cmbBorrowerType);
        ModuleTheme.styleSubtleButton(btnSearchBook);

        txtIssueId.setEditable(false);
        txtIssueDate.setEditable(false);
        txtDueDate.setEditable(false);
        txtIssuedBy.setEditable(false);

        Color ro = new Color(245, 245, 245);
        txtIssueId.setBackground(ro);
        txtIssueDate.setBackground(ro);
        txtDueDate.setBackground(ro);
        txtIssuedBy.setBackground(ro);

        cmbBorrowerType.addActionListener(e -> syncBorrowerMode());
        syncBorrowerMode();
    }

    private void syncBorrowerMode() {
        boolean facultyMode = "FACULTY".equals(String.valueOf(cmbBorrowerType.getSelectedItem()));
        txtCardId.setEditable(!facultyMode);
        txtCardId.setBackground(facultyMode ? new Color(245, 245, 245) : ModuleTheme.WHITE);
        if (facultyMode) {
            txtCardId.setText("");
        }

        txtFacultyName.setEditable(facultyMode);
        txtFacultyContact.setEditable(facultyMode);
        txtFacultyName.setBackground(facultyMode ? ModuleTheme.WHITE : new Color(245, 245, 245));
        txtFacultyContact.setBackground(facultyMode ? ModuleTheme.WHITE : new Color(245, 245, 245));
        if (!facultyMode) {
            txtFacultyName.setText("");
            txtFacultyContact.setText("");
        }
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

    public JComboBox<String> getCmbBorrowerType() { return cmbBorrowerType; }
    public JTextField getTxtCardId() { return txtCardId; }
    public JTextField getTxtFacultyName() { return txtFacultyName; }
    public JTextField getTxtFacultyContact() { return txtFacultyContact; }
    public JTextField getTxtAccessionNo() { return txtAccessionNo; }
    public JTextField getTxtIssueId() { return txtIssueId; }
    public JTextField getTxtIssueDate() { return txtIssueDate; }
    public JTextField getTxtDueDate() { return txtDueDate; }
    public JTextField getTxtIssuedBy() { return txtIssuedBy; }
    public JButton getBtnSearchBook() { return btnSearchBook; }
    public JButton getBtnIssue() { return btnIssue; }
    public JButton getBtnClear() { return btnClear; }
    public JTextArea getTxtMessage() { return txtMessage; }

    public void clearIssueInputs() {
        cmbBorrowerType.setSelectedItem("STUDENT");
        txtCardId.setText("");
        txtFacultyName.setText("");
        txtFacultyContact.setText("");
        txtAccessionNo.setText("");
    }
}
