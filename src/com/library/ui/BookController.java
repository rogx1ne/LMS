package com.library.ui;

import com.library.dao.BookDAO;
import com.library.model.BookCopy;
import com.library.model.BookCopyStatusRow;
import com.library.model.BookStockItem;
import com.library.service.BookLogic;
import com.library.service.BookPdfService;
import com.library.service.CurrentUserContext;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BookController {
    private static final int LOW_STOCK_THRESHOLD = 2;

    private final BookModulePanel module;
    private final AddBookPanel addPanel;
    private final AccessionRegisterPanel registerPanel;
    private final StockPanel stockPanel;

    private final BookDAO dao = new BookDAO();
    private final BookPdfService pdfService = new BookPdfService();

    public BookController(BookModulePanel module) {
        this.module = module;
        this.addPanel = module.getAddBookPanel();
        this.registerPanel = module.getAccessionRegisterPanel();
        this.stockPanel = module.getStockPanel();

        bindEvents();
        refreshAll();
    }

    private void bindEvents() {
        addPanel.getBtnSave().addActionListener(e -> saveNewBookCopy());
        addPanel.getBtnReset().addActionListener(e -> {
            addPanel.clearForm();
            refreshAccessionPreview();
        });

        addPanel.getTxtAuthor().getDocument().addDocumentListener(new SimpleDocumentListener(this::syncBookNoFromAuthor));
        addPanel.getTxtWithdrawnDate().getDocument().addDocumentListener(new SimpleDocumentListener(this::syncStatusFromWithdrawnDate));

        registerPanel.getBtnRefresh().addActionListener(e -> resetFilters());
        registerPanel.getBtnEdit().addActionListener(e -> openUpdateDialogForSelected());
        registerPanel.getBtnPdf().addActionListener(e -> exportAccessionGridPdf());

        attachFilterListeners();

        stockPanel.getBtnRefresh().addActionListener(e -> refreshStockTable(true));
        stockPanel.getBtnCheckAlerts().addActionListener(e -> refreshStockTable(true));

        stockPanel.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int viewRow = stockPanel.getTable().rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                int row = stockPanel.getTable().convertRowIndexToModel(viewRow);
                openStockDrillDown(row);
            }
        });
    }

    private void refreshAll() {
        refreshAccessionPreview();
        refreshAccessionTable();
        refreshStockTable(true);
    }

    private void refreshAccessionPreview() {
        addPanel.setAccessionPreview(dao.peekNextAccessionNo());
    }

    private void saveNewBookCopy() {
        try {
            BookCopy draft = buildBookFromAddPanel();
            BookCopy created = dao.addBookCopy(draft);
            JOptionPane.showMessageDialog(module, "Book copy saved. Accession No: " + created.getAccessionNo());

            addPanel.clearForm();
            refreshAccessionPreview();
            refreshAccessionTable();
            refreshStockTable(false);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(module, ve.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(module, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BookCopy buildBookFromAddPanel() throws ValidationException {
        String author = BookLogic.normalizeAuthorName(addPanel.getTxtAuthor().getText());
        String title = BookLogic.normalizeTitle(addPanel.getTxtTitle().getText());
        Integer volume = BookLogic.validateVolumeNullable(addPanel.getTxtVolume().getText());
        int edition = BookLogic.validateEdition(addPanel.getTxtEdition().getText());
        String publisher = BookLogic.normalizePublisher(addPanel.getTxtPublisher().getText());
        String pubPlace = BookLogic.normalizePublicationPlace(addPanel.getTxtPubPlace().getText());
        int pubYear = BookLogic.validatePublicationYear(addPanel.getTxtPubYear().getText());
        int pages = BookLogic.validatePages(addPanel.getTxtPages().getText());
        String source = BookLogic.normalizeSource(String.valueOf(addPanel.getCmbSource().getSelectedItem()));
        String classNo = BookLogic.normalizeClassNo(addPanel.getTxtClassNo().getText());
        String bookNo = BookLogic.generateBookNo(author);
        BigDecimal cost = BookLogic.validateCost(addPanel.getTxtCost().getText());
        String billNo = BookLogic.normalizeBillNo(addPanel.getTxtBillNo().getText());

        Date billDate = parseDateNullable(addPanel.getTxtBillDate().getText(), "Bill Date");
        Date withdrawnDate = parseDateNullable(addPanel.getTxtWithdrawnDate().getText(), "Withdrawn Date");
        String remarks = BookLogic.normalizeRemarks(addPanel.getTxtRemarks().getText());
        String status = BookLogic.deriveStatus(withdrawnDate == null ? null : withdrawnDate.toLocalDate());

        return new BookCopy(
            null,
            author,
            title,
            volume,
            edition,
            publisher,
            pubPlace,
            pubYear,
            pages,
            source,
            classNo,
            bookNo,
            cost,
            billNo,
            billDate,
            withdrawnDate,
            remarks,
            status
        );
    }

    private void refreshAccessionTable() {
        DefaultTableModel model = registerPanel.getTableModel();
        model.setRowCount(0);

        for (BookCopy b : dao.getAllBookCopies()) {
            model.addRow(new Object[]{
                b.getAccessionNo(),
                b.getAuthorName(),
                b.getTitle(),
                b.getVolume(),
                b.getEdition(),
                b.getPublisher(),
                b.getPublicationPlace(),
                b.getPublicationYear(),
                b.getPages(),
                b.getSource(),
                b.getClassNo(),
                b.getBookNo(),
                b.getCost(),
                b.getBillNo(),
                b.getBillDate(),
                b.getWithdrawnDate(),
                b.getRemarks(),
                b.getStatus()
            });
        }
        applyFilters();
    }

    private void openUpdateDialogForSelected() {
        int selectedViewRow = registerPanel.getTable().getSelectedRow();
        if (selectedViewRow < 0) {
            JOptionPane.showMessageDialog(module, "Please select a row first.");
            return;
        }

        int modelRow = registerPanel.getTable().convertRowIndexToModel(selectedViewRow);
        String accessionNo = String.valueOf(registerPanel.getTableModel().getValueAt(modelRow, 0));

        BookCopy existing = dao.getBookByAccessionNo(accessionNo);
        if (existing == null) {
            JOptionPane.showMessageDialog(module, "Book copy not found.");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(module);
        BookEditDialog dialog = new BookEditDialog(owner, existing);
        dialog.setVisible(true);

        if (!dialog.isSaved()) return;

        try {
            BookCopy updated = buildBookFromEditDialog(dialog);
            if (dao.updateBookCopy(updated)) {
                JOptionPane.showMessageDialog(module, "Updated successfully.");
                refreshAccessionTable();
                refreshStockTable(false);
            } else {
                JOptionPane.showMessageDialog(module, "No changes saved.");
            }
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(module, ve.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BookCopy buildBookFromEditDialog(BookEditDialog d) throws ValidationException {
        String accession = d.getAccessNo();
        String author = BookLogic.normalizeAuthorName(d.getAuthor());
        String title = BookLogic.normalizeTitle(d.getTitle());
        Integer volume = BookLogic.validateVolumeNullable(d.getVolume());
        int edition = BookLogic.validateEdition(d.getEdition());
        String publisher = BookLogic.normalizePublisher(d.getPublisher());
        String pubPlace = BookLogic.normalizePublicationPlace(d.getPubPlace());
        int pubYear = BookLogic.validatePublicationYear(d.getPubYear());
        int pages = BookLogic.validatePages(d.getPages());
        String source = BookLogic.normalizeSource(d.getSource());
        String classNo = BookLogic.normalizeClassNo(d.getClassNo());
        String bookNo = d.getBookNo().isEmpty() ? BookLogic.generateBookNo(author) : d.getBookNo().toUpperCase();
        BigDecimal cost = BookLogic.validateCost(d.getCost());
        String billNo = BookLogic.normalizeBillNo(d.getBillNo());
        Date billDate = parseDateNullable(d.getBillDate(), "Bill Date");
        Date withdrawnDate = parseDateNullable(d.getWithdrawn(), "Withdrawn Date");
        String remarks = BookLogic.normalizeRemarks(d.getRemarks());

        String status = d.getStatus();
        if (withdrawnDate != null && "ACTIVE".equals(status)) {
            status = BookLogic.STATUS_WITHDRAWN;
        }

        return new BookCopy(
            accession,
            author,
            title,
            volume,
            edition,
            publisher,
            pubPlace,
            pubYear,
            pages,
            source,
            classNo,
            bookNo,
            cost,
            billNo,
            billDate,
            withdrawnDate,
            remarks,
            status
        );
    }

    private void exportAccessionGridPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("accession_register.pdf"));
        if (chooser.showSaveDialog(module) != JFileChooser.APPROVE_OPTION) return;

        try {
            pdfService.exportTable(registerPanel.getTable(), "Accession Register", chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(module, "PDF exported.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(module, "PDF export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStockTable(boolean checkAlerts) {
        DefaultTableModel model = stockPanel.getTableModel();
        model.setRowCount(0);

        List<BookStockItem> items = dao.getStockSummary();
        for (BookStockItem item : items) {
            model.addRow(new Object[]{
                item.getTitle(),
                item.getAuthorName(),
                item.getEdition(),
                item.getPublication(),
                item.getQuantity()
            });
        }

        if (checkAlerts) {
            List<BookStockItem> low = dao.checkAndLogLowStock(LOW_STOCK_THRESHOLD, CurrentUserContext.getUserId());
            if (low.isEmpty()) {
                stockPanel.getLblAlert().setText("Stock level is healthy.");
            } else {
                stockPanel.getLblAlert().setText("Low stock alert for " + low.size() + " title(s). Threshold: " + LOW_STOCK_THRESHOLD);
                JOptionPane.showMessageDialog(module,
                    "Low stock alert generated for " + low.size() + " title(s). Alert logged for admin.",
                    "Low Stock",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void openStockDrillDown(int modelRow) {
        String title = String.valueOf(stockPanel.getTableModel().getValueAt(modelRow, 0));
        String author = String.valueOf(stockPanel.getTableModel().getValueAt(modelRow, 1));
        int edition = Integer.parseInt(String.valueOf(stockPanel.getTableModel().getValueAt(modelRow, 2)));

        List<BookCopyStatusRow> rows = dao.getStockDrillDown(title, author, edition);
        Window owner = SwingUtilities.getWindowAncestor(module);
        BookDrillDownDialog d = new BookDrillDownDialog(owner, title, author, edition, rows);
        d.setVisible(true);
    }

    private void attachFilterListeners() {
        DocumentListener dl = new SimpleDocumentListener(this::applyFilters);
        registerPanel.getFltAccessNo().getDocument().addDocumentListener(dl);
        registerPanel.getFltAuthor().getDocument().addDocumentListener(dl);
        registerPanel.getFltTitle().getDocument().addDocumentListener(dl);
        registerPanel.getFltVolume().getDocument().addDocumentListener(dl);
        registerPanel.getFltPublisher().getDocument().addDocumentListener(dl);
        registerPanel.getFltYear().getDocument().addDocumentListener(dl);
        registerPanel.getFltBillNo().getDocument().addDocumentListener(dl);
        registerPanel.getFltSource().addActionListener(e -> applyFilters());
    }

    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        addRegexFilter(filters, registerPanel.getFltAccessNo().getText(), 0);
        addRegexFilter(filters, registerPanel.getFltAuthor().getText(), 1);
        addRegexFilter(filters, registerPanel.getFltTitle().getText(), 2);
        addRegexFilter(filters, registerPanel.getFltVolume().getText(), 3);
        addRegexFilter(filters, registerPanel.getFltPublisher().getText(), 5);
        addRegexFilter(filters, registerPanel.getFltYear().getText(), 7);
        addRegexFilter(filters, registerPanel.getFltBillNo().getText(), 13);

        String source = String.valueOf(registerPanel.getFltSource().getSelectedItem());
        if (!"All".equals(source)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(source) + "$", 9));
        }

        registerPanel.getSorter().setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        registerPanel.updateTotalCount(registerPanel.getTable().getRowCount());
    }

    private void resetFilters() {
        registerPanel.getFltAccessNo().setText("");
        registerPanel.getFltAuthor().setText("");
        registerPanel.getFltTitle().setText("");
        registerPanel.getFltVolume().setText("");
        registerPanel.getFltPublisher().setText("");
        registerPanel.getFltYear().setText("");
        registerPanel.getFltBillNo().setText("");
        registerPanel.getFltSource().setSelectedIndex(0);
        applyFilters();
    }

    private void addRegexFilter(List<RowFilter<Object, Object>> filters, String raw, int col) {
        String t = raw == null ? "" : raw.trim();
        if (t.isEmpty()) return;
        filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(t), col));
    }

    private void syncBookNoFromAuthor() {
        String author = addPanel.getTxtAuthor().getText().trim();
        if (author.isEmpty()) {
            addPanel.setBookNo("");
            return;
        }
        try {
            addPanel.setBookNo(BookLogic.generateBookNo(author));
        } catch (ValidationException e) {
            addPanel.setBookNo("");
        }
    }

    private void syncStatusFromWithdrawnDate() {
        String wd = addPanel.getTxtWithdrawnDate().getText().trim();
        addPanel.setStatus(wd.isEmpty() ? BookLogic.STATUS_ACTIVE : BookLogic.STATUS_WITHDRAWN);
    }

    private Date parseDateNullable(String raw, String fieldName) throws ValidationException {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return null;

        try {
            LocalDate ld = LocalDate.parse(s);
            return Date.valueOf(ld);
        } catch (Exception e) {
            throw new ValidationException(fieldName + " must be in yyyy-MM-dd format.");
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;
        SimpleDocumentListener(Runnable callback) { this.callback = callback; }
        @Override public void insertUpdate(DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
