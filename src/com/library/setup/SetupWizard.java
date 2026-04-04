package com.library.setup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Main GUI Setup Wizard for LMS Installation
 * Cross-platform installer with dependency checking and admin user creation
 */
public class SetupWizard extends JFrame {
    
    private static final Color COLOR_PRIMARY = new Color(31, 62, 109);
    private static final Color COLOR_SUCCESS = new Color(34, 139, 34);
    private static final Color COLOR_WARNING = new Color(255, 140, 0);
    private static final Color COLOR_ERROR = new Color(220, 20, 60);
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    private SystemEnvironment environment;
    private File installLocation;
    private boolean freshInstall = true;
    
    // Wizard pages
    private static final String PAGE_WELCOME = "WELCOME";
    private static final String PAGE_LOCATION = "LOCATION";
    private static final String PAGE_ENVIRONMENT = "ENVIRONMENT";
    private static final String PAGE_DATABASE = "DATABASE";
    private static final String PAGE_ADMIN = "ADMIN";
    private static final String PAGE_PROGRESS = "PROGRESS";
    private static final String PAGE_COMPLETE = "COMPLETE";
    
    public SetupWizard() {
        setTitle("LMS Setup Wizard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // Initialize environment scanner
        environment = new SystemEnvironment();
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);
        
        // Content (Card Layout for wizard pages)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        contentPanel.add(createWelcomePage(), PAGE_WELCOME);
        contentPanel.add(createLocationPage(), PAGE_LOCATION);
        contentPanel.add(createEnvironmentPage(), PAGE_ENVIRONMENT);
        contentPanel.add(createDatabasePage(), PAGE_DATABASE);
        contentPanel.add(createAdminPage(), PAGE_ADMIN);
        contentPanel.add(createProgressPage(), PAGE_PROGRESS);
        contentPanel.add(createCompletePage(), PAGE_COMPLETE);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel title = new JLabel("Library Management System - Setup Wizard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Cross-platform installer for Windows & Linux");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(200, 200, 200));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(COLOR_PRIMARY);
        textPanel.add(title);
        textPanel.add(subtitle);
        
