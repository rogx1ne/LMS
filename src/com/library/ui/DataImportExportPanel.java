package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataImportExportPanel extends JPanel {
    private final ButtonGroup exportModeGroup = new ButtonGroup();
    private final JRadioButton rbExportAll = new JRadioButton("All Tables (ZIP)", false);
    private final JRadioButton rbExportSelective = new JRadioButton("Selective Table", true);
    
    private final ButtonGroup importModeGroup = new ButtonGroup();
    private final JRadioButton rbImportAll = new JRadioButton("All Tables (ZIP)", false);
    private final JRadioButton rbImportSelective = new JRadioButton("Selective Table", true);
    
    private final JComboBox<String> cmbTable = new JComboBox<>();
    private final JButton btnExport = new JButton("Export To Excel");
    private final JButton btnImport = new JButton("Import From Excel");
    private final JTextArea txtResult = new JTextArea(12, 20);

    public DataImportExportPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Setup radio button groups
        exportModeGroup.add(rbExportAll);
        exportModeGroup.add(rbExportSelective);
        importModeGroup.add(rbImportAll);
        importModeGroup.add(rbImportSelective);
        
        // Style components
        ModuleTheme.styleCombo(cmbTable);
        ModuleTheme.stylePrimaryButton(btnExport);
        ModuleTheme.styleAccentButton(btnImport);
        styleRadioButton(rbExportAll);
        styleRadioButton(rbExportSelective);
        styleRadioButton(rbImportAll);
        styleRadioButton(rbImportSelective);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(ModuleTheme.WHITE);
        controls.setBorder(ModuleTheme.sectionBorder("Data Import / Export"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Export Mode Row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        controls.add(label("Export Mode"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        controls.add(rbExportSelective, gbc);

        gbc.gridx = 2;
        controls.add(rbExportAll, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        controls.add(btnExport, gbc);

        // Import Mode Row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        controls.add(label("Import Mode"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        controls.add(rbImportSelective, gbc);

        gbc.gridx = 2;
        controls.add(rbImportAll, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        controls.add(btnImport, gbc);

        // Table Selection Row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        controls.add(label("Select Table"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        controls.add(cmbTable, gbc);

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

    private void styleRadioButton(JRadioButton rb) {
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rb.setBackground(ModuleTheme.WHITE);
        rb.setForeground(ModuleTheme.BLUE_DARK);
    }

    public JRadioButton getRbExportAll() { return rbExportAll; }
    public JRadioButton getRbExportSelective() { return rbExportSelective; }
    public JRadioButton getRbImportAll() { return rbImportAll; }
    public JRadioButton getRbImportSelective() { return rbImportSelective; }
    public JComboBox<String> getCmbTable() { return cmbTable; }
    public JButton getBtnExport() { return btnExport; }
    public JButton getBtnImport() { return btnImport; }
    public JTextArea getTxtResult() { return txtResult; }
}
