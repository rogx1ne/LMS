package com.library.setup;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Automated dependency installer for Java 8 and Oracle 10g XE
 * Uses system package managers where available
 */
public class DependencyInstaller {
    
    private SystemEnvironment.OSType osType;
    
    public DependencyInstaller(SystemEnvironment.OSType osType) {
        this.osType = osType;
    }
    
    /**
     * Install Java 8
     */
    public boolean installJava(JTextArea log) {
        log.append("Installing Java 8...\n");
        
        try {
            if (osType == SystemEnvironment.OSType.LINUX) {
                return installJavaLinux(log);
            } else if (osType == SystemEnvironment.OSType.WINDOWS) {
                return installJavaWindows(log);
            } else {
                log.append("Automatic installation not supported on this OS\n");
                return false;
            }
        } catch (Exception e) {
            log.append("Installation failed: " + e.getMessage() + "\n");
            return false;
        }
    }
    
    private boolean installJavaLinux(JTextArea log) throws Exception {
        log.append("Detecting package manager...\n");
        
        // Check for pacman (Arch-based)
        if (commandExists("pacman")) {
            log.append("Using pacman...\n");
            return executeCommand("sudo pacman -S --noconfirm jdk8-openjdk", log);
        }
        
        // Check for apt (Debian/Ubuntu)
        if (commandExists("apt-get")) {
            log.append("Using apt-get...\n");
            executeCommand("sudo apt-get update", log);
            return executeCommand("sudo apt-get install -y openjdk-8-jdk", log);
        }
        
        // Check for yum (RHEL/CentOS)
        if (commandExists("yum")) {
            log.append("Using yum...\n");
            return executeCommand("sudo yum install -y java-1.8.0-openjdk-devel", log);
        }
        
        log.append("No supported package manager found\n");
        log.append("Please install Java 8 manually\n");
        return false;
    }
    
    private boolean installJavaWindows(JTextArea log) throws Exception {
        log.append("Checking for Chocolatey...\n");
        
        if (commandExists("choco")) {
            log.append("Using Chocolatey...\n");
            return executeCommand("choco install adoptopenjdk8 -y", log);
        }
        
        log.append("Chocolatey not found.\n");
        log.append("Please download Java 8 from:\n");
        log.append("https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html\n");
        return false;
    }
    
    /**
     * Install Oracle Database
     */
    public boolean installOracle(JTextArea log) {
        log.append("Installing Oracle Database...\n");
        log.append("\nNote: Oracle Database requires manual installation due to licensing.\n");
        log.append("Automatic installation is not available.\n\n");
        
        if (osType == SystemEnvironment.OSType.LINUX) {
            log.append("For Linux, consider using Docker:\n");
            log.append("  docker pull gvenzl/oracle-xe:11\n");
            log.append("  docker run -d -p 1521:1521 gvenzl/oracle-xe:11\n\n");
        }
        
        log.append("Download Oracle Database from:\n");
        log.append("https://www.oracle.com/database/technologies/xe-downloads.html\n");
        
        return false;
    }
    
    /**
     * Check if a command exists in PATH
     */
    private boolean commandExists(String command) {
        try {
            String checkCmd;
            if (osType == SystemEnvironment.OSType.WINDOWS) {
                checkCmd = "where " + command;
            } else {
                checkCmd = "which " + command;
            }
            
            Process process = Runtime.getRuntime().exec(checkCmd);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Execute a shell command and display output
     */
    private boolean executeCommand(String command, JTextArea log) {
        try {
            log.append("$ " + command + "\n");
            
            ProcessBuilder pb = new ProcessBuilder();
            if (osType == SystemEnvironment.OSType.WINDOWS) {
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("bash", "-c", command);
            }
            
            Process process = pb.start();
            
            // Read output
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            String line;
            while ((line = stdOut.readLine()) != null) {
                log.append(line + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
            while ((line = stdErr.readLine()) != null) {
                log.append(line + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.append("✓ Command completed successfully\n");
                return true;
            } else {
                log.append("✗ Command failed with exit code: " + exitCode + "\n");
                return false;
            }
            
        } catch (Exception e) {
            log.append("✗ Error: " + e.getMessage() + "\n");
            return false;
        }
    }
    
    /**
     * Show installation instructions dialog
     */
    public static void showInstallInstructions(Component parent, String dependency, SystemEnvironment.OSType osType) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), 
                                     "Install " + dependency, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("Install " + dependency);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        
        JTextArea txtLog = new JTextArea();
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtLog.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtLog);
        mainPanel.add(scroll, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        DependencyInstaller installer = new DependencyInstaller(osType);
        
        JButton btnInstall = new JButton("Auto Install");
        btnInstall.addActionListener(e -> {
            btnInstall.setEnabled(false);
            new Thread(() -> {
                boolean success;
                if (dependency.contains("Java")) {
                    success = installer.installJava(txtLog);
                } else {
                    success = installer.installOracle(txtLog);
                }
                
                if (success) {
                    txtLog.append("\n✓ Installation completed!\n");
                    txtLog.append("Please restart the setup wizard.\n");
                } else {
                    txtLog.append("\n✗ Auto-installation not available.\n");
                    txtLog.append("Please follow the manual instructions above.\n");
                }
            }).start();
        });
        buttonPanel.add(btnInstall);
        
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}
