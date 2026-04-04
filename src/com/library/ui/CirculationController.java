package com.library.ui;

import com.library.dao.CirculationDAO;
import com.library.model.AvailableBookRow;
import com.library.model.CirculationReportRow;
import com.library.model.IssueReturnResult;
import com.library.model.IssueTransaction;
import com.library.service.CirculationService;
import com.library.service.CurrentUserContext;
import com.library.service.PdfReportService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.RowFilter;

public class CirculationController {
    private final CirculationModulePanel modulePanel;
    private final IssuePanel issuePanel;
    private final ReturnPanel returnPanel;
    private final CirculationReportPanel reportPanel;
    private final CirculationDAO circulationDAO = new CirculationDAO();
    private final CirculationService circulationService = new CirculationService();
    private final PdfReportService reportService = new PdfReportService();
    private boolean schemaAvailable;
    private boolean schemaWarningShown;

    public CirculationController(CirculationModulePanel modulePanel) {
        this.modulePanel = modulePanel;
        this.issuePanel = modulePanel.getIssuePanel();
        this.returnPanel = modulePanel.getReturnPanel();
        this.reportPanel = modulePanel.getReportPanel();
        bindActions();
        initState();
    }

    private void bindActions() {
        issuePanel.getBtnIssue().addActionListener(e -> handleIssueBook());
        issuePanel.getBtnClear().addActionListener(e -> {
            issuePanel.clearIssueInputs();
            refreshIssueDefaults();
        });
        issuePanel.getBtnSearchBook().addActionListener(e -> openAvailableBookSearch());

        returnPanel.getBtnSearch().addActionListener(e -> refreshReturnTable());
        returnPanel.getTxtBorrowerSearch().addActionListener(e -> refreshReturnTable());
        returnPanel.getBtnReset().addActionListener(e -> resetReturnFilters());
        returnPanel.getBtnProcessReturn().addActionListener(e -> handleProcessReturn());

        reportPanel.getTxtSearch().getDocument().addDocumentListener(new SimpleDocumentListener(this::applyReportFilters));
        reportPanel.getCmbStatus().addActionListener(e -> applyReportFilters());
        reportPanel.getTxtFromDate().addPropertyChangeListener("text", e -> applyReportFilters());
        reportPanel.getTxtToDate().addPropertyChangeListener("text", e -> applyReportFilters());
        reportPanel.getBtnRefresh().addActionListener(e -> refreshCirculationReport());
        reportPanel.getBtnPdf().addActionListener(e ->
            reportService.exportToPdf(modulePanel, reportPanel.getTable(), "Circulation Report", "Circulation_Report")
        );
    }

    private void initState() {
        schemaAvailable = circulationDAO.isCirculationSchemaAvailable();
        refreshIssueDefaults();
        if (schemaAvailable) {
            refreshReturnTable();
            refreshCirculationReport();
        } else {
            showSchemaWarningOnce();
            resetReturnFilters();
            reportPanel.getTableModel().setRowCount(0);
            reportPanel.updateTotalCount(0);
            issuePanel.getTxtMessage().setText(
                "Missing circulation schema.\nRun the updated script.sql to recreate TBL_ISSUE and CIRC_STATUS."
            );
        }
    }

    private void refreshIssueDefaults() {
        issuePanel.getTxtIssueId().setText(schemaAvailable ? circulationDAO.peekNextIssueId() : "SCHEMA_MISSING");
        LocalDate issueDate = LocalDate.now();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        issuePanel.getTxtIssueDate().setText(issueDate.format(dtf));
        issuePanel.getTxtDueDate().setText(circulationService.calculateDueDate(issueDate).format(dtf));
        issuePanel.getTxtIssuedBy().setText(CurrentUserContext.getUserId());
    }

