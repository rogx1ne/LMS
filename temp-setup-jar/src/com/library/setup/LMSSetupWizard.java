package com.library.setup;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * LMS Setup Wizard - Professional Edition
 * Enhanced UI/UX matching LMS project design
 * 
 * Color Scheme (Professional Library Management):
 * - Primary: Deep Blue (#1E3A8A) - Professional, trustworthy
 * - Secondary: Teal (#0F766E) - Modern, fresh
 * - Accent: Gold (#D97706) - Premium, elegant
 * - Background: Light Gray (#F9FAFB) - Clean, minimal
 * - Text: Dark Gray (#1F2937) - High contrast, readable
 */
public class LMSSetupWizard extends JFrame {
    
    // Professional Color Palette (updated)
    private static final Color PRIMARY_COLOR = new Color(30, 58, 138);        // Deep Blue #1E3A8A
    private static final Color SECONDARY_COLOR = new Color(15, 118, 110);     // Teal #0F766E
    private static final Color ACCENT_COLOR = new Color(217, 119, 6);         // Gold #D97706
    private static final Color LIGHT_BG = new Color(249, 250, 251);           // Light Gray #F9FAFB
    private static final Color PANEL_BG = Color.WHITE;                        // Pure White #FFFFFF
    private static final Color BUTTON_BG = PRIMARY_COLOR;                     // Deep Blue for buttons
    private static final Color BUTTON_HOVER = new Color(25, 48, 115);         // Darker blue on hover
    private static final Color TEXT_FG = new Color(31, 41, 55);               // Dark Gray #1F2937
    private static final Color SECONDARY_TEXT = new Color(107, 114, 128);     // Medium Gray #6B7280
    private static final Color ERROR_FG = new Color(220, 38, 38);             // Red #DC2626
    private static final Color SUCCESS_FG = new Color(34, 197, 94);           // Green #22C55E
    private static final Color WARNING_FG = new Color(217, 119, 6);           // Gold warning
    private static final Color BORDER_COLOR = new Color(229, 231, 235);       // Light Border #E5E7EB
    
    // Setup state
    private File installLocation;
    private String adminUserId;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminPassword;
    private String adminPasswordConfirm;
    
    // Form field references for validation
    private JTextField adminUserIdField;
    private JTextField adminNameField;
    private JTextField adminEmailField;
    private JTextField adminPhoneField;
    private JPasswordField adminPasswordField;
    private JPasswordField adminPasswordConfirmField;
    
    // UI Components
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton nextButton;
    private JButton backButton;
    private JButton cancelButton;
    private JTextArea progressLogArea;
    
    // Page constants
    private static final String PAGE_WELCOME = "WELCOME";
    private static final String PAGE_LOCATION = "LOCATION";
    private static final String PAGE_CHECK = "CHECK";
    private static final String PAGE_ADMIN = "ADMIN";
    private static final String PAGE_PROGRESS = "PROGRESS";
    private static final String PAGE_COMPLETE = "COMPLETE";
    
    private String currentPage = PAGE_WELCOME;
    
    public LMSSetupWizard() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("LMS Setup Wizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel with light background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(LIGHT_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        
        // Content area (CardLayout for pages)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(LIGHT_BG);
        
        // Add pages (don't add listeners yet - nextButton not ready)
        cardPanel.add(createWelcomePage(), PAGE_WELCOME);
        cardPanel.add(createLocationPage(), PAGE_LOCATION);
        cardPanel.add(createCheckPage(), PAGE_CHECK);
        cardPanel.add(createAdminPage(), PAGE_ADMIN);
        cardPanel.add(createProgressPage(), PAGE_PROGRESS);
        cardPanel.add(createCompletePage(), PAGE_COMPLETE);
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        
        // Footer with buttons (CREATE FIRST before using in pages)
        mainPanel.add(createFooter(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Logo/Title area
        JLabel title = new JLabel("📚 Library Management System - Setup Wizard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        
        // Subtitle
        JLabel subtitle = new JLabel("Professional Edition v2.0");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(229, 231, 235)); // Light gray
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PRIMARY_COLOR);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subtitle);
        
        header.add(textPanel, BorderLayout.WEST);
        
        // Right side: Progress indicator
        JLabel progressLabel = new JLabel("Step 1 of 6");
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        progressLabel.setForeground(ACCENT_COLOR);
        header.add(progressLabel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createWelcomePage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel welcomeText = new JLabel("<html><center>" +
            "<h1>Welcome to LMS Setup</h1>" +
            "<p>This wizard will guide you through installing the</p>" +
            "<p>Library Management System</p><br>" +
            "<p><b>Prerequisites:</b></p>" +
            "<p>• Java 8 or higher</p>" +
            "<p>• Oracle 10g or higher</p>" +
            "</center></html>");
        welcomeText.setForeground(TEXT_FG);
        welcomeText.setFont(new Font("Arial", Font.PLAIN, 14));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(welcomeText, gbc);
        
        return panel;
    }
    
    private JPanel createLocationPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Choose Installation Location");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_FG);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JLabel pathLabel = new JLabel("Installation Path:");
        pathLabel.setForeground(TEXT_FG);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(pathLabel, gbc);
        
        JTextField pathField = new JTextField(40);
        pathField.setText(System.getProperty("user.home") + File.separator + "LMS");
        pathField.setBackground(Color.WHITE);
        pathField.setForeground(TEXT_FG);
        pathField.setCaretColor(TEXT_FG);
        gbc.gridx = 1;
        panel.add(pathField, gbc);
        
        JButton browseButton = new JButton("Browse...");
        styleButton(browseButton, false);
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        panel.add(browseButton, gbc);
        
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        JLabel infoLabel = new JLabel("The wizard will create this directory if it doesn't exist.");
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    private JPanel createCheckPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("System Requirements Check");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_FG);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        // Check Java
        String javaVersion = System.getProperty("java.version");
        String javaCheck = checkJavaVersion();
        JLabel javaLabel = new JLabel("Java: " + javaVersion);
        javaLabel.setForeground(javaCheck.contains("✓") ? SUCCESS_FG : ERROR_FG);
        panel.add(javaLabel, gbc);
        
        gbc.gridx = 1;
        JLabel javaStatus = new JLabel(javaCheck);
        javaStatus.setForeground(javaCheck.contains("✓") ? SUCCESS_FG : ERROR_FG);
        panel.add(javaStatus, gbc);
        
        // Check Oracle
        gbc.gridy++;
        gbc.gridx = 0;
        String oracleCheck = checkOracleDatabase();
        JLabel oracleLabel = new JLabel("Oracle Database:");
        oracleLabel.setForeground(oracleCheck.contains("✓") ? SUCCESS_FG : ERROR_FG);
        panel.add(oracleLabel, gbc);
        
        gbc.gridx = 1;
        JLabel oracleStatus = new JLabel(oracleCheck);
        oracleStatus.setForeground(oracleCheck.contains("✓") ? SUCCESS_FG : ERROR_FG);
        panel.add(oracleStatus, gbc);
        
        // Instructions if missing
        if (!javaCheck.contains("✓") || !oracleCheck.contains("✓")) {
            gbc.gridy += 2;
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            JLabel instructionLabel = new JLabel("<html>" +
                "Please install missing dependencies and then click 'Retry':<br>" +
                (javaCheck.contains("✗") ? "• Java 8 or higher required<br>" : "") +
                (oracleCheck.contains("✗") ? "• Oracle 10g or higher required<br>" : "") +
                "</html>");
            instructionLabel.setForeground(ERROR_FG);
            panel.add(instructionLabel, gbc);
        }
        
        return panel;
    }
    
    private JPanel createAdminPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Admin User Setup");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_FG);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        
        // User ID
        JLabel userIdLabel = new JLabel("User ID:");
        userIdLabel.setForeground(TEXT_FG);
        panel.add(userIdLabel, gbc);
        gbc.gridx = 1;
        adminUserIdField = new JTextField("ADMIN", 30);
        adminUserIdField.setBackground(Color.WHITE);
        adminUserIdField.setForeground(TEXT_FG);
        adminUserIdField.setCaretColor(TEXT_FG);
        adminUserIdField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                adminUserId = adminUserIdField.getText();
            }
        });
        panel.add(adminUserIdField, gbc);
        
        // Name
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(TEXT_FG);
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        adminNameField = new JTextField(30);
        adminNameField.setBackground(Color.WHITE);
        adminNameField.setForeground(TEXT_FG);
        adminNameField.setCaretColor(TEXT_FG);
        adminNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                adminName = adminNameField.getText();
            }
        });
        panel.add(adminNameField, gbc);
        
        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(TEXT_FG);
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        adminEmailField = new JTextField(30);
        adminEmailField.setBackground(Color.WHITE);
        adminEmailField.setForeground(TEXT_FG);
        adminEmailField.setCaretColor(TEXT_FG);
        adminEmailField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                adminEmail = adminEmailField.getText();
            }
        });
        panel.add(adminEmailField, gbc);
        
        // Phone
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setForeground(TEXT_FG);
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        adminPhoneField = new JTextField(30);
        adminPhoneField.setBackground(Color.WHITE);
        adminPhoneField.setForeground(TEXT_FG);
        adminPhoneField.setCaretColor(TEXT_FG);
        adminPhoneField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                adminPhone = adminPhoneField.getText();
            }
        });
        panel.add(adminPhoneField, gbc);
        
        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel pwdLabel = new JLabel("Password:");
        pwdLabel.setForeground(TEXT_FG);
        panel.add(pwdLabel, gbc);
        gbc.gridx = 1;
        adminPasswordField = new JPasswordField(30);
        adminPasswordField.setBackground(Color.WHITE);
        adminPasswordField.setForeground(TEXT_FG);
        adminPasswordField.setCaretColor(TEXT_FG);
        adminPasswordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                adminPassword = new String(adminPasswordField.getPassword());
            }
        });
        panel.add(adminPasswordField, gbc);
        
        // Confirm Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setForeground(TEXT_FG);
        panel.add(confirmLabel, gbc);
        gbc.gridx = 1;
        adminPasswordConfirmField = new JPasswordField(30);
        adminPasswordConfirmField.setBackground(Color.WHITE);
        adminPasswordConfirmField.setForeground(TEXT_FG);
        adminPasswordConfirmField.setCaretColor(TEXT_FG);
        adminPasswordConfirmField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                adminPasswordConfirm = new String(adminPasswordConfirmField.getPassword());
            }
        });
        panel.add(adminPasswordConfirmField, gbc);
        
        return panel;
    }
    
    private JPanel createProgressPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Installing...");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_FG);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setForeground(BUTTON_BG);
        panel.add(progressBar, BorderLayout.CENTER);
        
        progressLogArea = new JTextArea(10, 50);
        progressLogArea.setEditable(false);
        progressLogArea.setBackground(new Color(245, 245, 245));
        progressLogArea.setForeground(TEXT_FG);
        progressLogArea.setFont(new Font("Courier", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(progressLogArea);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCompletePage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel successLabel = new JLabel("✓ Installation Complete!");
        successLabel.setFont(new Font("Arial", Font.BOLD, 20));
        successLabel.setForeground(SUCCESS_FG);
        gbc.gridy = 0;
        panel.add(successLabel, gbc);
        
        gbc.gridy++;
        JLabel pathLabel = new JLabel("Installation Path: " + 
            (installLocation != null ? installLocation.getAbsolutePath() : ""));
        pathLabel.setForeground(TEXT_FG);
        panel.add(pathLabel, gbc);
        
        gbc.gridy++;
        JLabel launchLabel = new JLabel("To launch the application:");
        launchLabel.setForeground(TEXT_FG);
        launchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(launchLabel, gbc);
        
        gbc.gridy++;
        String launchCmd = System.getProperty("os.name").toLowerCase().contains("win") ?
            "run.bat" : "./run-with-env.sh";
        JLabel cmdLabel = new JLabel("$ " + launchCmd);
        cmdLabel.setForeground(new Color(100, 100, 150));
        cmdLabel.setFont(new Font("Courier", Font.PLAIN, 12));
        panel.add(cmdLabel, gbc);
        
        return panel;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(LIGHT_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        footer.setBorder(new EmptyBorder(15, 10, 15, 10));
        
        backButton = new JButton("⬅ Back");
        styleButton(backButton, false);
        backButton.addActionListener(e -> goToPreviousPage());
        footer.add(backButton);
        
        nextButton = new JButton("Next ➜");
        styleButton(nextButton, true);
        nextButton.addActionListener(e -> goToNextPage());
        footer.add(nextButton);
        
        cancelButton = new JButton("✕ Cancel");
        styleButton(cancelButton, false);
        cancelButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel installation?", 
                "Confirm Cancel", 
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        footer.add(cancelButton);
        
        return footer;
    }
    
    private void styleButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isPrimary) {
            button.setBackground(ACCENT_COLOR);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(8, 20, 8, 20));
            button.setOpaque(true);
        } else {
            button.setBackground(new Color(243, 244, 246));
            button.setForeground(TEXT_FG);
            button.setBorder(new LineBorder(BORDER_COLOR, 1));
            button.setOpaque(true);
        }
    }
    
    private void addLabelField(JPanel panel, GridBagConstraints gbc, String label, String value, java.util.function.Consumer<String> setter) {
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(TEXT_FG);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        JTextField field = new JTextField(30);
        field.setText(value);
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_FG);
        field.setCaretColor(TEXT_FG);
        field.addActionListener(e -> setter.accept(field.getText()));
        panel.add(field, gbc);
        
        gbc.gridx = 0;
    }
    
    private String checkJavaVersion() {
        try {
            String version = System.getProperty("java.version");
            // Extract major version
            int major = Integer.parseInt(version.split("\\.")[0]);
            if (major >= 8) {
                return "✓ Java " + major + " found";
            }
        } catch (Exception e) {
            // ignore
        }
        return "✗ Java 8+ required";
    }
    
    private String checkOracleDatabase() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // Try to get version info
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe", "system", "oracle");
            if (conn != null) {
                conn.close();
                return "✓ Oracle detected";
            }
        } catch (Exception e) {
            // Oracle not accessible
        }
        return "✗ Oracle 10g+ required";
    }
    
    private void goToPage(String pageName) {
        currentPage = pageName;
        cardLayout.show(cardPanel, pageName);
        updateButtonState();
    }
    
    private void goToNextPage() {
        switch (currentPage) {
            case PAGE_WELCOME:
                goToPage(PAGE_LOCATION);
                break;
            case PAGE_LOCATION:
                // Get path from location field
                String path = JOptionPane.showInputDialog(this,
                    "Enter installation path:",
                    System.getProperty("user.home") + File.separator + "LMS");
                if (path != null && !path.trim().isEmpty()) {
                    File testPath = new File(path.trim());
                    if (isValidInstallPath(testPath)) {
                        installLocation = testPath;
                        goToPage(PAGE_CHECK);
                    } else {
                        showError("Invalid installation path:\n" +
                                 "- Must be writable\n" +
                                 "- Cannot contain spaces (recommended)");
                    }
                }
                break;
            case PAGE_CHECK:
                goToPage(PAGE_ADMIN);
                break;
            case PAGE_ADMIN:
                // Validate admin details before proceeding
                String[] validation = validateAdminForm();
                if (validation[0].equals("OK")) {
                    // Extract values from form
                    JPanel adminPanel = (JPanel) cardPanel.getComponent(2); // PAGE_ADMIN
                    extractAdminFormValues(adminPanel);
                    goToPage(PAGE_PROGRESS);
                    performInstallation();
                } else {
                    showError("Validation Error:\n" + validation[1]);
                }
                break;
            case PAGE_COMPLETE:
                System.exit(0);
                break;
        }
    }
    
    private boolean isValidInstallPath(File path) {
        // Check if path is writable (create temp file)
        try {
            if (!path.exists()) {
                path.mkdirs();
            }
            File testFile = new File(path, ".lms_test");
            testFile.createNewFile();
            testFile.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private String[] validateAdminForm() {
        // Email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        
        // User ID: 2-5 alphanumeric characters
        if (adminUserId == null || adminUserId.trim().isEmpty()) {
            return new String[]{"FAIL", "User ID cannot be empty"};
        }
        if (adminUserId.length() < 2 || adminUserId.length() > 5) {
            return new String[]{"FAIL", "User ID must be 2-5 characters"};
        }
        if (!adminUserId.matches("^[A-Za-z0-9]+$")) {
            return new String[]{"FAIL", "User ID must contain only letters and numbers"};
        }
        
        // Name validation
        if (adminName == null || adminName.trim().isEmpty()) {
            return new String[]{"FAIL", "Name cannot be empty"};
        }
        if (adminName.length() > 50) {
            return new String[]{"FAIL", "Name must not exceed 50 characters"};
        }
        
        // Email validation
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            return new String[]{"FAIL", "Email cannot be empty"};
        }
        if (!adminEmail.matches(emailRegex)) {
            return new String[]{"FAIL", "Invalid email format"};
        }
        
        // Phone validation (10 digits)
        if (adminPhone == null || adminPhone.trim().isEmpty()) {
            return new String[]{"FAIL", "Phone cannot be empty"};
        }
        if (!adminPhone.matches("^[0-9]{10}$")) {
            return new String[]{"FAIL", "Phone must be exactly 10 digits"};
        }
        
        // Password validation (min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit)
        if (adminPassword == null || adminPassword.isEmpty()) {
            return new String[]{"FAIL", "Password cannot be empty"};
        }
        if (adminPassword.length() < 8) {
            return new String[]{"FAIL", "Password must be at least 8 characters"};
        }
        if (!adminPassword.matches(".*[A-Z].*")) {
            return new String[]{"FAIL", "Password must contain at least one uppercase letter"};
        }
        if (!adminPassword.matches(".*[a-z].*")) {
            return new String[]{"FAIL", "Password must contain at least one lowercase letter"};
        }
        if (!adminPassword.matches(".*[0-9].*")) {
            return new String[]{"FAIL", "Password must contain at least one digit"};
        }
        
        // Password confirmation match
        if (adminPasswordConfirm == null || !adminPassword.equals(adminPasswordConfirm)) {
            return new String[]{"FAIL", "Passwords do not match"};
        }
        
        return new String[]{"OK", ""};
    }
    
    private void extractAdminFormValues(JPanel panel) {
        // Values are already stored via focus listeners
        // This method is here for future extensibility
    }
    
    private void goToPreviousPage() {
        switch (currentPage) {
            case PAGE_LOCATION:
                goToPage(PAGE_WELCOME);
                break;
            case PAGE_CHECK:
                goToPage(PAGE_LOCATION);
                break;
            case PAGE_ADMIN:
                goToPage(PAGE_CHECK);
                break;
        }
    }
    
    private void updateButtonState() {
        backButton.setEnabled(!currentPage.equals(PAGE_WELCOME));
        nextButton.setEnabled(!currentPage.equals(PAGE_PROGRESS));
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void performInstallation() {
        // Run installation in background thread
        new Thread(() -> {
            try {
                // Create installation manager with progress listener
                InstallationManager manager = new InstallationManager(
                    installLocation, adminUserId, adminName, adminEmail, 
                    adminPhone, adminPassword,
                    new InstallationManager.InstallationProgressListener() {
                        @Override
                        public void onProgress(String message) {
                            if (progressLogArea != null) {
                                progressLogArea.append(message + "\n");
                                progressLogArea.setCaretPosition(progressLogArea.getDocument().getLength());
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            if (progressLogArea != null) {
                                progressLogArea.append("ERROR: " + error + "\n");
                            }
                            SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(LMSSetupWizard.this, 
                                    error, "Installation Error", JOptionPane.ERROR_MESSAGE)
                            );
                        }
                        
                        @Override
                        public void onComplete() {
                            SwingUtilities.invokeLater(() -> goToPage(PAGE_COMPLETE));
                        }
                    }
                );
                
                // Start installation
                manager.install();
                
            } catch (Exception e) {
                showError("Installation failed: " + e.getMessage());
                goToPage(PAGE_ADMIN);
            }
        }).start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LMSSetupWizard());
    }
}
