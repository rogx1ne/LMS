package com.library.ui;

import com.library.dao.UserDAO;
import com.library.model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class LoginFrame extends JFrame {

    // --- COLOR PALETTE ---
    private static final Color COLOR_BLUE_DARK  = new Color(31, 62, 109); 
    private static final Color COLOR_GREEN      = new Color(46, 125, 50); 
    private static final Color COLOR_WHITE      = Color.WHITE;
    private static final Color COLOR_BLACK      = Color.BLACK;

    private JTextField txtUserId;
    private JPasswordField txtPassword;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Library Management System");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // MAIN CONTAINER
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        setContentPane(mainPanel);

        // --- LEFT PANEL (BRANDING) ---
        JPanel pnlLeft = new JPanel();
        pnlLeft.setBackground(COLOR_WHITE); 
        pnlLeft.setLayout(new GridBagLayout());
        
        // Logo
        ImageIcon logoIcon = loadIconSafely("lib/icons/logo_full.png", 300, 300);
        JLabel lblLogo = new JLabel(logoIcon);
        pnlLeft.add(lblLogo);

        // --- RIGHT PANEL (LOGIN FORM) ---
        JPanel pnlRight = new JPanel();
        pnlRight.setBackground(COLOR_BLUE_DARK);
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBorder(new EmptyBorder(60, 60, 60, 60));

        // Form Title
        JLabel lblLoginTitle = new JLabel("LOGIN");
        lblLoginTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblLoginTitle.setForeground(COLOR_WHITE);
        lblLoginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Inputs
        JLabel lblUser = createLabel("UID");
        txtUserId = createSharpField();
        
        JLabel lblPass = createLabel("PASSWORD");
        txtPassword = createSharpPasswordField();

        // Login Button
        JButton btnLogin = createSharpButton("LOGIN");

        // Footer Links
        JPanel pnlLinks = new JPanel(new BorderLayout());
        pnlLinks.setBackground(COLOR_BLUE_DARK);
        pnlLinks.setMaximumSize(new Dimension(350, 30));
        pnlLinks.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCreate = createLink("CREATE USER");
        JLabel lblForgot = createLink("FORGOT PASSWORD");

        pnlLinks.add(lblCreate, BorderLayout.WEST);
        pnlLinks.add(lblForgot, BorderLayout.EAST);

        // Assemble Right Panel
        pnlRight.add(lblLoginTitle);
        pnlRight.add(Box.createVerticalStrut(40));
        pnlRight.add(lblUser);
        pnlRight.add(Box.createVerticalStrut(5));
        pnlRight.add(txtUserId);
        pnlRight.add(Box.createVerticalStrut(20));
        pnlRight.add(lblPass);
        pnlRight.add(Box.createVerticalStrut(5));
        pnlRight.add(txtPassword);
        pnlRight.add(Box.createVerticalStrut(30));
        pnlRight.add(btnLogin);
        pnlRight.add(Box.createVerticalStrut(20));
        pnlRight.add(pnlLinks);

        // Add to Frame
        mainPanel.add(pnlLeft);
        mainPanel.add(pnlRight);

        // --- EVENTS ---
        btnLogin.addActionListener(e -> {
            String uid = txtUserId.getText();
            String pwd = new String(txtPassword.getPassword());

            if (userDAO.validateLogin(uid, pwd)) {
                this.dispose();
                new DashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Link Actions
        lblCreate.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent e) { showSignupDialog(); } 
        });
        lblForgot.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent e) { showForgotPasswordDialog(); } 
        });
    }

    // ==========================================
    //       MAIN LOGIN UI HELPERS
    // ==========================================

    private JTextField createSharpField() {
        JTextField field = new JTextField();
        field.setUI(new BasicTextFieldUI()); 
        styleSharpInput(field);
        return field;
    }

    private JPasswordField createSharpPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setUI(new BasicPasswordFieldUI()); 
        styleSharpInput(field);
        return field;
    }

    private void styleSharpInput(JTextField field) {
        field.setPreferredSize(new Dimension(350, 45));
        field.setMaximumSize(new Dimension(350, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(COLOR_BLACK);
        field.setBackground(COLOR_WHITE);
        field.setCaretColor(COLOR_BLACK);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private JButton createSharpButton(String text) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Force remove Linux theme
        btn.setPreferredSize(new Dimension(150, 45));
        btn.setMaximumSize(new Dimension(150, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(46, 125, 50)); 
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setBackground(new Color(46, 125, 50)); 
                btn.setForeground(Color.WHITE); 
            }
            public void mouseExited(MouseEvent e) { 
                btn.setBackground(Color.WHITE); 
                btn.setForeground(new Color(46, 125, 50)); 
            }
        });
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
    
    private JLabel createLink(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(COLOR_WHITE);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private ImageIcon loadIconSafely(String path, int w, int h) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return null;
    }

    // ==========================================
    //           DIALOG LOGIC
    // ==========================================

    // 1. Create User Dialog (With Auto-ID)
    private void showSignupDialog() {
        JDialog d = new JDialog(this, "Create New User", true);
        d.setSize(400, 450); 
        d.setLocationRelativeTo(this);
        d.setResizable(false);

        JPanel pnlMain = new JPanel();
        pnlMain.setBackground(COLOR_WHITE);
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblTitle = new JLabel("REGISTER");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_BLACK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Inputs (Helpers are defined below now)
        JLabel lblName = createLabelForDialog("FULL NAME");
        JTextField txtName = createSharpFieldForDialog();
        
        JLabel lblPass = createLabelForDialog("PASSWORD");
        JPasswordField txtPass = createSharpPasswordFieldForDialog();
        
        JLabel lblSecQ = createLabelForDialog("SECURITY QUESTION");
        JTextField txtSecQ = createSharpFieldForDialog();
        
        JLabel lblSecA = createLabelForDialog("SECURITY ANSWER");
        JTextField txtSecA = createSharpFieldForDialog();

        JButton btnRegister = createSharpButtonForDialog("REGISTER");

        // Add to Panel
        pnlMain.add(lblTitle); pnlMain.add(Box.createVerticalStrut(20));
        
        pnlMain.add(lblName); pnlMain.add(txtName); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblPass); pnlMain.add(txtPass); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblSecQ); pnlMain.add(txtSecQ); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblSecA); pnlMain.add(txtSecA); pnlMain.add(Box.createVerticalStrut(20));
        
        pnlMain.add(btnRegister);

        d.add(pnlMain);

        // Logic
        btnRegister.addActionListener(e -> {
            String name = txtName.getText();
            String pwd = new String(txtPass.getPassword());
            String ques = txtSecQ.getText();
            String ans = txtSecA.getText();

            if(name.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Name and Password are required.");
                return;
            }

            // 1. GENERATE ID AUTOMATICALLY
            String autoId = userDAO.generateNextUserId();

            // 2. Create User Object
            User newUser = new User(autoId, name, pwd, ques, ans);
            
            // 3. Save to DB
            if (userDAO.addUser(newUser)) {
                String message = "User Created Successfully!\n\nYOUR USER ID IS: " + autoId + "\n\nPlease memorize this ID.";
                JOptionPane.showMessageDialog(d, message, "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "Error creating user. Please try again.");
            }
        });
        d.setVisible(true);
    }

    // 2. Forgot Password Dialog
    private void showForgotPasswordDialog() {
        String uid = JOptionPane.showInputDialog(this, "Enter your User ID:");
        if (uid == null || uid.trim().isEmpty()) return;

        String question = userDAO.getSecurityQuestion(uid);
        if (question == null) {
            JOptionPane.showMessageDialog(this, "User ID not found!");
            return;
        }

        String ans = JOptionPane.showInputDialog(this, "Security Question: " + question + "\n\nEnter your Answer:");
        if (ans == null || ans.trim().isEmpty()) return;

        if (userDAO.validateSecurityAnswer(uid, ans)) {
            String newPass = JOptionPane.showInputDialog(this, "Identity Verified!\n\nEnter New Password:");
            if (newPass != null && !newPass.trim().isEmpty()) {
                if (userDAO.updatePasswordOnly(uid, newPass)) {
                    JOptionPane.showMessageDialog(this, "Password Reset Successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error updating password.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect Security Answer!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    //    DIALOG HELPERS (THESE WERE MISSING)
    // ==========================================
    
    private JLabel createLabelForDialog(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(COLOR_BLACK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createSharpFieldForDialog() {
        JTextField field = new JTextField();
        field.setUI(new BasicTextFieldUI()); // Clean look
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_BLACK);
        field.setPreferredSize(new Dimension(300, 35));
        field.setMaximumSize(new Dimension(300, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private JPasswordField createSharpPasswordFieldForDialog() {
        JPasswordField field = new JPasswordField();
        field.setUI(new BasicPasswordFieldUI()); // Clean look
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_BLACK);

        field.setPreferredSize(new Dimension(300, 35));
        field.setMaximumSize(new Dimension(300, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private JButton createSharpButtonForDialog(String text) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Force remove Linux theme
        btn.setMaximumSize(new Dimension(300, 40));
        btn.setBackground(COLOR_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}