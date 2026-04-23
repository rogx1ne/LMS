package com.library.setup;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * GUI Uninstaller for LMS
 */
public class Uninstaller extends JFrame {
    
    private File installDirectory;
    private SystemEnvironment.OSType osType;
    
    private JCheckBox chkRemoveData;
    private JTextArea txtLog;
    private JButton btnUninstall;
    private JButton btnCancel;
    private JProgressBar progressBar;
    
    public Uninstaller() {
        osType = SystemEnvironment.detectOSType();
        findInstallDirectory();
        initUI();
    }
    
    private void findInstallDirectory() {
        // Try to detect install directory from current location
        String currentDir = System.getProperty("user.dir");
        installDirectory = new File(currentDir);
    }
    
    private void initUI() {
        setTitle("Uninstall Library Management System");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel lblTitle = new JLabel("Uninstall Library Management System");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Options
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Uninstall Options"));
        
        JLabel lblInfo = new JLabel("<html>Installation Directory:<br/>" + 
                                    installDirectory.getAbsolutePath() + "</html>");
        optionsPanel.add(lblInfo);
        
        chkRemoveData = new JCheckBox("Remove database schema and all data (Cannot be undone!)");
        chkRemoveData.setForeground(Color.RED);
        optionsPanel.add(chkRemoveData);
        
        JLabel lblWarning = new JLabel("<html><font color='orange'>⚠ Warning: This will remove all shortcuts, " +
                                       "files, and optionally database tables.</font></html>");
        optionsPanel.add(lblWarning);
        
        centerPanel.add(optionsPanel, BorderLayout.NORTH);
        
        // Log area
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Uninstall Log"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        centerPanel.add(progressBar, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnUninstall = new JButton("Uninstall");
        btnUninstall.addActionListener(e -> performUninstall());
        buttonPanel.add(btnUninstall);
        
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Cancel uninstallation?", 
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void performUninstall() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to uninstall LMS?\n" +
                (chkRemoveData.isSelected() ? "⚠ This will PERMANENTLY DELETE all database data!" : ""),
                "Confirm Uninstall",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        btnUninstall.setEnabled(false);
        btnCancel.setEnabled(false);
        progressBar.setVisible(true);
        
        new Thread(() -> {
            try {
                log("Starting uninstallation...\n");
                
                // Step 1: Remove shortcuts
                progressBar.setValue(10);
                log("Removing desktop shortcuts...");
                removeShortcuts();
                log(" Done\n");
                
                // Step 2: Remove database (optional)
                progressBar.setValue(30);
                if (chkRemoveData.isSelected()) {
                    log("Removing database schema...");
                    removeDatabaseSchema();
                    log(" Done\n");
                } else {
                    log("Keeping database schema and data\n");
                }
                
                // Step 3: Remove files
                progressBar.setValue(60);
                log("Removing installation files...");
                removeFiles();
                log(" Done\n");
                
                progressBar.setValue(100);
                log("\nUninstallation completed successfully!\n");
                
                JOptionPane.showMessageDialog(this,
                        "LMS has been uninstalled successfully.",
                        "Uninstall Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                
                System.exit(0);
                
            } catch (Exception e) {
                log("\nError: " + e.getMessage() + "\n");
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Uninstallation failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                btnUninstall.setEnabled(true);
                btnCancel.setEnabled(true);
            }
        }).start();
    }
    
    private void removeShortcuts() throws Exception {
        String userHome = System.getProperty("user.home");
        
        if (osType == SystemEnvironment.OSType.LINUX) {
            // Remove .desktop files
            File applicationsDir = new File(userHome, ".local/share/applications");
            File desktopFile = new File(applicationsDir, "lms.desktop");
            if (desktopFile.exists()) {
                desktopFile.delete();
                log(" [Removed application menu entry]");
            }
            
            File desktop = new File(userHome, "Desktop");
            File desktopShortcut = new File(desktop, "LMS.desktop");
            if (desktopShortcut.exists()) {
                desktopShortcut.delete();
                log(" [Removed desktop shortcut]");
            }
            
        } else if (osType == SystemEnvironment.OSType.WINDOWS) {
            // Remove desktop shortcut
            File desktop = new File(userHome, "Desktop");
            File desktopShortcut = new File(desktop, "Library Management System.lnk");
            if (desktopShortcut.exists()) {
                desktopShortcut.delete();
                log(" [Removed desktop shortcut]");
            }
            
            // Remove Start Menu folder
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                File startMenuDir = new File(appData, "Microsoft\\Windows\\Start Menu\\Programs\\Library Management System");
                if (startMenuDir.exists()) {
                    deleteDirectory(startMenuDir);
                    log(" [Removed Start Menu entry]");
                }
            }
        }
    }
    
    private void removeDatabaseSchema() throws Exception {
        // Load database configuration (would normally read from config file)
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String username = "system";
        String password = "manager";
        
        // This is a placeholder - in reality, you'd read connection details from a config file
        log("\n  ⚠ Database removal requires manual configuration\n");
        log("  Please run the following SQL as SYSTEM user:\n");
        log("  DROP TABLE TBL_USER CASCADE CONSTRAINTS;\n");
        log("  DROP TABLE TBL_ACCESSION CASCADE CONSTRAINTS;\n");
        log("  DROP TABLE TBL_STUDENT CASCADE CONSTRAINTS;\n");
        log("  DROP TABLE TBL_ISSUE CASCADE CONSTRAINTS;\n");
        log("  (... and all other TBL_* tables)\n");
    }
    
    private void removeFiles() throws Exception {
        // Delete bin directory
        File binDir = new File(installDirectory, "bin");
        if (binDir.exists()) {
            deleteDirectory(binDir);
            log(" [Removed bin/]");
        }
        
        // Delete launcher scripts
        File linuxLauncher = new File(installDirectory, "LMS-Launcher.sh");
        if (linuxLauncher.exists()) {
            linuxLauncher.delete();
            log(" [Removed launcher script]");
        }
        
        File windowsLauncher = new File(installDirectory, "LMS-Launcher.bat");
        if (windowsLauncher.exists()) {
            windowsLauncher.delete();
            log(" [Removed launcher script]");
        }
        
        // Note: We don't delete src/, lib/, docs/ to preserve the original installation
        log(" [Kept source files and libraries]");
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message);
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Uninstaller uninstaller = new Uninstaller();
            uninstaller.setVisible(true);
        });
    }
}
