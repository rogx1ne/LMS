package com.library.ui;

import com.library.dao.CirculationDAO;
import com.library.model.IssueReturnResult;
import com.library.model.IssueTransaction;
import com.library.service.CirculationService;
import com.library.service.CurrentUserContext;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CirculationController {
    private final CirculationModulePanel modulePanel;
    private final IssuePanel issuePanel;
    private final ReturnPanel returnPanel;
    private final CirculationDAO circulationDAO = new CirculationDAO();
    private final CirculationService circulationService = new CirculationService();
    private boolean schemaAvailable;
    private boolean schemaWarningShown;

    public CirculationController(CirculationModulePanel modulePanel) {
        this.modulePanel = modulePanel;
        this.issuePanel = modulePanel.getIssuePanel();
        this.returnPanel = modulePanel.getReturnPanel();
        bindActions();
        initState();
    }

    private void bindActions() {
        issuePanel.getBtnIssue().addActionListener(e -> handleIssueBook());
        issuePanel.getBtnClear().addActionListener(e -> {
            issuePanel.clearIssueInputs();
            refreshIssueDefaults();
        });

        returnPanel.getBtnSearch().addActionListener(e -> handleSearchIssued());
        returnPanel.getTxtCardIdSearch().addActionListener(e -> handleSearchIssued());
        returnPanel.getBtnReset().addActionListener(e -> resetReturnTable());
        returnPanel.getBtnProcessReturn().addActionListener(e -> handleProcessReturn());
    }

    private void initState() {
        schemaAvailable = circulationDAO.isCirculationSchemaAvailable();
        refreshIssueDefaults();
        if (!schemaAvailable) {
            showSchemaWarningOnce();
            resetReturnTable();
            issuePanel.getTxtMessage().setText(
                "Missing circulation schema.\nRun circulation_migration.sql to create TBL_ISSUE and CIRC_STATUS."
            );
        }
    }

    private void refreshIssueDefaults() {
        issuePanel.getTxtIssueId().setText(schemaAvailable ? circulationDAO.peekNextIssueId() : "SCHEMA_MISSING");
        LocalDate issueDate = LocalDate.now();
        issuePanel.getTxtIssueDate().setText(issueDate.toString());
        issuePanel.getTxtDueDate().setText(circulationService.calculateDueDate(issueDate).toString());
        issuePanel.getTxtIssuedBy().setText(CurrentUserContext.getUserId());
    }

    private void handleIssueBook() {
        if (!ensureSchema()) return;
        try {
            IssueTransaction txn = circulationDAO.issueBook(
                issuePanel.getTxtCardId().getText(),
                issuePanel.getTxtAccessionNo().getText(),
                CurrentUserContext.getUserId()
            );

            String message =
                "Issued Successfully\n" +
                "Issue ID: " + txn.getIssueId() + "\n" +
                "Card ID: " + txn.getCardId() + "\n" +
                "Accession No: " + txn.getAccessionNo() + "\n" +
                "Due Date: " + txn.getDueDate();
            issuePanel.getTxtMessage().setText(message);
            JOptionPane.showMessageDialog(modulePanel, message, "Book Issued", JOptionPane.INFORMATION_MESSAGE);

            String currentSearch = returnPanel.getTxtCardIdSearch().getText().trim();
            if (!currentSearch.isEmpty() && currentSearch.equalsIgnoreCase(txn.getCardId())) {
                loadIssuedBooksForCard(txn.getCardId());
            }

            issuePanel.clearIssueInputs();
            refreshIssueDefaults();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Issue failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSearchIssued() {
        if (!ensureSchema()) return;
        String cardId = returnPanel.getTxtCardIdSearch().getText().trim();
        if (cardId.isEmpty()) {
            JOptionPane.showMessageDialog(modulePanel, "Card ID is required to search.");
            return;
        }
        loadIssuedBooksForCard(cardId);
    }

    private void loadIssuedBooksForCard(String cardId) {
        try {
            List<IssueTransaction> txns = circulationDAO.getIssuedBooksByCard(cardId);
            DefaultTableModel model = returnPanel.getTableModel();
            model.setRowCount(0);

            int i = 1;
            for (IssueTransaction t : txns) {
                model.addRow(new Object[]{
                    i++,
                    t.getIssueId(),
                    t.getAccessionNo(),
                    t.getBookTitle(),
                    t.getAuthorName(),
                    t.getIssueDate(),
                    t.getDueDate(),
                    t.getIssuedBy()
                });
            }
            returnPanel.getLblCount().setText("Issued Books: " + txns.size());
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        try {
            IssueReturnResult result = circulationDAO.returnBook(issueId);
            String message =
                "Return Processed\n" +
                "Issue ID: " + result.getIssueId() + "\n" +
                "Accession No: " + result.getAccessionNo() + "\n" +
                "Return Date: " + result.getReturnDate() + "\n" +
                "Fine: Rs. " + result.getFine();
            JOptionPane.showMessageDialog(modulePanel, message, "Book Returned", JOptionPane.INFORMATION_MESSAGE);

            String cardId = returnPanel.getTxtCardIdSearch().getText().trim();
            if (!cardId.isEmpty()) {
                loadIssuedBooksForCard(cardId);
            } else {
                resetReturnTable();
            }
            refreshIssueDefaults();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Return failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetReturnTable() {
        returnPanel.getTxtCardIdSearch().setText("");
        returnPanel.getTableModel().setRowCount(0);
        returnPanel.getLblCount().setText("Issued Books: 0");
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
            "Circulation schema is missing.\nRun circulation_migration.sql as PRJ2531H and restart.",
            "Database Schema Required",
            JOptionPane.WARNING_MESSAGE
        );
    }
}
