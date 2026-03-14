package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Constructor;

/**
 * Loads the assistant panel lazily (via reflection) so the core LMS UI stays usable
 * even if the assistant code throws at runtime.
 */
public class AssistantLoaderPanel extends JPanel {
    private static final String ASSISTANT_CLASS = "com.library.assistant.ui.AssistantPanel";

    private final DashboardFrame dashboard;
    private final JPanel content = new JPanel(new BorderLayout());
    private boolean loaded = false;

    public AssistantLoaderPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;

        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);

        content.setBackground(ModuleTheme.WHITE);
        content.add(buildPlaceholder(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModuleTheme.WHITE);
        header.setBorder(ModuleTheme.sectionBorder("Assistant"));

        JLabel title = new JLabel("Rule-based Assistant (safe mode)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ModuleTheme.BLUE_DARK);
        title.setBorder(new EmptyBorder(6, 10, 6, 10));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JComponent buildPlaceholder() {
        JPanel panel = new JPanel();
        panel.setBackground(ModuleTheme.WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("<html><b>What it can do now</b><br/>" +
            "• Navigate you to modules/sections<br/>" +
            "• Generate a few reports (Excel export)<br/>" +
            "• Help troubleshoot common errors</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btn = new JButton("Open Assistant");
        ModuleTheme.stylePrimaryButton(btn);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> loadAssistant());

        panel.add(desc);
        panel.add(Box.createVerticalStrut(14));
        panel.add(btn);
        panel.add(Box.createVerticalStrut(10));

        JLabel hint = new JLabel("Rules live in ./assistant/ so you can edit without rebuilding.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(120, 120, 120));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(hint);

        return panel;
    }

    private void loadAssistant() {
        if (loaded) return;
        loaded = true;

        try {
            Class<?> clazz = Class.forName(ASSISTANT_CLASS);
            Constructor<?> ctor = clazz.getConstructor(DashboardFrame.class);
            Object instance = ctor.newInstance(dashboard);
            if (!(instance instanceof JComponent)) {
                throw new IllegalStateException(ASSISTANT_CLASS + " is not a Swing component.");
            }

            content.removeAll();
            content.add((JComponent) instance, BorderLayout.CENTER);
            content.revalidate();
            content.repaint();
        } catch (Throwable t) {
            loaded = false;
            showLoadError(t);
        }
    }

    private void showLoadError(Throwable t) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        ModuleTheme.styleTextArea(area);
        area.setText(
            "Assistant failed to start.\n\n" +
            "This does not affect the rest of the LMS.\n\n" +
            t.getClass().getName() + ": " + (t.getMessage() == null ? "" : t.getMessage())
        );

        JButton retry = new JButton("Retry");
        ModuleTheme.styleSubtleButton(retry);
        retry.addActionListener(e -> loadAssistant());

        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setBackground(ModuleTheme.WHITE);
        wrap.setBorder(new EmptyBorder(18, 18, 18, 18));
        wrap.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(ModuleTheme.WHITE);
        footer.add(retry);
        wrap.add(footer, BorderLayout.SOUTH);

        content.removeAll();
        content.add(wrap, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}

