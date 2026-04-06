package com.library.ui;

import com.library.dao.BillDAO;
import com.library.model.BillItem;
import com.library.service.PdfReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class BillReportPanel extends JPanel {
    private final BillDAO billDAO = new BillDAO();
    private final PdfReportService reportService = new PdfReportService();

    private final JTable table;
    private final DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtFilter = new JTextField(20);

    public BillReportPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- TOP BAR ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Search Official Bills"));

        JLabel lblSearch = new JLabel("Filter Bills:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ModuleTheme.styleInput(txtFilter);

        JButton btnRefresh = new JButton("Refresh");
        ModuleTheme.stylePrimaryButton(btnRefresh);

        JButton btnPdf = new JButton("Export Report");
        ModuleTheme.styleAccentButton(btnPdf);

        top.add(lblSearch);
        top.add(txtFilter);
        top.add(btnRefresh);
        top.add(btnPdf);

        // --- TABLE ---
        String[] cols = {"Bill ID", "Seller ID", "Title", "Author", "Qty", "Unit Price", "Date", "Tax (%)", "Total Amount", "Grand Total"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ModuleTheme.styleTable(table);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(ModuleTheme.createEmptyStateLayer(
            table,
            () -> txtFilter.getText().trim().isEmpty() ? "No Bills available." : "Record Not Found"
        ));
        scroll.getViewport().setBackground(ModuleTheme.TABLE_BG);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // --- ACTIONS ---
        btnRefresh.addActionListener(e -> refreshData());
        btnPdf.addActionListener(e -> reportService.exportToPdf(this, table, "Official Bill Report", "Official_Bills"));

        txtFilter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        refreshData();
    }

    private void applyFilter() {
        String t = txtFilter.getText().trim();
        if (t.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(t)));
    }

    public void refreshData() {
        model.setRowCount(0);
        List<BillItem> items = billDAO.getAllBills();
        for (BillItem b : items) {
            model.addRow(new Object[]{
                b.getBillId(),
                b.getSellerId(),
                b.getTitle(),
                b.getAuthor(),
                b.getQuantity(),
                b.getUnitPrice(),
                b.getBillDate(),
                b.getTax(),
                b.getTotalAmount(),
                b.getGrandTotal()
            });
        }
    }
}
