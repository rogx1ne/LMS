package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new GridBagLayout());
        setBackground(ModuleTheme.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // --- LOGO ---
        ImageIcon logoIcon = loadIconSafely("lib/icons/logo_full.png", 350, 350);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            add(lblLogo, gbc);
        }

        // --- WELCOME MESSAGE ---
        gbc.gridy = 1;
        JLabel lblWelcome = new JLabel("Welcome to Library Management System");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(ModuleTheme.BLUE_DARK);
        add(lblWelcome, gbc);

        gbc.gridy = 2;
        JLabel lblSub = new JLabel("Select a module from the sidebar to get started.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSub.setForeground(Color.GRAY);
        add(lblSub, gbc);
    }

    private ImageIcon loadIconSafely(String path, int w, int h) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return null;
    }
}
