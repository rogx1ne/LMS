package com.library.ui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerDialog extends JDialog {
    private final Calendar calendar;
    private final JLabel lblMonthYear;
    private final JPanel pnlDays;
    private Date selectedDate;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public DatePickerDialog(Window owner, Date initialDate) {
        super(owner, "Select Date", ModalityType.APPLICATION_MODAL);
        this.calendar = Calendar.getInstance();
        if (initialDate != null) {
            this.calendar.setTime(initialDate);
            this.selectedDate = initialDate;
        } else {
            this.selectedDate = calendar.getTime();
        }

        setSize(300, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(ModuleTheme.BLUE_DARK);
        pnlHeader.setPreferredSize(new Dimension(300, 40));

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setForeground(Color.WHITE);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnPrev = createNavButton("<");
        JButton btnNext = createNavButton(">");

        btnPrev.addActionListener(e -> { calendar.add(Calendar.MONTH, -1); updateCalendar(); });
        btnNext.addActionListener(e -> { calendar.add(Calendar.MONTH, 1); updateCalendar(); });

        pnlHeader.add(btnPrev, BorderLayout.WEST);
        pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
        pnlHeader.add(btnNext, BorderLayout.EAST);

        // Days of week header
        JPanel pnlWeekDays = new JPanel(new GridLayout(1, 7));
        pnlWeekDays.setBackground(new Color(240, 240, 240));
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            pnlWeekDays.add(lbl);
        }

        pnlDays = new JPanel(new GridLayout(0, 7));
        pnlDays.setBackground(Color.WHITE);

        add(pnlHeader, BorderLayout.NORTH);
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlWeekDays, BorderLayout.NORTH);
        pnlCenter.add(pnlDays, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        updateCalendar();
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateCalendar() {
        lblMonthYear.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) + " " + calendar.get(Calendar.YEAR));
        pnlDays.removeAll();

        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Padding for first week
        for (int i = 1; i < firstDayOfWeek; i++) {
            pnlDays.add(new JLabel(""));
        }

        // Days
        Date today = new Date();
        String todayStr = sdf.format(today);
        String selectedStr = selectedDate != null ? sdf.format(selectedDate) : "";

        for (int i = 1; i <= daysInMonth; i++) {
            final int day = i;
            temp.set(Calendar.DAY_OF_MONTH, day);
            String currentStr = sdf.format(temp.getTime());

            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (currentStr.equals(todayStr)) {
                btn.setForeground(ModuleTheme.GREEN);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            if (currentStr.equals(selectedStr)) {
                btn.setBackground(new Color(200, 230, 255));
                btn.setOpaque(true);
            }

            btn.addActionListener(e -> {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                selectedDate = calendar.getTime();
                dispose();
            });

            pnlDays.add(btn);
        }

        pnlDays.revalidate();
        pnlDays.repaint();
    }

    public String getSelectedDate() {
        return selectedDate != null ? sdf.format(selectedDate) : null;
    }

    public static String showPicker(Window owner, String initialDate) {
        Date start = null;
        if (initialDate != null && !initialDate.isEmpty()) {
            try {
                start = new SimpleDateFormat("yyyy-MM-dd").parse(initialDate);
            } catch (Exception ignored) {}
        }
        DatePickerDialog dialog = new DatePickerDialog(owner, start);
        dialog.setVisible(true);
        return dialog.getSelectedDate();
    }
}
