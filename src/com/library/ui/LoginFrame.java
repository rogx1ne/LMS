package com.library.ui;

import com.library.dao.UserDAO;
import com.library.service.CurrentUserContext;
import com.library.service.PasswordResetOtpService;
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
    private final PasswordResetOtpService otpService = new PasswordResetOtpService();

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

        JLabel lblForgot = createLink("FORGOT PASSWORD?");

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
            String uid = txtUserId.getText().trim();
            String pwd = new String(txtPassword.getPassword());

            if (userDAO.validateLogin(uid, pwd)) {
                String display = userDAO.getDisplayName(uid);
                String role = userDAO.getUserRole(uid);
                CurrentUserContext.setUser(uid, display, role);
                this.dispose();
                new DashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Link Actions
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

    // 1. Forgot Password Dialog
    private void showForgotPasswordDialog() {
        JDialog d = new JDialog(this, "Forgot Password", true);
        d.setSize(400, 480);
        d.setLocationRelativeTo(this);
        d.setResizable(false);

        JPanel pnlMain = new JPanel();
        pnlMain.setBackground(COLOR_WHITE);
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblTitle = new JLabel("RESET PASSWORD");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_BLACK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUid = createLabelForDialog("USER ID");
        JTextField txtUid = createSharpFieldForDialog();

        JLabel lblEmailInfo = createLabelForDialog("REGISTERED EMAIL");
        JLabel txtEmailInfo = new JLabel("-");
        txtEmailInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtEmailInfo.setForeground(new Color(70, 70, 70));
        txtEmailInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtEmailInfo.setMaximumSize(new Dimension(300, 25));

        JLabel lblOtp = createLabelForDialog("OTP");
        JTextField txtOtp = createSharpFieldForDialog();

        JLabel lblPass = createLabelForDialog("NEW PASSWORD");
        JPasswordField txtPass = createSharpPasswordFieldForDialog();
        JLabel lblConfirm = createLabelForDialog("CONFIRM PASSWORD");
        JPasswordField txtConfirm = createSharpPasswordFieldForDialog();

        JButton btnSendOtp = createSharpButtonForDialog("SEND OTP");
        JButton btnVerifyOtp = createSharpButtonForDialog("VERIFY OTP");
        JButton btnReset = createSharpButtonForDialog("RESET");

        txtOtp.setEnabled(false);
        btnVerifyOtp.setEnabled(false);
        txtPass.setEnabled(false);
        txtConfirm.setEnabled(false);
        btnReset.setEnabled(false);

        // Add to Panel
        pnlMain.add(lblTitle); pnlMain.add(Box.createVerticalStrut(20));

        pnlMain.add(lblUid); pnlMain.add(txtUid); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblEmailInfo); pnlMain.add(txtEmailInfo); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(btnSendOtp); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblOtp); pnlMain.add(txtOtp); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(btnVerifyOtp); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblPass); pnlMain.add(txtPass); pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblConfirm); pnlMain.add(txtConfirm); pnlMain.add(Box.createVerticalStrut(20));

        pnlMain.add(btnReset);

        d.add(pnlMain);

        final String[] generatedOtp = new String[]{null};
        final long[] otpSentAt = new long[]{0L};
        final boolean[] otpVerified = new boolean[]{false};
        final int otpValidityMs = 10 * 60 * 1000; // 10 minutes

        btnSendOtp.addActionListener(e -> {
            String uid = txtUid.getText().trim();
            if (uid.isEmpty()) {
                JOptionPane.showMessageDialog(d, "User ID is required.");
                return;
            }

            String email = userDAO.getActiveUserEmail(uid);
            if (email == null || email.trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "User ID does not exist.");
                return;
            }

            try {
                String otp = otpService.generateOtp();
                otpService.sendOtp(email, uid, otp);
                generatedOtp[0] = otp;
                otpSentAt[0] = System.currentTimeMillis();
                otpVerified[0] = false;

                txtEmailInfo.setText(maskEmail(email));
                txtOtp.setEnabled(true);
                btnVerifyOtp.setEnabled(true);
                txtPass.setEnabled(false);
                txtConfirm.setEnabled(false);
                btnReset.setEnabled(false);
                txtPass.setText("");
                txtConfirm.setText("");

                JOptionPane.showMessageDialog(d, "OTP sent to registered email.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Unable to send OTP: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnVerifyOtp.addActionListener(e -> {
            String uid = txtUid.getText().trim();
            String otpInput = txtOtp.getText().trim();

            if (uid.isEmpty()) {
                JOptionPane.showMessageDialog(d, "User ID is required.");
                return;
            }

            if (generatedOtp[0] == null) {
                JOptionPane.showMessageDialog(d, "Please send OTP first.");
                return;
            }
            if (otpInput.isEmpty()) {
                JOptionPane.showMessageDialog(d, "OTP is required.");
                return;
            }
            if ((System.currentTimeMillis() - otpSentAt[0]) > otpValidityMs) {
                JOptionPane.showMessageDialog(d, "OTP expired. Please request a new OTP.");
                return;
            }
            if (!generatedOtp[0].equals(otpInput)) {
                JOptionPane.showMessageDialog(d, "OTP does not match.");
                return;
            }

            otpVerified[0] = true;
            txtPass.setEnabled(true);
            txtConfirm.setEnabled(true);
            btnReset.setEnabled(true);
            JOptionPane.showMessageDialog(d, "OTP verified. Enter new password.");
        });

        // Reset logic
        btnReset.addActionListener(e -> {
            String uid = txtUid.getText().trim();
            String pwd = new String(txtPass.getPassword());
            String confirm = new String(txtConfirm.getPassword());

            if (uid.isEmpty()) {
                JOptionPane.showMessageDialog(d, "User ID is required.");
                return;
            }
            if (!otpVerified[0]) {
                JOptionPane.showMessageDialog(d, "Please verify OTP first.");
                return;
            }
            if (pwd.isEmpty()) {
                JOptionPane.showMessageDialog(d, "New Password is required.");
                return;
            }
            if (pwd.length() > 10) {
                JOptionPane.showMessageDialog(d, "Password must be 10 characters or less.");
                return;
            }

            if (!pwd.equals(confirm)) {
                JOptionPane.showMessageDialog(d, "Confirm Password does not match.");
                return;
            }

            if (userDAO.updatePasswordByUserId(uid, pwd)) {
                JOptionPane.showMessageDialog(d, "Password reset successful. Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "Unable to reset password.");
            }
        });
        d.setVisible(true);
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        String value = email.trim();
        int at = value.indexOf('@');
        if (at <= 1) return value;
        String local = value.substring(0, at);
        String domain = value.substring(at);
        if (local.length() <= 2) return local.charAt(0) + "*" + domain;
        return local.substring(0, 2) + "****" + domain;
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
