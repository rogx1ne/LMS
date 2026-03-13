package com.library.ui;

import javax.swing.*;
import java.awt.*;

public final class LetterIcon implements Icon {
    private final String text;
    private final int size;
    private final Color bg;
    private final Color fg;

    public LetterIcon(String text, int size, Color bg, Color fg) {
        this.text = text == null ? "" : text.trim();
        this.size = Math.max(12, size);
        this.bg = bg == null ? new Color(230, 230, 230) : bg;
        this.fg = fg == null ? Color.BLACK : fg;
    }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = size;
            g2.setColor(bg);
            g2.fillRoundRect(x, y, size, size, arc, arc);

            String s = text.isEmpty() ? "?" : text.substring(0, 1).toUpperCase();
            float fontSize = Math.max(10f, size * 0.55f);
            Font font = (c == null ? new Font("Segoe UI", Font.BOLD, (int) fontSize) : c.getFont().deriveFont(Font.BOLD, fontSize));
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            int textW = fm.stringWidth(s);
            int textH = fm.getAscent();
            int tx = x + (size - textW) / 2;
            int ty = y + (size + textH) / 2 - 2;

            g2.setColor(fg);
            g2.drawString(s, tx, ty);
        } finally {
            g2.dispose();
        }
    }
}