        header.add(textPanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createWelcomePage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JTextArea welcomeText = new JTextArea();
        welcomeText.setText(
            "Welcome to the Library Management System Setup Wizard!\n\n" +
            "This wizard will guide you through the installation process.\n\n" +
            "The setup will:\n" +
            "  • Check your system for required dependencies (Java 8, Oracle 10g XE)\n" +
            "  • Install or configure the LMS application\n" +
            "  • Set up the database schema\n" +
            "  • Create your administrator account\n" +
            "  • Generate desktop shortcuts for easy access\n\n" +
            "Detected System:\n" +
            "  OS: " + environment.getOsName() + " " + environment.getOsVersion() + "\n" +
            "  Type: " + environment.getOsType() + "\n\n" +
            "Click 'Next' to continue."
        );
        welcomeText.setEditable(false);
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeText.setLineWrap(true);
        welcomeText.setWrapStyleWord(true);
        welcomeText.setBackground(Color.WHITE);
        welcomeText.setForeground(Color.DARK_GRAY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnNext = new JButton("Next >");
        styleButton(btnNext, COLOR_PRIMARY);
        btnNext.addActionListener(e -> cardLayout.show(contentPanel, PAGE_LOCATION));
        
        JButton btnCancel = new JButton("Cancel");
        styleButton(btnCancel, Color.GRAY);
        btnCancel.addActionListener(e -> handleExit());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnNext);
        
        page.add(welcomeText, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private JPanel createLocationPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitle = new JLabel("Choose Installation Location");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        formPanel.add(lblTitle, gbc);
        
        JLabel lblInfo = new JLabel("Select where you want to install the Library Management System:");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 1;
        formPanel.add(lblInfo, gbc);
        
        String defaultPath = environment.getOsType() == SystemEnvironment.OSType.WINDOWS 
            ? "C:\\Program Files\\LMS" 
            : System.getProperty("user.home") + "/LMS";
        
        JTextField txtLocation = new JTextField(defaultPath);
        txtLocation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLocation.setPreferredSize(new Dimension(400, 35));
        gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(txtLocation, gbc);
        
        JButton btnBrowse = new JButton("Browse...");
        styleButton(btnBrowse, COLOR_PRIMARY);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(btnBrowse, gbc);
        
        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(new File(defaultPath).getParentFile());
            if (chooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
                txtLocation.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnBack = new JButton("< Back");
        styleButton(btnBack, Color.GRAY);
        btnBack.addActionListener(e -> cardLayout.show(contentPanel, PAGE_WELCOME));
        
        JButton btnNext = new JButton("Next >");
        styleButton(btnNext, COLOR_PRIMARY);
        btnNext.addActionListener(e -> {
            String path = txtLocation.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an installation location.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            installLocation = new File(path);
            cardLayout.show(contentPanel, PAGE_ENVIRONMENT);
        });
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnNext);
        
        page.add(formPanel, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private JPanel createEnvironmentPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JLabel lblTitle = new JLabel("Dependency Check");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        
        JTextArea txtSummary = new JTextArea();
        txtSummary.setEditable(false);
        txtSummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSummary.setText(environment.getSummary());
        txtSummary.setBackground(new Color(245, 245, 245));
        txtSummary.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane scroll = new JScrollPane(txtSummary);
        scroll.setPreferredSize(new Dimension(600, 300));
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statusPanel.setBackground(Color.WHITE);
        
        if (environment.isEnvironmentReady()) {
            JLabel lblStatus = new JLabel("✓ All dependencies met. Ready to install!");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStatus.setForeground(COLOR_SUCCESS);
            statusPanel.add(lblStatus);
        } else {
            JLabel lblStatus = new JLabel("⚠ Some dependencies are missing or incompatible");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStatus.setForeground(COLOR_WARNING);
            statusPanel.add(lblStatus);
            
            if (!environment.isJavaInstalled() || !environment.isJavaCompatible()) {
                JButton btnInstallJava = new JButton("Install Java 8");
                styleButton(btnInstallJava, COLOR_WARNING);
                btnInstallJava.addActionListener(e -> DependencyInstaller.showInstallInstructions(this, "Java 8", environment.getOsType()));
                statusPanel.add(btnInstallJava);
            }
            
            if (!environment.isOracleInstalled()) {
                JButton btnInstallOracle = new JButton("Install Oracle 10g XE");
                styleButton(btnInstallOracle, COLOR_WARNING);
                btnInstallOracle.addActionListener(e -> DependencyInstaller.showInstallInstructions(this, "Oracle 10g XE", environment.getOsType()));
                statusPanel.add(btnInstallOracle);
            }
        }
        
        JPanel topPanel = new JPanel(new BorderLayout(0, 20));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(scroll, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.SOUTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnBack = new JButton("< Back");
        styleButton(btnBack, Color.GRAY);
        btnBack.addActionListener(e -> cardLayout.show(contentPanel, PAGE_LOCATION));
        
        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, COLOR_PRIMARY);
        btnRefresh.addActionListener(e -> {
            environment = new SystemEnvironment();
            contentPanel.remove(2);
            contentPanel.add(createEnvironmentPage(), PAGE_ENVIRONMENT, 2);
            cardLayout.show(contentPanel, PAGE_ENVIRONMENT);
        });
        
        JButton btnNext = new JButton("Next >");
        styleButton(btnNext, COLOR_PRIMARY);
        btnNext.addActionListener(e -> cardLayout.show(contentPanel, PAGE_DATABASE));
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnNext);
        
        page.add(topPanel, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private JPanel createDatabasePage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JLabel lblTitle = new JLabel("Database Configuration");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        
        // Connection form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtHost = new JTextField("localhost", 20);
        JTextField txtPort = new JTextField("1521", 20);
        JTextField txtSid = new JTextField("xe", 20);
        JTextField txtUsername = new JTextField("system", 20);
        JPasswordField txtPassword = new JPasswordField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtHost, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPort, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("SID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtSid, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);
        
        JTextArea txtLog = new JTextArea(10, 40);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(scrollLog, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout(0, 20));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnBack = new JButton("< Back");
        styleButton(btnBack, Color.GRAY);
        btnBack.addActionListener(e -> cardLayout.show(contentPanel, PAGE_ENVIRONMENT));
        
        JButton btnTest = new JButton("Test Connection");
        styleButton(btnTest, COLOR_PRIMARY);
        btnTest.addActionListener(e -> {
            txtLog.setText("");
            txtLog.append("Testing database connection...\n");
            try {
                String url = "jdbc:oracle:thin:@" + txtHost.getText() + ":" + txtPort.getText() + ":" + txtSid.getText();
                DatabaseInitializer dbInit = new DatabaseInitializer(url, txtUsername.getText(), new String(txtPassword.getPassword()));
                
                if (dbInit.testConnection()) {
                    txtLog.append("✓ Connection successful!\n");
                    boolean hasSchema = dbInit.schemaExists();
                    txtLog.append("Schema exists: " + hasSchema + "\n");
                    
                    if (hasSchema) {
                        boolean hasUsers = dbInit.hasExistingUsers();
                        txtLog.append("Existing users found: " + hasUsers + "\n");
                        if (hasUsers) {
                            txtLog.append("\nRecommendation: Use REPAIR mode to update schema without losing data.\n");
                            freshInstall = false;
                        }
                    } else {
                        txtLog.append("\nRecommendation: Fresh installation.\n");
                        freshInstall = true;
                    }
                } else {
                    txtLog.append("✗ Connection failed!\n");
                }
            } catch (Exception ex) {
                txtLog.append("✗ Error: " + ex.getMessage() + "\n");
            }
        });
        
        JButton btnNext = new JButton("Next >");
        styleButton(btnNext, COLOR_PRIMARY);
        btnNext.addActionListener(e -> {
            // Store connection details for later use
            System.setProperty("lms.db.host", txtHost.getText());
            System.setProperty("lms.db.port", txtPort.getText());
            System.setProperty("lms.db.sid", txtSid.getText());
            System.setProperty("lms.db.username", txtUsername.getText());
            System.setProperty("lms.db.password", new String(txtPassword.getPassword()));
            cardLayout.show(contentPanel, PAGE_ADMIN);
        });
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnTest);
        buttonPanel.add(btnNext);
        
        page.add(topPanel, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private JPanel createAdminPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JLabel lblTitle = new JLabel("Create Administrator Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        
        // Admin form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtUsername = new JTextField("admin", 25);
        JPasswordField txtPassword = new JPasswordField(25);
        JPasswordField txtConfirmPassword = new JPasswordField(25);
        JTextField txtFullName = new JTextField(25);
        JTextField txtEmail = new JTextField(25);
        
        JLabel lblPasswordStrength = new JLabel(" ");
        lblPasswordStrength.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        
        // Password strength indicator
        txtPassword.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkPassword(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkPassword(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkPassword(); }
            
            void checkPassword() {
                String pwd = new String(txtPassword.getPassword());
                AdminUserCreator.PasswordValidation validation = AdminUserCreator.validatePassword(pwd);
                lblPasswordStrength.setText(validation.message);
                if (validation.isValid) {
                    lblPasswordStrength.setForeground(validation.isWeak ? COLOR_WARNING : COLOR_SUCCESS);
                } else {
                    lblPasswordStrength.setForeground(COLOR_ERROR);
                }
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtConfirmPassword, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(lblPasswordStrength, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtFullName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Email (Optional):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        
        JLabel lblInfo = new JLabel("<html>This account will have full administrative privileges.<br/>" +
                                     "You can create additional users after installation.</html>");
        lblInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        centerPanel.add(lblInfo, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout(0, 20));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnBack = new JButton("< Back");
        styleButton(btnBack, Color.GRAY);
        btnBack.addActionListener(e -> cardLayout.show(contentPanel, PAGE_DATABASE));
        
        JButton btnNext = new JButton("Install");
        styleButton(btnNext, COLOR_SUCCESS);
        btnNext.addActionListener(e -> {
            // Validate form
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirmPassword.getPassword());
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AdminUserCreator.PasswordValidation validation = AdminUserCreator.validatePassword(password);
            if (!validation.isValid) {
                JOptionPane.showMessageDialog(this, validation.message, "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Store admin details for installation
            System.setProperty("lms.admin.username", username);
            System.setProperty("lms.admin.password", password);
            System.setProperty("lms.admin.fullname", fullName);
            System.setProperty("lms.admin.email", email);
            
            cardLayout.show(contentPanel, PAGE_PROGRESS);
            performInstallation();
        });
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnNext);
        
        page.add(topPanel, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private JProgressBar installProgressBar;
    private JTextArea installLog;
    
    private JPanel createProgressPage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JLabel lblTitle = new JLabel("Installing Library Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        
        installProgressBar = new JProgressBar(0, 100);
        installProgressBar.setStringPainted(true);
        installProgressBar.setPreferredSize(new Dimension(500, 30));
        
        installLog = new JTextArea(15, 50);
        installLog.setEditable(false);
        installLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(installLog);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(installProgressBar, BorderLayout.NORTH);
        centerPanel.add(scrollLog, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout(0, 20));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        
        page.add(topPanel, BorderLayout.CENTER);
        
        return page;
    }
    
    private JPanel createCompletePage() {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JLabel lblTitle = new JLabel("Installation Complete!");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_SUCCESS);
        
        JTextArea txtSummary = new JTextArea();
        txtSummary.setEditable(false);
        txtSummary.setOpaque(false);
        txtSummary.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSummary.setText(
            "Library Management System has been successfully installed.\n\n" +
            "Installation Directory: " + (installLocation != null ? installLocation.getAbsolutePath() : "N/A") + "\n\n" +
            "You can now:\n" +
            "• Launch LMS from the desktop shortcut\n" +
            "• Find it in your Start Menu / Application Menu\n" +
            "• Run the launcher script from the installation directory\n\n" +
            "Thank you for installing LMS!"
        );
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 30));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(txtSummary, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout(0, 30));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnLaunch = new JButton("Launch LMS");
        styleButton(btnLaunch, COLOR_SUCCESS);
        btnLaunch.addActionListener(e -> {
            try {
                if (installLocation != null) {
                    File launcher = new File(installLocation, 
                        environment.getOsType() == SystemEnvironment.OSType.WINDOWS ? "LMS-Launcher.bat" : "LMS-Launcher.sh");
                    if (launcher.exists()) {
                        Runtime.getRuntime().exec(launcher.getAbsolutePath());
                    }
                }
                System.exit(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to launch: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton btnFinish = new JButton("Finish");
        styleButton(btnFinish, COLOR_PRIMARY);
        btnFinish.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(btnLaunch);
        buttonPanel.add(btnFinish);
        
        page.add(topPanel, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private JPanel createPlaceholderPage(String title, String message, String prevPage, String nextPage) {
        JPanel page = new JPanel(new BorderLayout(20, 20));
        page.setBackground(Color.WHITE);
        page.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        
        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(lblTitle);
        centerPanel.add(lblMessage);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        if (prevPage != null) {
            JButton btnBack = new JButton("< Back");
            styleButton(btnBack, Color.GRAY);
            btnBack.addActionListener(e -> cardLayout.show(contentPanel, prevPage));
            buttonPanel.add(btnBack);
        }
        
        if (nextPage != null) {
            JButton btnNext = new JButton("Next >");
            styleButton(btnNext, COLOR_PRIMARY);
            btnNext.addActionListener(e -> cardLayout.show(contentPanel, nextPage));
            buttonPanel.add(btnNext);
        } else {
            JButton btnFinish = new JButton("Finish");
            styleButton(btnFinish, COLOR_SUCCESS);
            btnFinish.addActionListener(e -> System.exit(0));
            buttonPanel.add(btnFinish);
        }
        
        page.add(centerPanel, BorderLayout.CENTER);
        page.add(buttonPanel, BorderLayout.SOUTH);
        
        return page;
    }
    
    private void performInstallation() {
        new Thread(() -> {
            try {
                installLog.setText("");
                logInstall("Starting installation...\n");
                
                // Step 1: Copy files to installation directory
                installProgressBar.setValue(10);
                logInstall("Preparing installation directory...\n");
                prepareInstallationDirectory();
                
                // Step 2: Compile sources
                installProgressBar.setValue(20);
                logInstall("Compiling Java sources...\n");
                LauncherGenerator launcher = new LauncherGenerator(installLocation, environment.getOsType());
                launcher.compileAllSources();
                logInstall("✓ Compilation successful\n");
                
                // Step 3: Initialize database
                installProgressBar.setValue(40);
                logInstall("Initializing database...\n");
                String url = "jdbc:oracle:thin:@" + System.getProperty("lms.db.host") + ":" + 
                             System.getProperty("lms.db.port") + ":" + System.getProperty("lms.db.sid");
                DatabaseInitializer dbInit = new DatabaseInitializer(url, 
                    System.getProperty("lms.db.username"), System.getProperty("lms.db.password"));
                
                if (freshInstall) {
                    logInstall("Performing fresh installation...\n");
                    File scriptSql = new File(installLocation, "script.sql");
                    File dummySql = new File(installLocation, "dummy.sql");
                    dbInit.performFreshInstall(scriptSql, dummySql, new DatabaseInitializer.ProgressCallback() {
                        public void onProgress(int percentage, String message) {
                            logInstall(message);
                        }
                    });
                } else {
                    logInstall("Repairing existing installation...\n");
                    File scriptSql = new File(installLocation, "script.sql");
                    dbInit.performRepair(scriptSql, new DatabaseInitializer.ProgressCallback() {
                        public void onProgress(int percentage, String message) {
                            logInstall(message);
                        }
                    });
                }
                logInstall("✓ Database initialized\n");
                
                // Step 4: Create admin user
                installProgressBar.setValue(60);
                logInstall("Creating administrator account...\n");
                AdminUserCreator adminCreator = new AdminUserCreator(dbInit.getConnection());
                String userId = adminCreator.createAdminUser(
                    System.getProperty("lms.admin.username"),
                    System.getProperty("lms.admin.password"),
                    System.getProperty("lms.admin.fullname"),
                    System.getProperty("lms.admin.email")
                );
                logInstall("✓ Admin user created: " + userId + "\n");
                
                // Step 5: Generate launcher scripts
                installProgressBar.setValue(75);
                logInstall("Generating launcher scripts...\n");
                File launcherScript = launcher.generateLauncher();
                logInstall("✓ Launcher created: " + launcherScript.getName() + "\n");
                
                // Step 6: Create desktop shortcuts
                installProgressBar.setValue(85);
                logInstall("Creating desktop shortcuts...\n");
                ShortcutCreator shortcutCreator = new ShortcutCreator(installLocation, launcherScript, environment.getOsType());
                shortcutCreator.createShortcut();
                logInstall("✓ Shortcuts created\n");
                
                // Step 7: Cleanup
                installProgressBar.setValue(95);
                logInstall("Finalizing installation...\n");
                
                installProgressBar.setValue(100);
                logInstall("\n✓✓✓ Installation completed successfully! ✓✓✓\n");
                
                // Move to completion page
                SwingUtilities.invokeLater(() -> {
                    cardLayout.show(contentPanel, PAGE_COMPLETE);
                });
                
            } catch (Exception e) {
                logInstall("\n✗ Installation failed: " + e.getMessage() + "\n");
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(SetupWizard.this,
                        "Installation failed:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void prepareInstallationDirectory() throws Exception {
        // In a real installer, we would copy files from a bundled JAR
        // For now, we assume the current directory structure is correct
        logInstall("Installation directory: " + installLocation.getAbsolutePath() + "\n");
        
        // Ensure required directories exist
        new File(installLocation, "bin").mkdirs();
        new File(installLocation, "lib").mkdirs();
        new File(installLocation, "src").mkdirs();
        new File(installLocation, "docs").mkdirs();
    }
    
    private void logInstall(String message) {
        SwingUtilities.invokeLater(() -> {
            installLog.append(message);
            installLog.setCaretPosition(installLog.getDocument().getLength());
        });
    }
    
    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel the installation?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            SetupWizard wizard = new SetupWizard();
            wizard.setVisible(true);
        });
    }
}
