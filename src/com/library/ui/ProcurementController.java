package com.library.ui;

import com.library.dao.OrderDAO;
import com.library.dao.SellerDAO;
import com.library.model.OrderDetail;
import com.library.model.OrderHeader;
import com.library.model.OrderSummary;
import com.library.model.Seller;
import com.library.service.ProcurementEmailService;
import com.library.service.ProcurementPdfService;
import com.library.service.ProcurementValidationService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.io.File;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProcurementController {
    private final ProcurementModulePanel module;
    private final SellerPanel sellerPanel;
    private final OrderEntryPanel orderEntryPanel;
    private final OrderViewPanel orderViewPanel;

    private final SellerDAO sellerDAO = new SellerDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProcurementValidationService validationService = new ProcurementValidationService();
    private final ProcurementPdfService pdfService = new ProcurementPdfService();
    private final ProcurementEmailService emailService = new ProcurementEmailService();

    private OrderHeader lastOrderHeader;
    private Seller lastOrderSeller;
    private List<OrderDetail> lastOrderDetails = new ArrayList<>();
    private boolean sellerSchemaAvailable;
    private boolean orderSchemaAvailable;
    private boolean schemaWarningShown;

    public ProcurementController(ProcurementModulePanel module) {
        this.module = module;
        this.sellerPanel = module.getSellerPanel();
        this.orderEntryPanel = module.getOrderEntryPanel();
        this.orderViewPanel = module.getOrderViewPanel();

        bind();
        initState();
    }

    private void bind() {
        bindSellerActions();
        bindOrderEntryActions();
        bindOrderViewActions();
    }

    private void initState() {
        sellerSchemaAvailable = sellerDAO.isSellerSchemaAvailable();
        orderSchemaAvailable = orderDAO.isOrderSchemaAvailable();

        if (sellerSchemaAvailable) {
            refreshSellerPreviewId();
            refreshSellerTable();
            refreshSellerIdCombo();
        } else {
            sellerPanel.getTxtSellerId().setText("SCHEMA_MISSING");
            sellerPanel.getTableModel().setRowCount(0);
        }

        if (orderSchemaAvailable) {
            refreshOrderEntryDefaults();
            refreshOrderViewTable();
        } else {
            orderEntryPanel.getTxtOrderId().setText("SCHEMA_MISSING");
            orderEntryPanel.getTxtOrderDate().setText("");
            orderEntryPanel.getItemsModel().setRowCount(0);
            orderEntryPanel.getTxtReceiptPreview().setText(
                "Missing order tables. Run updated script.sql (or migration) to create TBL_ORDER_HEADER and TBL_ORDER_DETAILS."
            );
            orderViewPanel.getTableModel().setRowCount(0);
        }

        if (!sellerSchemaAvailable || !orderSchemaAvailable) {
            showSchemaWarningOnce();
        }
    }

    private void bindSellerActions() {
        sellerPanel.getBtnAdd().addActionListener(e -> addSeller());
        sellerPanel.getBtnUpdate().addActionListener(e -> updateSeller());
        sellerPanel.getBtnClear().addActionListener(e -> clearSellerFormAndFilters());
        sellerPanel.getBtnPdf().addActionListener(e -> exportSellerPdf());

        sellerPanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillSellerFormFromSelection();
        });

        DocumentListener dl = new SimpleDocumentListener(this::applySellerFilters);
        sellerPanel.getFltSellerId().getDocument().addDocumentListener(dl);
        sellerPanel.getFltCompany().getDocument().addDocumentListener(dl);
        sellerPanel.getFltContactPerson().getDocument().addDocumentListener(dl);
    }

    private void bindOrderEntryActions() {
        orderEntryPanel.getBtnAddItem().addActionListener(e -> addOrderItemToTempTable());
        orderEntryPanel.getBtnRemoveItem().addActionListener(e -> removeSelectedOrderItem());
        orderEntryPanel.getBtnPlaceOrder().addActionListener(e -> placeOrder());
        orderEntryPanel.getBtnDownloadReceipt().addActionListener(e -> downloadCurrentReceipt());
        orderEntryPanel.getBtnSendEmail().addActionListener(e -> sendCurrentReceiptByEmail());
    }

    private void bindOrderViewActions() {
        orderViewPanel.getBtnRefresh().addActionListener(e -> resetOrderFilters());
        orderViewPanel.getBtnDownloadPdf().addActionListener(e -> downloadSelectedOrderReceipt());
        orderViewPanel.getBtnUpdateOrder().addActionListener(e -> openOrderUpdateDialog());

        orderViewPanel.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int viewRow = orderViewPanel.getTable().rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                int modelRow = orderViewPanel.getTable().convertRowIndexToModel(viewRow);
                String orderId = String.valueOf(orderViewPanel.getTableModel().getValueAt(modelRow, 1));
                openOrderDetailsDialog(orderId);
            }
        });

        DocumentListener dl = new SimpleDocumentListener(this::applyOrderFilters);
        orderViewPanel.getFltOrderId().getDocument().addDocumentListener(dl);
        orderViewPanel.getFltOrderDate().getDocument().addDocumentListener(dl);
    }

    private void addSeller() {
        if (!ensureSellerSchema()) return;
        try {
            Seller raw = new Seller(
                sellerPanel.getTxtSellerId().getText().trim(),
                sellerPanel.getTxtCompanyName().getText(),
                sellerPanel.getTxtCompanyContact().getText(),
                sellerPanel.getTxtCompanyMail().getText(),
                sellerPanel.getTxtContactPerson().getText(),
                sellerPanel.getTxtContactNo().getText(),
                sellerPanel.getTxtContactMail().getText(),
                sellerPanel.getTxtAddress().getText()
            );
            Seller seller = validationService.validateSeller(raw);

            if (sellerDAO.addSeller(seller)) {
                JOptionPane.showMessageDialog(module, "Seller added successfully.");
                sellerPanel.clearForm();
                refreshSellerPreviewId();
                refreshSellerTable();
                refreshSellerIdCombo();
            } else {
                JOptionPane.showMessageDialog(module, "Unable to save seller.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(module, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSeller() {
        if (!ensureSellerSchema()) return;
        String id = sellerPanel.getTxtSellerId().getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(module, "Select a seller row first.");
            return;
        }

        try {
            Seller raw = new Seller(
                id,
                sellerPanel.getTxtCompanyName().getText(),
                sellerPanel.getTxtCompanyContact().getText(),
                sellerPanel.getTxtCompanyMail().getText(),
                sellerPanel.getTxtContactPerson().getText(),
                sellerPanel.getTxtContactNo().getText(),
                sellerPanel.getTxtContactMail().getText(),
                sellerPanel.getTxtAddress().getText()
            );
            Seller seller = validationService.validateSeller(raw);
            if (sellerDAO.updateSeller(seller)) {
                JOptionPane.showMessageDialog(module, "Seller updated.");
                refreshSellerTable();
                refreshSellerIdCombo();
            } else {
                JOptionPane.showMessageDialog(module, "No changes saved.");
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(module, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillSellerFormFromSelection() {
        int viewRow = sellerPanel.getTable().getSelectedRow();
        if (viewRow < 0) return;
        int row = sellerPanel.getTable().convertRowIndexToModel(viewRow);
        DefaultTableModel m = sellerPanel.getTableModel();

        sellerPanel.getTxtSellerId().setText(String.valueOf(m.getValueAt(row, 0)));
        sellerPanel.getTxtCompanyName().setText(String.valueOf(m.getValueAt(row, 1)));
        sellerPanel.getTxtCompanyContact().setText(String.valueOf(m.getValueAt(row, 2)));
        sellerPanel.getTxtCompanyMail().setText(String.valueOf(m.getValueAt(row, 3)));
        sellerPanel.getTxtContactPerson().setText(String.valueOf(m.getValueAt(row, 4)));
        sellerPanel.getTxtContactNo().setText(String.valueOf(m.getValueAt(row, 5)));
        sellerPanel.getTxtContactMail().setText(String.valueOf(m.getValueAt(row, 6)));
        sellerPanel.getTxtAddress().setText(String.valueOf(m.getValueAt(row, 7)));
    }

    private void refreshSellerPreviewId() {
        sellerPanel.getTxtSellerId().setText(sellerDAO.peekNextSellerId());
    }

    private void refreshSellerTable() {
        DefaultTableModel model = sellerPanel.getTableModel();
        model.setRowCount(0);
        for (Seller s : sellerDAO.getAllSellers()) {
            model.addRow(new Object[]{
                s.getSellerId(),
                s.getCompanyName(),
                s.getCompanyContactNo(),
                s.getCompanyMail(),
                s.getContactPerson(),
                s.getContactPersonNo(),
                s.getContactPersonMail(),
                s.getAddress()
            });
        }
        applySellerFilters();
    }

    private void applySellerFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        addRegexFilter(filters, sellerPanel.getFltSellerId().getText(), 0);
        addRegexFilter(filters, sellerPanel.getFltCompany().getText(), 1);
        addRegexFilter(filters, sellerPanel.getFltContactPerson().getText(), 4);
        sellerPanel.getSorter().setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void clearSellerFormAndFilters() {
        sellerPanel.clearForm();
        sellerPanel.getFltSellerId().setText("");
        sellerPanel.getFltCompany().setText("");
        sellerPanel.getFltContactPerson().setText("");
        refreshSellerPreviewId();
        applySellerFilters();
    }

    private void exportSellerPdf() {
        if (!ensureSellerSchema()) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("sellers.pdf"));
        if (chooser.showSaveDialog(module) != JFileChooser.APPROVE_OPTION) return;
        try {
            pdfService.exportTable(sellerPanel.getTable(), "Seller Master", chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(module, "Seller PDF downloaded.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(module, "PDF export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSellerIdCombo() {
        if (!sellerSchemaAvailable) {
            orderEntryPanel.getCmbSellerId().removeAllItems();
            return;
        }
        JComboBox<String> cmb = orderEntryPanel.getCmbSellerId();
        cmb.removeAllItems();
        for (String id : sellerDAO.getAllSellerIds()) cmb.addItem(id);
    }

    private void refreshOrderEntryDefaults() {
        if (!orderSchemaAvailable) return;
        orderEntryPanel.getTxtOrderId().setText(orderDAO.peekNextOrderId());
        orderEntryPanel.getTxtOrderDate().setText(String.valueOf(orderDAO.getCurrentDbDate()));
        orderEntryPanel.getItemsModel().setRowCount(0);
        orderEntryPanel.getTxtReceiptPreview().setText("");
        lastOrderHeader = null;
        lastOrderSeller = null;
        lastOrderDetails = new ArrayList<>();
    }

    private void addOrderItemToTempTable() {
        try {
            OrderDetail item = validationService.validateOrderDetailInput(
                orderEntryPanel.getTxtBookTitle().getText(),
                orderEntryPanel.getTxtAuthor().getText(),
                orderEntryPanel.getTxtPublication().getText(),
                orderEntryPanel.getTxtQty().getText()
            );
            orderEntryPanel.getItemsModel().addRow(new Object[]{
                item.getBookTitle(), item.getAuthor(), item.getPublication(), item.getQuantity()
            });
            orderEntryPanel.clearItemInput();
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(module, e.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedOrderItem() {
        int viewRow = orderEntryPanel.getItemsTable().getSelectedRow();
        if (viewRow < 0) return;
        int row = orderEntryPanel.getItemsTable().convertRowIndexToModel(viewRow);
        orderEntryPanel.getItemsModel().removeRow(row);
    }

    private void placeOrder() {
        if (!ensureSellerSchema() || !ensureOrderSchema()) return;
        String sellerId = String.valueOf(orderEntryPanel.getCmbSellerId().getSelectedItem());
        List<OrderDetail> details = readOrderDetails(orderEntryPanel.getItemsModel(), null);

        try {
            validationService.validateOrder(sellerId, details);
            OrderHeader created = orderDAO.createOrder(sellerId, orderDAO.getCurrentDbDate(), details);
            Seller seller = sellerDAO.getSellerById(sellerId);

            lastOrderHeader = created;
            lastOrderSeller = seller;
            lastOrderDetails = details;

            orderEntryPanel.getTxtReceiptPreview().setText(buildReceiptText(created, seller, details));
            JOptionPane.showMessageDialog(module, "Order placed successfully: " + created.getOrderId());

            refreshOrderEntryDefaults();
            orderEntryPanel.getTxtReceiptPreview().setText(buildReceiptText(created, seller, details));
            lastOrderHeader = created;
            lastOrderSeller = seller;
            lastOrderDetails = details;
            refreshOrderViewTable();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(module, ve.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(module, "Order save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildReceiptText(OrderHeader header, Seller seller, List<OrderDetail> details) {
        StringBuilder sb = new StringBuilder();
        sb.append("LIBRARY PROCUREMENT RECEIPT\n");
        sb.append("---------------------------------------------\n");
        sb.append("Order ID   : ").append(header.getOrderId()).append('\n');
        sb.append("Order Date : ").append(header.getOrderDate()).append('\n');
        sb.append("Seller ID  : ").append(header.getSellerId()).append('\n');
        sb.append("Company    : ").append(seller == null ? "" : seller.getCompanyName()).append('\n');
        sb.append("Company Mail: ").append(seller == null ? "" : seller.getCompanyMail()).append('\n');
        sb.append("Contact Mail: ").append(seller == null ? "" : seller.getContactPersonMail()).append("\n\n");

        int i = 1;
        for (OrderDetail d : details) {
            sb.append(i++).append(") ").append(d.getBookTitle())
              .append(" | ").append(d.getAuthor())
              .append(" | ").append(d.getPublication())
              .append(" | Qty: ").append(d.getQuantity())
              .append('\n');
        }
        return sb.toString();
    }

    private void downloadCurrentReceipt() {
        if (lastOrderHeader == null || lastOrderSeller == null || lastOrderDetails.isEmpty()) {
            JOptionPane.showMessageDialog(module, "No order receipt to download. Place an order first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(lastOrderHeader.getOrderId() + "_receipt.pdf"));
        if (chooser.showSaveDialog(module) != JFileChooser.APPROVE_OPTION) return;

        try {
            pdfService.generateOrderReceiptPdf(lastOrderHeader, lastOrderSeller, lastOrderDetails, chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(module, "Receipt PDF downloaded.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(module, "PDF generation failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendCurrentReceiptByEmail() {
        if (lastOrderHeader == null || lastOrderSeller == null || lastOrderDetails.isEmpty()) {
            JOptionPane.showMessageDialog(module, "No order receipt to email. Place an order first.");
            return;
        }

        try {
            Path tempPdf = File.createTempFile(lastOrderHeader.getOrderId() + "_receipt", ".pdf").toPath();
            pdfService.generateOrderReceiptPdf(lastOrderHeader, lastOrderSeller, lastOrderDetails, tempPdf);

            ProcurementEmailService.MailConfig cfg = new ProcurementEmailService.MailConfig(
                env("LMS_SMTP_HOST"),
                intEnv("LMS_SMTP_PORT", 587),
                env("LMS_SMTP_USER"),
                env("LMS_SMTP_PASS"),
                env("LMS_SMTP_FROM")
            );

            boolean sent = emailService.sendReceiptPdfToSeller(
                lastOrderSeller.getCompanyMail(),
                lastOrderSeller.getContactPersonMail(),
                "Order Receipt - " + lastOrderHeader.getOrderId(),
                "Please find attached order receipt.",
                tempPdf,
                cfg
            );

            if (sent) {
                JOptionPane.showMessageDialog(module, "Receipt email sent.");
            } else {
                JOptionPane.showMessageDialog(module, "Email method executed, but no SMTP send implementation is active yet.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(module, "Email failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshOrderViewTable() {
        if (!orderSchemaAvailable) return;
        DefaultTableModel model = orderViewPanel.getTableModel();
        model.setRowCount(0);

        int i = 1;
        for (OrderSummary s : orderDAO.getAllOrderSummaries()) {
            model.addRow(new Object[]{i++, s.getOrderId(), s.getOrderDate()});
        }
        applyOrderFilters();
    }

    private void applyOrderFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        addRegexFilter(filters, orderViewPanel.getFltOrderId().getText(), 1);
        addRegexFilter(filters, orderViewPanel.getFltOrderDate().getText(), 2);
        orderViewPanel.getSorter().setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void resetOrderFilters() {
        orderViewPanel.getFltOrderId().setText("");
        orderViewPanel.getFltOrderDate().setText("");
        applyOrderFilters();
    }

    private void openOrderDetailsDialog(String orderId) {
        if (!ensureOrderSchema()) return;
        OrderHeader header = orderDAO.getOrderHeaderById(orderId);
        if (header == null) return;

        Seller seller = sellerDAO.getSellerById(header.getSellerId());
        List<OrderDetail> details = orderDAO.getOrderDetails(orderId);

        Window owner = SwingUtilities.getWindowAncestor(module);
        String sellerInfo = seller == null ? "" : seller.getCompanyName();
        OrderDetailsDialog dialog = new OrderDetailsDialog(owner, header, sellerInfo, details);
        dialog.getBtnDownloadPdf().addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(orderId + "_receipt.pdf"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    pdfService.generateOrderReceiptPdf(header, seller, details, chooser.getSelectedFile().toPath());
                    JOptionPane.showMessageDialog(dialog, "Order PDF downloaded.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Download failed: " + ex.getMessage());
                }
            }
        });
        dialog.setVisible(true);
    }

    private void downloadSelectedOrderReceipt() {
        if (!ensureOrderSchema()) return;
        int viewRow = orderViewPanel.getTable().getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(module, "Select an order first.");
            return;
        }
        int row = orderViewPanel.getTable().convertRowIndexToModel(viewRow);
        String orderId = String.valueOf(orderViewPanel.getTableModel().getValueAt(row, 1));

        OrderHeader header = orderDAO.getOrderHeaderById(orderId);
        if (header == null) return;
        Seller seller = sellerDAO.getSellerById(header.getSellerId());
        List<OrderDetail> details = orderDAO.getOrderDetails(orderId);

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(orderId + "_receipt.pdf"));
        if (chooser.showSaveDialog(module) != JFileChooser.APPROVE_OPTION) return;

        try {
            pdfService.generateOrderReceiptPdf(header, seller, details, chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(module, "Order PDF downloaded.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(module, "Download failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openOrderUpdateDialog() {
        if (!ensureSellerSchema() || !ensureOrderSchema()) return;
        int viewRow = orderViewPanel.getTable().getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(module, "Select an order to update.");
            return;
        }

        int row = orderViewPanel.getTable().convertRowIndexToModel(viewRow);
        String orderId = String.valueOf(orderViewPanel.getTableModel().getValueAt(row, 1));

        OrderHeader header = orderDAO.getOrderHeaderById(orderId);
        if (header == null) return;
        List<OrderDetail> existingDetails = orderDAO.getOrderDetails(orderId);

        Window owner = SwingUtilities.getWindowAncestor(module);
        ProcOrderEditDialog dialog = new ProcOrderEditDialog(owner, header, sellerDAO.getAllSellerIds(), existingDetails);

        dialog.getBtnAddItem().addActionListener(e -> {
            try {
                OrderDetail item = validationService.validateOrderDetailInput(
                    dialog.getTxtTitle().getText(),
                    dialog.getTxtAuthor().getText(),
                    dialog.getTxtPublication().getText(),
                    dialog.getTxtQty().getText()
                );
                dialog.getModel().addRow(new Object[]{item.getBookTitle(), item.getAuthor(), item.getPublication(), item.getQuantity()});
                dialog.getTxtTitle().setText("");
                dialog.getTxtAuthor().setText("");
                dialog.getTxtPublication().setText("");
                dialog.getTxtQty().setText("");
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.getBtnRemoveItem().addActionListener(e -> {
            int vRow = dialog.getTable().getSelectedRow();
            if (vRow < 0) return;
            int mRow = dialog.getTable().convertRowIndexToModel(vRow);
            dialog.getModel().removeRow(mRow);
        });

        dialog.setVisible(true);
        if (!dialog.isSaved()) return;

        try {
            String sellerId = String.valueOf(dialog.getCmbSellerId().getSelectedItem());
            Date orderDate = Date.valueOf(LocalDate.parse(dialog.getTxtOrderDate().getText().trim()));
            List<OrderDetail> details = readOrderDetails(dialog.getModel(), orderId);

            validationService.validateOrder(sellerId, details);
            if (orderDAO.updateOrderReplaceDetails(orderId, sellerId, orderDate, details)) {
                JOptionPane.showMessageDialog(module, "Order updated successfully.");
                refreshOrderViewTable();
            } else {
                JOptionPane.showMessageDialog(module, "Order update failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(module, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<OrderDetail> readOrderDetails(DefaultTableModel model, String orderId) {
        List<OrderDetail> details = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            details.add(new OrderDetail(
                orderId,
                String.valueOf(model.getValueAt(i, 0)),
                String.valueOf(model.getValueAt(i, 1)),
                String.valueOf(model.getValueAt(i, 2)),
                Integer.parseInt(String.valueOf(model.getValueAt(i, 3)))
            ));
        }
        return details;
    }

    private void addRegexFilter(List<RowFilter<Object, Object>> filters, String raw, int col) {
        String t = raw == null ? "" : raw.trim();
        if (t.isEmpty()) return;
        filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(t), col));
    }

    private String env(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value;
    }

    private int intEnv(String key, int fallback) {
        try {
            return Integer.parseInt(env(key));
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean ensureSellerSchema() {
        if (sellerSchemaAvailable) return true;
        showSchemaWarningOnce();
        return false;
    }

    private boolean ensureOrderSchema() {
        if (orderSchemaAvailable) return true;
        showSchemaWarningOnce();
        return false;
    }

    private void showSchemaWarningOnce() {
        if (schemaWarningShown) return;
        schemaWarningShown = true;

        StringBuilder sb = new StringBuilder("Procurement module schema mismatch detected.\n");
        if (!sellerSchemaAvailable) sb.append("- Missing/old TBL_SELLER structure\n");
        if (!orderSchemaAvailable) sb.append("- Missing TBL_ORDER_HEADER / TBL_ORDER_DETAILS\n");
        sb.append("\nRun updated script.sql as PRJ2531H and restart the app.");
        JOptionPane.showMessageDialog(module, sb.toString(), "Database Schema Required", JOptionPane.WARNING_MESSAGE);
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;
        SimpleDocumentListener(Runnable callback) { this.callback = callback; }
        @Override public void insertUpdate(DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
