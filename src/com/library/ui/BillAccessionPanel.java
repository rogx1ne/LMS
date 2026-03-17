package com.library.ui;

import com.library.dao.BillDAO;
import com.library.dao.BookDAO;
import com.library.dao.OrderDAO;
import com.library.model.BillItem;
import com.library.model.BookCopy;
import com.library.model.OrderDetail;
import com.library.model.OrderHeader;
import com.library.service.ExcelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BillAccessionPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private final BillDAO billDAO = new BillDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final ExcelService excelService = new ExcelService();

    private final JTextField txtOrderId = new JTextField();
    private final JButton btnSearch = new JButton("Search Order/Bill");
    private final JButton btnUploadExcel = new JButton("Upload Excel Bill");

    private final DefaultTableModel tableModel;
    private final JTable itemsTable;

    private final JButton btnProcess = new JButton("Add Books to Accession Register");

    public BillAccessionPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- TOP BAR ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Bill Lookup"));

        JLabel lblOrder = new JLabel("Order/Bill ID:");
        lblOrder.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOrder.setForeground(ModuleTheme.BLUE_DARK);

        ModuleTheme.styleInput(txtOrderId);
        ModuleTheme.stylePrimaryButton(btnSearch);
        ModuleTheme.styleAccentButton(btnUploadExcel);

        top.add(lblOrder);
        top.add(txtOrderId);
        top.add(btnSearch);
        top.add(new JSeparator(JSeparator.VERTICAL));
        top.add(btnUploadExcel);

        // --- TABLE ---
        String[] cols = {
            "Title", "Author", "Qty", "Cost",
            "Edition", "Pub Year", "Pages", "Publisher", "Pub Place", "Class No", "Book No",
            "Subject", "Course", "Year", "Type"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c >= 3;
            }
        };
        itemsTable = new JTable(tableModel);
        ModuleTheme.styleTable(itemsTable);
        JScrollPane scroll = new JScrollPane(itemsTable);
        scroll.setBorder(ModuleTheme.sectionBorder("Bill Items (Review & Complete Details)"));

        // --- BOTTOM ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnProcess);
        bottom.add(btnProcess);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // --- ACTIONS ---
        btnSearch.addActionListener(e -> searchOrderOrBill());
        btnUploadExcel.addActionListener(e -> uploadExcel());
        btnProcess.addActionListener(e -> processToAccession());
    }

    private void searchOrderOrBill() {
        String oid = txtOrderId.getText().trim();
        if (oid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Order/Bill ID.", "Missing ID", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Try TBL_BILL (Official Received items)
        if (billDAO.billExists(oid)) {
            List<BillItem> billItems = billDAO.getBillItems(oid);
            populateTableFromBill(billItems);
            return;
        }

        // 2. Try TBL_ORDER_HEADER (Order Intent)
        OrderHeader header = orderDAO.getOrderHeaderById(oid);
        if (header != null) {
            List<OrderDetail> details = orderDAO.getOrderDetails(oid);
            populateTableFromDetails(details);
            return;
        }

        JOptionPane.showMessageDialog(this, "No Official Bill or Order found with this ID.", "Not Found", JOptionPane.ERROR_MESSAGE);
    }

    private void populateTableFromBill(List<BillItem> items) {
        tableModel.setRowCount(0);
        for (BillItem b : items) {
            tableModel.addRow(new Object[]{
                b.getTitle(), b.getAuthor(), b.getQuantity(), b.getUnitPrice(),
                "1", "", "", "", "", "", "",
                "", "", "", "BOOK"
            });
        }
    }

    private void populateTableFromDetails(List<OrderDetail> details) {
        tableModel.setRowCount(0);
        for (OrderDetail d : details) {
            tableModel.addRow(new Object[]{
                d.getBookTitle(), d.getAuthor(), d.getQuantity(), "",
                "1", "", "", d.getPublication(), "", "", "",
                "", "", "", "BOOK"
            });
        }
    }

    private void uploadExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Bill Excel");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                List<Map<String, String>> rows = excelService.readRows(file.toPath());
                populateTableFromExcel(rows);
                txtOrderId.setText(file.getName()); 
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to read Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateTableFromExcel(List<Map<String, String>> rows) {
        tableModel.setRowCount(0);
        for (Map<String, String> row : rows) {
            String title = row.getOrDefault("Title", row.getOrDefault("TITLE", ""));
            String author = row.getOrDefault("Author", row.getOrDefault("AUTHOR", ""));
            String qty = row.getOrDefault("Qty", row.getOrDefault("Quantity", "1"));
            String pub = row.getOrDefault("Publisher", row.getOrDefault("PUBLISHER", ""));

            tableModel.addRow(new Object[]{
                title, author, qty, "",
                "1", "", "", pub, "", "", "",
                "", "", "", "BOOK"
            });
        }
    }

    private void processToAccession() {
        if (itemsTable.isEditing()) {
            itemsTable.getCellEditor().stopCellEditing();
        }

        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "No items to process.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<BookCopy> toInsert = new ArrayList<>();
        String billNo = txtOrderId.getText().trim();
        Date billDate = new Date(System.currentTimeMillis());

        try {
            for (int i = 0; i < rowCount; i++) {
                String title = (String) tableModel.getValueAt(i, 0);
                String author = (String) tableModel.getValueAt(i, 1);
                int qty = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                BigDecimal cost = new BigDecimal(tableModel.getValueAt(i, 3).toString().isEmpty() ? "0" : tableModel.getValueAt(i, 3).toString());

                int edition = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                int year = Integer.parseInt(tableModel.getValueAt(i, 5).toString().isEmpty() ? "0" : tableModel.getValueAt(i, 5).toString());
                int pages = Integer.parseInt(tableModel.getValueAt(i, 6).toString().isEmpty() ? "0" : tableModel.getValueAt(i, 6).toString());
                String publisher = (String) tableModel.getValueAt(i, 7);
                String pubPlace = (String) tableModel.getValueAt(i, 8);
                String classNo = (String) tableModel.getValueAt(i, 9);
                String bookNo = (String) tableModel.getValueAt(i, 10);

                String subject = (String) tableModel.getValueAt(i, 11);
                String course = (String) tableModel.getValueAt(i, 12);
                String yearTag = (String) tableModel.getValueAt(i, 13);
                String type = (String) tableModel.getValueAt(i, 14);

                if (classNo.trim().isEmpty() || bookNo.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Class No and Book No are mandatory for row " + (i + 1), "Validation", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // --- VERIFY AGAINST TBL_BILL ---
                if (billDAO.billExists(billNo)) {
                    int remaining = billDAO.getRemainingQuantity(billNo, title, author);
                    if (remaining == -1) {
                         JOptionPane.showMessageDialog(this, "Item '" + title + "' is NOT on official Bill " + billNo, "Verification Error", JOptionPane.ERROR_MESSAGE);
                         return;
                    }
                    if (qty > remaining) {
                         JOptionPane.showMessageDialog(this, "Requested quantity (" + qty + ") exceeds remaining quantity on Bill (" + remaining + ") for '" + title + "'.", "Verification Error", JOptionPane.ERROR_MESSAGE);
                         return;
                    }
                }

                for (int q = 0; q < qty; q++) {
                    toInsert.add(new BookCopy(
                        null, 
                        author, title, null, edition, publisher, pubPlace,
                        year, pages, "PURCHASED", classNo, bookNo, cost,
                        billNo, billDate, 
                        subject, course, yearTag, type,
                        null, "Added via Bill Import", "ACTIVE"
                    ));
                }
            }

            List<BookCopy> saved = bookDAO.addBookCopies(toInsert);
            JOptionPane.showMessageDialog(this, "Successfully added " + saved.size() + " book copies to the Accession Register.", "Success", JOptionPane.INFORMATION_MESSAGE);
            tableModel.setRowCount(0);
            txtOrderId.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format in numeric fields.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
