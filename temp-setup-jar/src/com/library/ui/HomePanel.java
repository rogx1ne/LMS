package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildCenter(), BorderLayout.CENTER);
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(ModuleTheme.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // --- LOGO ---
        ImageIcon logoIcon = loadIconSafely("lib/icons/logo_full.png", 400, 400);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            center.add(lblLogo, gbc);
        }

        // --- WELCOME MESSAGE ---
        gbc.gridy = 1;
        JLabel lblWelcome = new JLabel("Welcome to Library Management System");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblWelcome.setForeground(ModuleTheme.BLUE_DARK);
        center.add(lblWelcome, gbc);

        gbc.gridy = 2;
        JLabel lblSub = new JLabel("Use the Global Search at the top or the sidebar to navigate.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSub.setForeground(Color.GRAY);
        center.add(lblSub, gbc);

        return center;
    }

    private ImageIcon loadIconSafely(String path, int w, int h) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return null;
    }
}
