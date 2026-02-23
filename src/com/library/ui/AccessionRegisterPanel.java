package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class AccessionRegisterPanel extends JPanel {
    private final JTextField fltAccessNo = new JTextField();
    private final JTextField fltAuthor = new JTextField();
    private final JTextField fltTitle = new JTextField();
    private final JTextField fltVolume = new JTextField();
    private final JTextField fltPublisher = new JTextField();
    private final JTextField fltYear = new JTextField();
    private final JComboBox<String> fltSource = new JComboBox<>(new String[]{"All", "PURCHASE", "DONATION", "GIFT", "EXCHANGE"});
    private final JTextField fltBillNo = new JTextField();

    private final JButton btnRefresh = new JButton("Reset");
    private final JButton btnEdit = new JButton("Edit / Update");
    private final JButton btnPdf = new JButton("Download PDF");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel lblTotal = new JLabel("Total Records: 0");

    public AccessionRegisterPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(ModuleTheme.WHITE);

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setBackground(ModuleTheme.WHITE);
        filters.setBorder(ModuleTheme.sectionBorder("Search & Filters"));

        styleFilters();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFilter(filters, gbc, "Accession No:", fltAccessNo, 0, 0);
        addFilter(filters, gbc, "Author:", fltAuthor, 2, 0);
        addFilter(filters, gbc, "Book Title:", fltTitle, 4, 0);
        addFilter(filters, gbc, "Volume:", fltVolume, 6, 0);

        addFilter(filters, gbc, "Publisher:", fltPublisher, 0, 1);
        addFilter(filters, gbc, "Pub Year:", fltYear, 2, 1);
        addFilter(filters, gbc, "Source:", fltSource, 4, 1);
        addFilter(filters, gbc, "Bill No:", fltBillNo, 6, 1);

        gbc.gridx = 7;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        ModuleTheme.styleSubtleButton(btnRefresh);
        filters.add(btnRefresh, gbc);

        String[] cols = {
            "Accession No", "Author Name", "Book Title", "Volume", "Edition", "Publisher", "Publisher Place",
            "Pub Year", "Pages", "Source", "Class No", "Book No", "Cost", "Bill No", "Bill Date",
            "Withdrawn Date", "Remarks", "Status"
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

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(ModuleTheme.WHITE);
        lblTotal.setForeground(ModuleTheme.BLACK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleAccentButton(btnEdit);
        ModuleTheme.stylePrimaryButton(btnPdf);
        actions.add(btnEdit);
        actions.add(btnPdf);

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);

        add(filters, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void styleFilters() {
        ModuleTheme.styleInput(fltAccessNo);
        ModuleTheme.styleInput(fltAuthor);
        ModuleTheme.styleInput(fltTitle);
        ModuleTheme.styleInput(fltVolume);
        ModuleTheme.styleInput(fltPublisher);
        ModuleTheme.styleInput(fltYear);
        ModuleTheme.styleInput(fltBillNo);
        ModuleTheme.styleCombo(fltSource);
    }

    private void addFilter(JPanel panel, GridBagConstraints gbc, String label, JComponent cmp, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;
        panel.add(cmp, gbc);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(ModuleTheme.BLACK);
        return l;
    }

    public void updateTotalCount(int count) {
        lblTotal.setText("Total Records: " + count);
    }

    public JTextField getFltAccessNo() { return fltAccessNo; }
    public JTextField getFltAuthor() { return fltAuthor; }
    public JTextField getFltTitle() { return fltTitle; }
    public JTextField getFltVolume() { return fltVolume; }
    public JTextField getFltPublisher() { return fltPublisher; }
    public JTextField getFltYear() { return fltYear; }
    public JComboBox<String> getFltSource() { return fltSource; }
    public JTextField getFltBillNo() { return fltBillNo; }

    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnEdit() { return btnEdit; }
    public JButton getBtnPdf() { return btnPdf; }

    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }
}
