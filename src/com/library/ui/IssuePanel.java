package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IssuePanel extends JPanel {
    private final JTextField txtCardId = new JTextField();
    private final JTextField txtAccessionNo = new JTextField();
    private final JTextField txtIssueId = new JTextField();
    private final JTextField txtIssueDate = new JTextField();
    private final JTextField txtDueDate = new JTextField();
    private final JTextField txtIssuedBy = new JTextField();

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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "Card ID", txtCardId, "Accession No", txtAccessionNo);
        addRow(form, gbc, row++, "Issue ID", txtIssueId, "Issued By", txtIssuedBy);
        addRow(form, gbc, row++, "Issue Date", txtIssueDate, "Due Date", txtDueDate);

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
        ModuleTheme.styleInput(txtAccessionNo);
        ModuleTheme.styleInput(txtIssueId);
        ModuleTheme.styleInput(txtIssueDate);
        ModuleTheme.styleInput(txtDueDate);
        ModuleTheme.styleInput(txtIssuedBy);

        txtIssueId.setEditable(false);
        txtIssueDate.setEditable(false);
        txtDueDate.setEditable(false);
        txtIssuedBy.setEditable(false);

        Color ro = new Color(245, 245, 245);
        txtIssueId.setBackground(ro);
        txtIssueDate.setBackground(ro);
        txtDueDate.setBackground(ro);
        txtIssuedBy.setBackground(ro);
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

    public JTextField getTxtCardId() { return txtCardId; }
    public JTextField getTxtAccessionNo() { return txtAccessionNo; }
    public JTextField getTxtIssueId() { return txtIssueId; }
    public JTextField getTxtIssueDate() { return txtIssueDate; }
    public JTextField getTxtDueDate() { return txtDueDate; }
    public JTextField getTxtIssuedBy() { return txtIssuedBy; }
    public JButton getBtnIssue() { return btnIssue; }
    public JButton getBtnClear() { return btnClear; }
    public JTextArea getTxtMessage() { return txtMessage; }

    public void clearIssueInputs() {
        txtCardId.setText("");
        txtAccessionNo.setText("");
    }
}
