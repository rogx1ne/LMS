package com.library.setup;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

/**
 * LMS Uninstallation Manager - Handles complete removal of LMS
 * Responsible for:
 * 1. Removing database tables and data
 * 2. Deleting application files
 * 3. Removing launcher scripts
 */
public class UninstallationManager {
    
    private File installDir;
    private UninstallationProgressListener listener;
    
    public interface UninstallationProgressListener {
        void onProgress(String message);
        void onError(String error);
        void onComplete();
    }
    
    public UninstallationManager(File installDir, UninstallationProgressListener listener) {
        this.installDir = installDir;
        this.listener = listener;
    }
    
    /**
     * Execute full uninstallation workflow
     */
    public void uninstall() {
        try {
            log("Starting LMS Uninstallation...");
            
            // Step 1: Remove database data
            log("Removing database data...");
            removeDatabaseData();
            
            // Step 2: Remove application files
            log("Removing application files...");
            removeApplicationFiles();
            
            // Step 3: Remove installation directory
            log("Removing installation directory...");
            removeInstallationDirectory();
            
            log("✓ Uninstallation completed successfully!");
            if (listener != null) {
                listener.onComplete();
            }
        } catch (Exception e) {
            String error = "Uninstallation failed: " + e.getMessage();
            log(error);
            if (listener != null) {
                listener.onError(error);
            }
        }
    }
    
    /**
     * Remove all LMS database tables and objects
     */
    private void removeDatabaseData() throws Exception {
        String dbUrl = System.getenv("LMS_DB_URL");
        String dbUser = System.getenv("LMS_DB_USER");
        String dbPassword = System.getenv("LMS_DB_PASSWORD");
        
        if (dbUrl == null) dbUrl = "jdbc:oracle:thin:@localhost:1521:xe";
        if (dbUser == null) dbUser = "PRJ2531H";
        if (dbPassword == null) dbPassword = "PRJ2531H";
        
        // Set timezone property for Oracle compatibility
        System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            log("✓ Connected to database for cleanup");
            
            // Drop all LMS tables (reverse order of creation to handle constraints)
            String[] tablesToDrop = {
                "TBL_ISSUE",
                "TBL_BOOK_ALERT_LOG",
                "TBL_ORDER_DETAILS",
                "TBL_ORDER_HEADER",
                "TBL_BILL",
                "TBL_BOOK_STOCK",
                "TBL_BOOK_INFORMATION",
                "TBL_BOOK_CATALOG",
                "TBL_STUDENT",
                "TBL_SELLER",
                "TBL_AUDIT_LOG",
                "TBL_ID_COUNTER",
                "TBL_CREDENTIALS"
            };
            
            Statement stmt = conn.createStatement();
            for (String table : tablesToDrop) {
                try {
                    stmt.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS");
                    log("  ✓ Dropped table: " + table);
                } catch (SQLException e) {
                    // Table may not exist, which is fine
                    if (!e.getMessage().contains("does not exist")) {
                        log("  ⚠ Warning dropping " + table + ": " + e.getMessage());
                    }
                }
            }
            
            // Drop sequences
            String[] sequencesToDrop = {
                "SEQ_AUDIT_LOG",
                "SEQ_BOOK_ALERT_LOG",
                "SEQ_STUDENT_ID",
                "SEQ_SELLER_ID"
            };
            
            for (String seq : sequencesToDrop) {
                try {
                    stmt.execute("DROP SEQUENCE " + seq);
                    log("  ✓ Dropped sequence: " + seq);
                } catch (SQLException e) {
                    // Sequence may not exist, which is fine
                    if (!e.getMessage().contains("does not exist")) {
                        log("  ⚠ Warning dropping " + seq);
                    }
                }
            }
            
            stmt.close();
            conn.close();
            log("✓ Database data removed");
            
        } catch (SQLException e) {
            log("⚠ Could not connect to database for cleanup (may already be removed)");
            log("  Manual cleanup may be required: DROP USER PRJ2531H CASCADE");
        }
    }
    
    /**
     * Remove application files
     */
    private void removeApplicationFiles() throws IOException {
        if (installDir == null || !installDir.exists()) {
            log("⚠ Installation directory does not exist");
            return;
        }
        
        // List files to remove
        File[] files = installDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.delete()) {
                        log("  ✓ Removed: " + file.getName());
                    } else {
                        log("  ⚠ Could not remove: " + file.getName());
                    }
                } else if (file.isDirectory()) {
                    deleteDirectory(file);
                }
            }
        }
    }
    
    /**
     * Recursively delete directory
     */
    private void deleteDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteDirectory(file);
                }
            }
        }
        if (dir.delete()) {
            log("  ✓ Removed directory: " + dir.getName());
        }
    }
    
    /**
     * Remove entire installation directory
     */
    private void removeInstallationDirectory() throws IOException {
        if (installDir == null || !installDir.exists()) {
            log("⚠ Installation directory already removed");
            return;
        }
        
        try {
            // Ensure directory is empty
            File[] remaining = installDir.listFiles();
            if (remaining != null && remaining.length == 0) {
                if (installDir.delete()) {
                    log("✓ Installation directory removed: " + installDir.getAbsolutePath());
                } else {
                    log("⚠ Could not remove installation directory: " + installDir.getAbsolutePath());
                }
            } else {
                log("⚠ Installation directory not empty, some files may remain");
            }
        } catch (Exception e) {
            log("⚠ Error removing installation directory: " + e.getMessage());
        }
    }
    
    private void log(String message) {
        System.out.println(message);
        if (listener != null) {
            listener.onProgress(message);
        }
    }
}
