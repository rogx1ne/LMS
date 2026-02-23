package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataImportExportPanel extends JPanel {
    private final JComboBox<String> cmbTable = new JComboBox<>();
    private final JButton btnExport = new JButton("Export To Excel");
    private final JButton btnImport = new JButton("Import From Excel");
    private final JTextArea txtResult = new JTextArea(12, 20);

    public DataImportExportPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(ModuleTheme.WHITE);
        controls.setBorder(ModuleTheme.sectionBorder("Data Import / Export"));

        ModuleTheme.styleCombo(cmbTable);
        ModuleTheme.stylePrimaryButton(btnExport);
        ModuleTheme.styleAccentButton(btnImport);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        controls.add(label("Select Table"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        controls.add(cmbTable, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        controls.add(btnExport, gbc);

        gbc.gridx = 3;
        controls.add(btnImport, gbc);

        JPanel output = new JPanel(new BorderLayout());
        output.setBackground(ModuleTheme.WHITE);
        output.setBorder(ModuleTheme.sectionBorder("Operation Output"));
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtResult.setBackground(new Color(250, 250, 250));
        txtResult.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        output.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        add(controls, BorderLayout.NORTH);
        add(output, BorderLayout.CENTER);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    public JComboBox<String> getCmbTable() { return cmbTable; }
    public JButton getBtnExport() { return btnExport; }
    public JButton getBtnImport() { return btnImport; }
    public JTextArea getTxtResult() { return txtResult; }
}
