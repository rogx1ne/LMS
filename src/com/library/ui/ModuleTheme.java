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
    public static final Color GREEN_DARK = new Color(27, 94, 32);
    public static final Color WHITE = Color.WHITE;
    public static final Color BLACK = Color.BLACK;
    public static final Color HEADER_TEXT = new Color(80, 80, 80);
    public static final Color TABLE_BG = new Color(250, 252, 255);
    public static final Color TABLE_HEAD = new Color(230, 235, 245);

    // Status Colors (Subtle backgrounds)
    public static final Color STATUS_GREEN_BG = new Color(220, 245, 220);
    public static final Color STATUS_GREEN_FG = new Color(20, 100, 20);
    public static final Color STATUS_ORANGE_BG = new Color(255, 245, 210);
    public static final Color STATUS_ORANGE_FG = new Color(150, 90, 0);
    public static final Color STATUS_RED_BG = new Color(255, 230, 230);
    public static final Color STATUS_RED_FG = new Color(180, 20, 20);
    public static final Color STATUS_GRAY_BG = new Color(240, 240, 240);
    public static final Color STATUS_GRAY_FG = new Color(100, 100, 100);

    private static float currentScale = 1.0f;

    private ModuleTheme() {}

    public static void setScale(float scale) {
        currentScale = scale;
    }

    public static Font getFont(int style, int size) {
        return new Font("Segoe UI", style, (int)(size * currentScale));
    }

    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(getFont(Font.BOLD, 14));
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
        field.setFont(getFont(Font.PLAIN, 12));
        field.setPreferredSize(new Dimension((int)(180 * currentScale), (int)(30 * currentScale)));
        field.setBorder(compound());
    }

    public static void styleCombo(JComboBox<?> box) {
        box.setOpaque(true);
        box.setBackground(WHITE);
        box.setForeground(BLACK);
        box.setFont(getFont(Font.PLAIN, 12));
        box.setBorder(new LineBorder(Color.LIGHT_GRAY));
        box.setPreferredSize(new Dimension((int)(180 * currentScale), (int)(30 * currentScale)));
    }

    public static void styleTextArea(JTextArea area) {
        area.setOpaque(true);
        area.setBackground(WHITE);
        area.setForeground(BLACK);
        area.setCaretColor(BLACK);
        area.setFont(getFont(Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(compound());
    }

    public static void stylePrimaryButton(JButton btn) {
        styleButton(btn, BLUE_DARK, WHITE, GREEN, WHITE);
        btn.setFont(getFont(Font.BOLD, 12));
    }

    /**
     * Creates a modern rounded button with a subtle shadow-like border.
     */
    public static JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setForeground(fg);
        btn.setFont(getFont(Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Styles a panel to look like a modern "Card" with a subtle border and background.
     */
    public static void styleCard(JPanel panel) {
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
    }

    public static void stylePrimaryButtonOnBlue(JButton btn) {
        styleButton(btn, GREEN, WHITE, GREEN_DARK, WHITE);
        btn.setFont(getFont(Font.BOLD, 12));
    }

    public static void styleAccentButton(JButton btn) {
        styleButton(btn, GREEN, WHITE, BLUE_DARK, WHITE);
        btn.setFont(getFont(Font.BOLD, 12));
    }

    public static void styleSubtleButton(JButton btn) {
        styleButton(btn, WHITE, BLACK, BLUE_DARK, WHITE);
        btn.setFont(getFont(Font.BOLD, 12));
        btn.setBorder(new LineBorder(Color.LIGHT_GRAY));
    }

    public static void styleButton(JButton btn, Color normalBg, Color normalFg, Color hoverBg, Color hoverFg) {
        btn.setBackground(normalBg);
        btn.setForeground(normalFg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));

        if (normalBg != null && !WHITE.equals(normalBg)) {
            btn.setForeground(WHITE);
        }

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
        table.setRowHeight((int)(28 * currentScale));
        table.setBackground(TABLE_BG);
        table.setForeground(BLACK);
        table.setSelectionBackground(new Color(210, 230, 255));
        table.setSelectionForeground(BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(getFont(Font.PLAIN, 12));

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEAD);
        header.setForeground(BLUE_DARK);
        header.setFont(getFont(Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, (int)(32 * currentScale)));
    }

    public static void applyStatusRenderer(JTable table, int... columnIndices) {
        StatusCellRenderer renderer = new StatusCellRenderer();
        for (int index : columnIndices) {
            table.getColumnModel().getColumn(index).setCellRenderer(renderer);
        }
    }

    public static Border sectionBorder(String title) {
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            title
        );
        border.setTitleFont(getFont(Font.BOLD, 12));
        border.setTitleColor(BLUE_DARK);
        return border;
    }

    public static void setErrorBorder(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(STATUS_RED_FG, 1),
            new EmptyBorder(4, 6, 4, 6)
        ));
    }

    public static void setNormalBorder(JComponent c) {
        c.setBorder(compound());
    }

    public static void addValidation(JTextField field, java.util.function.Predicate<String> validator) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void check() {
                if (validator.test(field.getText())) {
                    setNormalBorder(field);
                } else {
                    setErrorBorder(field);
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
        });
    }

    public static <T extends JComponent> JLayer<T> createEmptyStateLayer(T component, String message) {
        return new JLayer<>(component, new javax.swing.plaf.LayerUI<T>() {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                boolean isEmpty = false;
                if (component instanceof JTable) {
                    isEmpty = ((JTable) component).getRowCount() == 0;
                }
                if (isEmpty) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(150, 150, 150));
                    g2.setFont(getFont(Font.ITALIC, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (c.getWidth() - fm.stringWidth(message)) / 2;
                    int y = (c.getHeight() + fm.getAscent()) / 2;
                    g2.drawString(message, x, y);
                    g2.dispose();
                }
            }
        });
    }

    private static Border compound() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 210, 210)),
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

    /**
     * Modern Toast notification.
     */
    public static void showToast(Component parent, String message) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        if (owner == null) return;

        JWindow toast = new JWindow(owner);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 50, 50, 230));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel label = new JLabel(message);
        label.setForeground(WHITE);
        label.setFont(getFont(Font.BOLD, 13));
        panel.add(label);

        toast.add(panel);
        toast.pack();

        // Position bottom center of owner
        Point p = owner.getLocationOnScreen();
        int x = p.x + (owner.getWidth() - toast.getWidth()) / 2;
        int y = p.y + owner.getHeight() - toast.getHeight() - 60;
        toast.setLocation(x, y);

        toast.setVisible(true);

        new Timer(2500, e -> {
            toast.dispose();
            ((Timer)e.getSource()).stop();
        }).start();
    }

    /**
     * Specialized renderer for Status columns.
     */
    private static class StatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value == null || isSelected) return c;

            String status = value.toString().toUpperCase();
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(ModuleTheme.getFont(Font.BOLD, 11));

            if (status.contains("AVAILABLE") || status.contains("ACTIVE") || status.contains("RETURNED") || status.contains("PAID")) {
                c.setBackground(STATUS_GREEN_BG);
                c.setForeground(STATUS_GREEN_FG);
            } else if (status.contains("ISSUED") || status.contains("PENDING") || status.contains("ORDERED")) {
                c.setBackground(STATUS_ORANGE_BG);
                c.setForeground(STATUS_ORANGE_FG);
            } else if (status.contains("WITHDRAWN") || status.contains("LOST") || status.contains("OVERDUE") || status.contains("DEACTIVATED")) {
                c.setBackground(STATUS_RED_BG);
                c.setForeground(STATUS_RED_FG);
            } else {
                c.setBackground(STATUS_GRAY_BG);
                c.setForeground(STATUS_GRAY_FG);
            }
            return c;
        }
    }
}