    private void handleIssueBook() {
        if (!ensureSchema()) return;
        try {
            if (!ModuleTheme.confirmPreview(
                modulePanel,
                "Confirm Book Issue",
                "Issue Book",
                "Issue ID: " + issuePanel.getTxtIssueId().getText().trim(),
                "Borrower Type: " + String.valueOf(issuePanel.getCmbBorrowerType().getSelectedItem()),
                "Card ID: " + issuePanel.getTxtCardId().getText().trim(),
                "Faculty Name: " + issuePanel.getTxtFacultyName().getText().trim(),
                "Faculty Contact: " + issuePanel.getTxtFacultyContact().getText().trim(),
                "Accession No: " + issuePanel.getTxtAccessionNo().getText().trim(),
                "Issue Date: " + issuePanel.getTxtIssueDate().getText().trim(),
                "Due Date: " + issuePanel.getTxtDueDate().getText().trim(),
                "Issued By: " + issuePanel.getTxtIssuedBy().getText().trim()
            )) {
                return;
            }

            IssueTransaction txn = circulationDAO.issueBook(
                String.valueOf(issuePanel.getCmbBorrowerType().getSelectedItem()),
                issuePanel.getTxtCardId().getText(),
                issuePanel.getTxtFacultyName().getText(),
                issuePanel.getTxtFacultyContact().getText(),
                issuePanel.getTxtAccessionNo().getText(),
                CurrentUserContext.getUserId()
            );

            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String message =
                "Issued Successfully\n" +
                "Issue ID: " + txn.getIssueId() + "\n" +
                "Borrower Type: " + txn.getBorrowerType() + "\n" +
                (txn.getCardId() == null ? "" : "Card ID: " + txn.getCardId() + "\n") +
                "Borrower Name: " + txn.getBorrowerName() + "\n" +
                "Borrower Contact: " + txn.getBorrowerContact() + "\n" +
                "Accession No: " + txn.getAccessionNo() + "\n" +
                "Due Date: " + (txn.getDueDate() != null ? txn.getDueDate().toLocalDate().format(dtf) : "");
            issuePanel.getTxtMessage().setText(message);
            JOptionPane.showMessageDialog(modulePanel, message, "Book Issued", JOptionPane.INFORMATION_MESSAGE);

            refreshReturnTable();
            refreshCirculationReport();

            issuePanel.clearIssueInputs();
            refreshIssueDefaults();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Issue failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshReturnTable() {
        if (!ensureSchema()) return;
        try {
            List<IssueTransaction> txns = circulationDAO.getOpenIssuedBooks();
            DefaultTableModel model = returnPanel.getTableModel();
            model.setRowCount(0);

            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int i = 1;
            for (IssueTransaction t : txns) {
                model.addRow(new Object[]{
                    i++,
                    t.getIssueId(),
                    t.getBorrowerType(),
                    t.getCardId() == null ? "" : t.getCardId(),
                    t.getBorrowerName(),
                    t.getBorrowerContact(),
                    t.getAccessionNo(),
                    t.getBookTitle(),
                    t.getAuthorName(),
                    t.getIssueDate() != null ? t.getIssueDate().toLocalDate().format(dtf) : "",
                    t.getDueDate() != null ? t.getDueDate().toLocalDate().format(dtf) : "",
                    t.getIssuedBy()
                });
            }
            applyReturnFilters();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Failed to load issued books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAvailableBookSearch() {
        if (!ensureSchema()) return;
        try {
            List<AvailableBookRow> rows = circulationDAO.getAvailableBooks();
            Window owner = SwingUtilities.getWindowAncestor(modulePanel);
            AvailableBookSearchDialog dialog = new AvailableBookSearchDialog(owner, rows);
            dialog.setVisible(true);

            String accessionNo = dialog.getSelectedAccessionNo();
            if (accessionNo != null && !accessionNo.trim().isEmpty()) {
                issuePanel.getTxtAccessionNo().setText(accessionNo);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Failed to load available books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleProcessReturn() {
        if (!ensureSchema()) return;
        int viewRow = returnPanel.getTable().getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(modulePanel, "Select an issued row first.");
            return;
        }

        int modelRow = returnPanel.getTable().convertRowIndexToModel(viewRow);
        String issueId = String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 1));
        String inspectionCondition = String.valueOf(returnPanel.getCmbInspectionCondition().getSelectedItem());

        if (!ModuleTheme.confirmPreview(
            modulePanel,
            "Confirm Book Return",
            "Process Return",
            "Issue ID: " + issueId,
            "Borrower Type: " + String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 2)),
            "Card ID: " + String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 3)),
            "Borrower Name: " + String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 4)),
            "Contact: " + String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 5)),
            "Accession No: " + String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 6)),
            "Book Title: " + String.valueOf(returnPanel.getTableModel().getValueAt(modelRow, 7)),
            "Inspection Result: " + inspectionCondition
        )) {
            return;
        }

        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            IssueReturnResult result = circulationDAO.returnBook(issueId, inspectionCondition);
            String inventoryAction =
                CirculationService.RETURN_CONDITION_GOOD.equals(result.getReturnCondition())
                    ? "Inventory Action: Book restored to library circulation."
                    : CirculationService.RETURN_CONDITION_DAMAGED.equals(result.getReturnCondition())
                        ? "Inventory Action: Book marked DAMAGED in catalog."
                        : "Inventory Action: Book marked LOST in catalog.";
            String message =
                "Return Processed\n" +
                "Issue ID: " + result.getIssueId() + "\n" +
                "Accession No: " + result.getAccessionNo() + "\n" +
                "Inspection: " + result.getReturnCondition() + "\n" +
                "Return Date: " + (result.getReturnDate() != null ? result.getReturnDate().toLocalDate().format(dtf) : "") + "\n" +
                "Late Fine: Rs. " + result.getLateFine() + "\n" +
                "Inspection Fine: Rs. " + result.getInspectionFine() + "\n" +
                "Total Fine: Rs. " + result.getFine() + "\n" +
                inventoryAction;
            JOptionPane.showMessageDialog(modulePanel, message, "Book Returned", JOptionPane.INFORMATION_MESSAGE);

            refreshReturnTable();
            refreshCirculationReport();
            refreshIssueDefaults();
            returnPanel.getCmbInspectionCondition().setSelectedItem(CirculationService.RETURN_CONDITION_GOOD);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Return failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyReturnFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String search = returnPanel.getTxtBorrowerSearch().getText().trim();
        if (!search.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(search), 1, 3, 4, 5, 6, 7, 8, 11));
        }

        String borrowerType = String.valueOf(returnPanel.getCmbBorrowerType().getSelectedItem());
        if (!"All".equals(borrowerType)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(borrowerType) + "$", 2));
        }

        returnPanel.getSorter().setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        returnPanel.getLblCount().setText("Open Issues: " + returnPanel.getSorter().getViewRowCount());
    }

    private void resetReturnFilters() {
        returnPanel.getTxtBorrowerSearch().setText("");
        returnPanel.getCmbBorrowerType().setSelectedIndex(0);
        returnPanel.getCmbInspectionCondition().setSelectedItem(CirculationService.RETURN_CONDITION_GOOD);
        if (schemaAvailable) {
            refreshReturnTable();
        } else {
            returnPanel.getTableModel().setRowCount(0);
            returnPanel.getLblCount().setText("Open Issues: 0");
        }
    }

    private void refreshCirculationReport() {
        DefaultTableModel model = reportPanel.getTableModel();
        model.setRowCount(0);

        if (!schemaAvailable) {
            reportPanel.updateTotalCount(0);
            return;
        }

        try {
            List<CirculationReportRow> rows = circulationDAO.getCirculationReportRows();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (CirculationReportRow row : rows) {
                model.addRow(new Object[]{
                    row.getIssueId(),
                    row.getBorrowerType(),
                    row.getCardId() == null ? "" : row.getCardId(),
                    row.getBorrowerName() == null ? "" : row.getBorrowerName(),
                    row.getBorrowerContact() == null ? "" : row.getBorrowerContact(),
                    row.getAccessionNo(),
                    row.getBookTitle(),
                    row.getAuthorName(),
                    row.getIssueDate() != null ? sdf.format(row.getIssueDate()) : "",
                    row.getDueDate() != null ? sdf.format(row.getDueDate()) : "",
                    row.getReturnDate() != null ? sdf.format(row.getReturnDate()) : "",
                    row.getReturnCondition() == null ? "" : row.getReturnCondition(),
                    row.getFine() == null ? "" : row.getFine(),
                    row.getIssuedBy(),
                    row.getStatus()
                });
            }
            applyReportFilters();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Failed to load circulation report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyReportFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String search = reportPanel.getTxtSearch().getText().trim();
        if (!search.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(search), 0, 1, 2, 3, 4, 5, 6, 7, 11, 13, 14));
        }

        String status = String.valueOf(reportPanel.getCmbStatus().getSelectedItem());
        if (!"All".equals(status)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(status) + "$", 14));
        }

        String fromDateStr = reportPanel.getTxtFromDate().getText().trim();
        String toDateStr = reportPanel.getTxtToDate().getText().trim();
        if (!fromDateStr.isEmpty() || !toDateStr.isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    String issueDate = String.valueOf(entry.getValue(8));
                    return isWithinDateRange(issueDate, fromDateStr, toDateStr);
                }
            });
        }

        reportPanel.getSorter().setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        reportPanel.updateTotalCount(reportPanel.getSorter().getViewRowCount());
    }

    private boolean isWithinDateRange(String dateStr, String fromDateStr, String toDateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return true;
        }
        if (fromDateStr.isEmpty() && toDateStr.isEmpty()) {
            return true;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date date = sdf.parse(dateStr);
            
            if (!fromDateStr.isEmpty()) {
                java.util.Date fromDate = sdf.parse(fromDateStr);
                if (date.before(fromDate)) {
                    return false;
                }
            }
            
            if (!toDateStr.isEmpty()) {
                java.util.Date toDate = sdf.parse(toDateStr);
                if (date.after(toDate)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean ensureSchema() {
        if (schemaAvailable) return true;
        showSchemaWarningOnce();
        return false;
    }

    private void showSchemaWarningOnce() {
        if (schemaWarningShown) return;
        schemaWarningShown = true;
        JOptionPane.showMessageDialog(
            modulePanel,
            "Circulation schema is missing.\nRun the updated script.sql as PRJ2531H and restart.",
            "Database Schema Required",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override public void insertUpdate(DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
