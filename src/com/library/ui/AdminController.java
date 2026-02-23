package com.library.ui;

import com.library.dao.AdminDAO;
import com.library.model.AuditLogEntry;
import com.library.model.User;
import com.library.service.AdminService;
import com.library.service.AuditLogger;
import com.library.service.CurrentUserContext;
import com.library.service.ExcelService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdminController {
    private final AdminModulePanel modulePanel;
    private final UserManagementPanel userPanel;
    private final DataImportExportPanel dataPanel;
    private final AuditLogPanel auditPanel;

    private final AdminDAO adminDAO = new AdminDAO();
    private final AdminService adminService = new AdminService();
    private final ExcelService excelService = new ExcelService();

    private boolean schemaAvailable;
    private boolean schemaWarningShown;

    public AdminController(AdminModulePanel modulePanel) {
        this.modulePanel = modulePanel;
        this.userPanel = modulePanel.getUserManagementPanel();
        this.dataPanel = modulePanel.getDataImportExportPanel();
        this.auditPanel = modulePanel.getAuditLogPanel();

        bindActions();
        initState();
    }

    private void initState() {
        schemaAvailable = adminDAO.isAdminSchemaAvailable();

        loadTableOptions();
        loadModuleOptions();
        if (schemaAvailable) {
            refreshUserPreviewId();
            refreshUserTable();
            refreshAuditTable();
        } else {
            userPanel.getTxtUserId().setText("SCHEMA_MISSING");
            userPanel.getTableModel().setRowCount(0);
            auditPanel.getTableModel().setRowCount(0);
            dataPanel.getTxtResult().setText(
                "Admin schema mismatch.\n" +
                "Run updated script.sql (STATUS in TBL_CREDENTIALS + TBL_AUDIT_LOG).\n" +
                "Then restart the app."
            );
        }
    }

    private void bindActions() {
        userPanel.getBtnCreateUser().addActionListener(e -> createUser());
        userPanel.getBtnDeleteUser().addActionListener(e -> deactivateSelectedUser());
        userPanel.getBtnRefresh().addActionListener(e -> {
            refreshUserPreviewId();
            refreshUserTable();
            refreshAuditTable();
        });
        userPanel.getBtnClear().addActionListener(e -> {
            userPanel.clearForm();
            refreshUserPreviewId();
        });

        userPanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = userPanel.getTable().getSelectedRow();
            if (viewRow < 0) return;
            int row = userPanel.getTable().convertRowIndexToModel(viewRow);
            userPanel.getTxtUserId().setText(String.valueOf(userPanel.getTableModel().getValueAt(row, 0)));
            userPanel.getTxtName().setText(String.valueOf(userPanel.getTableModel().getValueAt(row, 1)));
            userPanel.getTxtEmail().setText(String.valueOf(userPanel.getTableModel().getValueAt(row, 2)));
            userPanel.getTxtPhone().setText(String.valueOf(userPanel.getTableModel().getValueAt(row, 3)));
            userPanel.getTxtPassword().setText("");
        });

        dataPanel.getBtnExport().addActionListener(e -> exportToExcel());
        dataPanel.getBtnImport().addActionListener(e -> importFromExcel());

        auditPanel.getBtnSearch().addActionListener(e -> refreshAuditTable());
        auditPanel.getBtnReset().addActionListener(e -> {
            auditPanel.getTxtUserId().setText("");
            auditPanel.getTxtFromDate().setText("");
            auditPanel.getTxtToDate().setText("");
            auditPanel.getCmbModule().setSelectedItem("All");
            refreshAuditTable();
        });
    }

    private void createUser() {
        if (!ensureSchema()) return;
        try {
            String name = adminService.validateName(userPanel.getTxtName().getText());
            String password = adminService.validatePassword(new String(userPanel.getTxtPassword().getPassword()));
            String email = adminService.validateEmail(userPanel.getTxtEmail().getText());
            String phone = adminService.validatePhone(userPanel.getTxtPhone().getText());

            User created = adminDAO.createUser(name, password, email, phone, CurrentUserContext.getUserId());
            JOptionPane.showMessageDialog(modulePanel, "User created: " + created.getUserId(), "Success", JOptionPane.INFORMATION_MESSAGE);

            userPanel.clearForm();
            refreshUserPreviewId();
            refreshUserTable();
            refreshAuditTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Create user failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivateSelectedUser() {
        if (!ensureSchema()) return;
        int viewRow = userPanel.getTable().getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(modulePanel, "Select a user first.");
            return;
        }
        int row = userPanel.getTable().convertRowIndexToModel(viewRow);

        String userId = String.valueOf(userPanel.getTableModel().getValueAt(row, 0));
        String status = String.valueOf(userPanel.getTableModel().getValueAt(row, 4));

        if (CurrentUserContext.getUserId().equalsIgnoreCase(userId)) {
            JOptionPane.showMessageDialog(modulePanel, "You cannot deactivate your own account.");
            return;
        }
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(modulePanel, "Selected user is already inactive.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            modulePanel,
            "Deactivate user " + userId + "?",
            "Confirm Deactivation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        if (adminDAO.deactivateUser(userId, CurrentUserContext.getUserId())) {
            JOptionPane.showMessageDialog(modulePanel, "User deactivated.");
            refreshUserTable();
            refreshUserPreviewId();
            refreshAuditTable();
        } else {
            JOptionPane.showMessageDialog(modulePanel, "No changes applied.");
        }
    }

    private void exportToExcel() {
        if (!ensureSchema()) return;
        String selected = String.valueOf(dataPanel.getCmbTable().getSelectedItem());
        if (selected == null || selected.trim().isEmpty()) {
            JOptionPane.showMessageDialog(modulePanel, "Select a table first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(selected + "_" + LocalDate.now() + ".xlsx"));
        if (chooser.showSaveDialog(modulePanel) != JFileChooser.APPROVE_OPTION) return;

        Path path = normalizeXlsxPath(chooser.getSelectedFile().toPath());
        try {
            List<Map<String, Object>> rows = adminDAO.fetchTableData(selected);
            excelService.exportRows(selected, rows, path);

            if (schemaAvailable) {
                AuditLogger.logAction(
                    CurrentUserContext.getUserId(),
                    "Admin",
                    "Exported " + rows.size() + " rows from " + selected + " to " + path.getFileName()
                );
            }

            dataPanel.getTxtResult().setText(
                "Export successful.\nTable: " + selected + "\nRows: " + rows.size() + "\nFile: " + path
            );
            refreshAuditTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importFromExcel() {
        if (!ensureSchema()) return;
        String selected = String.valueOf(dataPanel.getCmbTable().getSelectedItem());
        if (selected == null || selected.trim().isEmpty()) {
            JOptionPane.showMessageDialog(modulePanel, "Select a table first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(modulePanel) != JFileChooser.APPROVE_OPTION) return;
        Path file = chooser.getSelectedFile().toPath();

        try {
            List<Map<String, String>> rows = excelService.readRows(file);
            int inserted = adminDAO.importRows(selected, rows, CurrentUserContext.getUserId());
            dataPanel.getTxtResult().setText(
                "Import successful.\nTable: " + selected + "\nRows inserted: " + inserted + "\nSource: " + file
            );
            refreshAuditTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(modulePanel, "Import failed (rolled back): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshUserPreviewId() {
        userPanel.getTxtUserId().setText(schemaAvailable ? adminDAO.peekNextUserId() : "SCHEMA_MISSING");
    }

    private void refreshUserTable() {
        DefaultTableModel model = userPanel.getTableModel();
        model.setRowCount(0);
        for (User u : adminDAO.getAllUsers()) {
            model.addRow(new Object[]{
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getPhoneNumber(),
                u.getStatus()
            });
        }
    }

    private void refreshAuditTable() {
        DefaultTableModel model = auditPanel.getTableModel();
        model.setRowCount(0);

        if (!schemaAvailable) return;

        try {
            Date from = adminService.parseDateOrNull(auditPanel.getTxtFromDate().getText());
            Date to = adminService.parseDateOrNull(auditPanel.getTxtToDate().getText());
            String userId = auditPanel.getTxtUserId().getText();
            String module = String.valueOf(auditPanel.getCmbModule().getSelectedItem());

            List<AuditLogEntry> logs = adminDAO.getAuditLogs(userId, module, from, to);
            for (AuditLogEntry log : logs) {
                model.addRow(new Object[]{
                    log.getLogId(),
                    log.getUserId(),
                    log.getModule(),
                    log.getActionDescription(),
                    log.getTimestamp()
                });
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(modulePanel, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTableOptions() {
        JComboBox<String> cmb = dataPanel.getCmbTable();
        cmb.removeAllItems();
        for (String name : adminDAO.getSupportedTableNames()) {
            cmb.addItem(name);
        }
    }

    private void loadModuleOptions() {
        JComboBox<String> cmb = auditPanel.getCmbModule();
        cmb.removeAllItems();
        cmb.addItem("All");
        cmb.addItem("Admin");
        cmb.addItem("Book");
        cmb.addItem("Student");
        cmb.addItem("Circulation");
        cmb.addItem("Procurement");
        cmb.addItem("Login");
        cmb.setSelectedItem("All");
    }

    private Path normalizeXlsxPath(Path path) {
        String p = path.toString();
        if (p.toLowerCase().endsWith(".xlsx")) return path;
        return path.resolveSibling(path.getFileName() + ".xlsx");
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
            "Admin schema required.\nRun updated script.sql and restart application.",
            "Database Schema Required",
            JOptionPane.WARNING_MESSAGE
        );
    }
}
