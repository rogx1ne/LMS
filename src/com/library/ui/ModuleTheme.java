package com.library.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class ModuleTheme {
    public static final Color BLUE_DARK = new Color(31, 62, 109);
    public static final Color GREEN = new Color(46, 125, 50);
    public static final Color WHITE = Color.WHITE;
    public static final Color BLACK = Color.BLACK;
    public static final Color HEADER_TEXT = new Color(80, 80, 80);
    public static final Color TABLE_BG = new Color(235, 250, 235);
    public static final Color TABLE_HEAD = new Color(200, 230, 200);

    private ModuleTheme() {}

    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(HEADER_TEXT);
        btn.setBackground(WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(BLUE_DARK); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setForeground(HEADER_TEXT); }
        });
        return btn;
    }

    public static void styleInput(JTextField field) {
        field.setOpaque(true);
        field.setBackground(WHITE);
        field.setForeground(BLACK);
        field.setCaretColor(BLACK);
        field.setPreferredSize(new Dimension(180, 30));
        field.setBorder(compound());
    }

    public static void styleCombo(JComboBox<?> box) {
        box.setOpaque(true);
        box.setBackground(WHITE);
        box.setForeground(BLACK);
        box.setBorder(new LineBorder(Color.LIGHT_GRAY));
        box.setPreferredSize(new Dimension(180, 30));
    }

    public static void styleTextArea(JTextArea area) {
        area.setOpaque(true);
        area.setBackground(WHITE);
        area.setForeground(BLACK);
        area.setCaretColor(BLACK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(compound());
    }

    public static void stylePrimaryButton(JButton btn) {
        styleButton(btn, BLUE_DARK, WHITE, GREEN, WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public static void styleAccentButton(JButton btn) {
        styleButton(btn, new Color(255, 140, 0), WHITE, GREEN, WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public static void styleSubtleButton(JButton btn) {
        styleButton(btn, WHITE, BLACK, BLUE_DARK, WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new LineBorder(Color.LIGHT_GRAY));
    }

    public static void styleButton(JButton btn, Color normalBg, Color normalFg, Color hoverBg, Color hoverFg) {
        btn.setBackground(normalBg);
        btn.setForeground(normalFg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Force remove platform specific styling
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hoverBg);
                    btn.setForeground(hoverFg);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(normalBg);
                    btn.setForeground(normalFg);
                }
            }
        });
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setBackground(TABLE_BG);
        table.setForeground(BLACK);
        table.setSelectionBackground(new Color(180, 220, 180));
        table.setSelectionForeground(BLACK);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEAD);
        header.setForeground(BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public static Border sectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            title
        );
    }

    private static Border compound() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(4, 6, 4, 6)
        );
    }

    public static void addDatePicker(JTextField field) {
        field.setEditable(false);
        field.setCursor(new Cursor(Cursor.HAND_CURSOR));
        field.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Window owner = SwingUtilities.getWindowAncestor(field);
                String selected = DatePickerDialog.showPicker(owner, field.getText());
                if (selected != null) {
                    field.setText(selected);
                }
            }
        });
    }
}
