package com.library.ui;

import com.library.dao.BillDAO;
import com.library.dao.SellerDAO;
import com.library.model.BillItem;
import com.library.model.Seller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class BillEntryPanel extends JPanel {
    private final BillDAO billDAO = new BillDAO();
    private final SellerDAO sellerDAO = new SellerDAO();

    private final JTextField txtBillNo = new JTextField();
    private final JComboBox<String> cmbSeller = new JComboBox<>();
    private final JTextField txtBillDate = new JTextField();
    private final JTextField txtTax = new JTextField("0");
    private final JTextField txtGrandTotal = new JTextField();

    private final JTextField txtItemTitle = new JTextField();
    private final JTextField txtItemAuthor = new JTextField();
    private final JTextField txtItemQty = new JTextField();
    private final JTextField txtItemPrice = new JTextField();
    private final JButton btnAddItem = new JButton("Add Item");
    private final JButton btnRemoveItem = new JButton("Remove Selected");

    private final DefaultTableModel tableModel;
    private final JTable itemsTable;

    private final JButton btnSaveBill = new JButton("Save Official Bill");

    public BillEntryPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- HEADER ---
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(ModuleTheme.WHITE);
        header.setBorder(ModuleTheme.sectionBorder("Bill Header Information"));
        styleInputs();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addLabel(header, gbc, "Bill ID / Invoice No:", 0, 0);
        gbc.gridx = 1; header.add(txtBillNo, gbc);
        addLabel(header, gbc, "Seller:", 2, 0);
        gbc.gridx = 3; header.add(cmbSeller, gbc);

        addLabel(header, gbc, "Bill Date:", 0, 1);
        gbc.gridx = 1; header.add(txtBillDate, gbc);
        addLabel(header, gbc, "Tax (%):", 2, 1);
        gbc.gridx = 3; header.add(txtTax, gbc);

        addLabel(header, gbc, "Grand Total (Rs):", 0, 2);
        gbc.gridx = 1; header.add(txtGrandTotal, gbc);

        // --- ITEM ENTRY ---
        JPanel itemEntry = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        itemEntry.setBackground(ModuleTheme.WHITE);
        itemEntry.setBorder(ModuleTheme.sectionBorder("Add Bill Items"));
        
        itemEntry.add(new JLabel("Title:"));
        itemEntry.add(txtItemTitle);
        itemEntry.add(new JLabel("Author:"));
        itemEntry.add(txtItemAuthor);
        itemEntry.add(new JLabel("Qty:"));
        itemEntry.add(txtItemQty);
        itemEntry.add(new JLabel("Unit Price:"));
        itemEntry.add(txtItemPrice);
        
        ModuleTheme.stylePrimaryButton(btnAddItem);
        ModuleTheme.styleSubtleButton(btnRemoveItem);
        itemEntry.add(btnAddItem);
        itemEntry.add(btnRemoveItem);

        // --- TABLE ---
        String[] cols = {"Title", "Author", "Qty", "Unit Price", "Total"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(tableModel);
        ModuleTheme.styleTable(itemsTable);
        JScrollPane scroll = new JScrollPane(itemsTable);

        // --- BOTTOM ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnSaveBill);
        bottom.add(btnSaveBill);

        JPanel center = new JPanel(new BorderLayout());
        center.add(itemEntry, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadSellers();
        
        // --- ACTIONS ---
        btnAddItem.addActionListener(e -> addItem());
        btnRemoveItem.addActionListener(e -> removeSelected());
        btnSaveBill.addActionListener(e -> saveBill());
    }

    private void styleInputs() {
        ModuleTheme.styleInput(txtBillNo);
        ModuleTheme.styleCombo(cmbSeller);
        ModuleTheme.styleInput(txtBillDate);
        ModuleTheme.styleInput(txtTax);
        ModuleTheme.styleInput(txtGrandTotal);
        ModuleTheme.addDatePicker(txtBillDate);

        ModuleTheme.styleInput(txtItemTitle);
        ModuleTheme.styleInput(txtItemAuthor);
        ModuleTheme.styleInput(txtItemQty);
        ModuleTheme.styleInput(txtItemPrice);

        txtItemQty.setPreferredSize(new Dimension(50, 30));
        txtItemPrice.setPreferredSize(new Dimension(80, 30));
        txtItemTitle.setPreferredSize(new Dimension(150, 30));
    }

    private void addLabel(JPanel p, GridBagConstraints gbc, String txt, int x, int y) {
        gbc.gridx = x; gbc.gridy = y; gbc.weightx = 0;
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(l, gbc);
    }

    private void loadSellers() {
        cmbSeller.removeAllItems();
        for (Seller s : sellerDAO.getAllSellers()) {
            cmbSeller.addItem(s.getSellerId());
        }
    }

    private void addItem() {
        try {
            String title = txtItemTitle.getText().trim();
            String author = txtItemAuthor.getText().trim();
            int qty = Integer.parseInt(txtItemQty.getText().trim());
            BigDecimal price = new BigDecimal(txtItemPrice.getText().trim());
            
            if (title.isEmpty() || author.isEmpty()) throw new Exception("Title/Author required");
            
            BigDecimal total = price.multiply(new BigDecimal(qty));
            tableModel.addRow(new Object[]{title, author, qty, price, total});
            
            txtItemTitle.setText("");
            txtItemAuthor.setText("");
            txtItemQty.setText("");
            txtItemPrice.setText("");
            autoCalculateGrandTotal();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Check item fields: " + e.getMessage());
        }
    }

    private void removeSelected() {
        int r = itemsTable.getSelectedRow();
        if (r >= 0) {
            tableModel.removeRow(r);
            autoCalculateGrandTotal();
        }
    }

    private void autoCalculateGrandTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total = total.add((BigDecimal) tableModel.getValueAt(i, 4));
        }
        int taxPercent = 0;
        try { taxPercent = Integer.parseInt(txtTax.getText().trim()); } catch (Exception ignored) {}
        
        BigDecimal taxAmt = total.multiply(new BigDecimal(taxPercent)).divide(new BigDecimal(100));
        txtGrandTotal.setText(total.add(taxAmt).setScale(2, RoundingMode.HALF_UP).toString());
    }

    private void saveBill() {
        try {
            String bid = txtBillNo.getText().trim();
            String sid = (String) cmbSeller.getSelectedItem();
            Date dt = Date.valueOf(txtBillDate.getText().trim());
            int tax = Integer.parseInt(txtTax.getText().trim());
            BigDecimal gt = new BigDecimal(txtGrandTotal.getText().trim());

            if (bid.isEmpty() || sid == null) throw new Exception("Bill ID and Seller are required");
            if (tableModel.getRowCount() == 0) throw new Exception("No items in bill");

            List<BillItem> items = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                items.add(new BillItem(
                    bid, sid,
                    (String) tableModel.getValueAt(i, 0),
                    (String) tableModel.getValueAt(i, 1),
                    (Integer) tableModel.getValueAt(i, 2),
                    (BigDecimal) tableModel.getValueAt(i, 3),
                    dt, tax,
                    (BigDecimal) tableModel.getValueAt(i, 4),
                    gt
                ));
            }

            billDAO.createBill(items);
            JOptionPane.showMessageDialog(this, "Bill saved successfully.");
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving bill: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtBillNo.setText("");
        txtBillDate.setText("");
        txtTax.setText("0");
        txtGrandTotal.setText("");
        tableModel.setRowCount(0);
    }
}
