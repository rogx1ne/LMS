package com.library.assistant.ui;

import com.library.assistant.AssistantAction;
import com.library.assistant.AssistantActionType;
import com.library.assistant.AssistantEngine;
import com.library.assistant.AssistantReply;
import com.library.assistant.reports.AssistantReportService;
import com.library.ui.DashboardFrame;
import com.library.ui.ModuleTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssistantPanel extends JPanel {
    private static final String CLIENT_PROP_DISABLE_ENTER_TRAVERSAL = "lms.disableEnterTraversal";

    private final DashboardFrame dashboard;
    private final AssistantEngine engine = new AssistantEngine();
    private final AssistantReportService reports = new AssistantReportService();

    private final JTextArea conversation = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final JTable table = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JButton btnExportExcel = new JButton("Export to Excel");
    private List<Map<String, Object>> lastRows = new ArrayList<>();
    private String lastSheetName = "Report";

    public AssistantPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;

        setLayout(new BorderLayout(10, 10));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildQuickActions(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildComposer(), BorderLayout.SOUTH);

        appendAssistant("Hi — I’m a rule-based assistant. Try: low stock, overdue, issue book, return book, ORA-12514, OTP email.");
    }

    private JComponent buildQuickActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(ModuleTheme.WHITE);
        bar.setBorder(ModuleTheme.sectionBorder("Quick Actions"));

        JButton lowStock = new JButton("Low Stock Report");
        ModuleTheme.styleSubtleButton(lowStock);
        lowStock.addActionListener(e -> handleText("low stock"));

        JButton overdue = new JButton("Overdue Issues");
        ModuleTheme.styleSubtleButton(overdue);
        overdue.addActionListener(e -> handleText("overdue"));

        JButton dbHelp = new JButton("DB Connection Help");
        ModuleTheme.styleSubtleButton(dbHelp);
        dbHelp.addActionListener(e -> handleText("ORA-12514"));

        JButton otpHelp = new JButton("OTP / SMTP Help");
        ModuleTheme.styleSubtleButton(otpHelp);
        otpHelp.addActionListener(e -> handleText("otp email"));

        bar.add(lowStock);
        bar.add(overdue);
        bar.add(dbHelp);
        bar.add(otpHelp);
        return bar;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setBackground(ModuleTheme.WHITE);

        // Conversation
        conversation.setEditable(false);
        ModuleTheme.styleTextArea(conversation);
        JScrollPane convoScroll = new JScrollPane(conversation);
        convoScroll.setBorder(ModuleTheme.sectionBorder("Conversation"));

        // Results
        table.setModel(tableModel);
        ModuleTheme.styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(ModuleTheme.sectionBorder("Results"));

        JPanel resultsWrap = new JPanel(new BorderLayout(0, 8));
        resultsWrap.setBackground(ModuleTheme.WHITE);
        resultsWrap.add(tableScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnExportExcel);
        btnExportExcel.setEnabled(false);
        btnExportExcel.addActionListener(e -> exportLastRowsToExcel());
        actions.add(btnExportExcel);
        resultsWrap.add(actions, BorderLayout.SOUTH);

        center.add(convoScroll);
        center.add(resultsWrap);
        return center;
    }

    private JComponent buildComposer() {
        JPanel composer = new JPanel(new BorderLayout(10, 0));
        composer.setBackground(ModuleTheme.WHITE);
        composer.setBorder(ModuleTheme.sectionBorder("Ask"));

        input.putClientProperty(CLIENT_PROP_DISABLE_ENTER_TRAVERSAL, Boolean.TRUE);
        ModuleTheme.styleInput(input);
        input.setToolTipText("Ask: low stock, overdue, issue book, return book, seller master, ORA-17800, OTP...");
        input.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    sendFromInput();
                }
            }
        });

        ModuleTheme.stylePrimaryButton(btnSend);
        btnSend.addActionListener(e -> sendFromInput());

        composer.add(input, BorderLayout.CENTER);
        composer.add(btnSend, BorderLayout.EAST);
        return composer;
    }

    private void sendFromInput() {
        String text = input.getText() == null ? "" : input.getText().trim();
        if (text.isEmpty()) return;
        input.setText("");
        handleText(text);
    }

    private void handleText(String text) {
        appendUser(text);
        AssistantReply reply = engine.handle(text);
        appendAssistant(reply.getMessage());
        perform(reply.getAction(), text);
    }

    private void perform(AssistantAction action, String userText) {
        if (action == null) return;
        AssistantActionType type = action.getType();
        if (type == null || type == AssistantActionType.NONE) return;

        try {
            switch (type) {
                case NAVIGATE:
                    navigate(action.getArg1(), action.getArg2());
                    break;
                case HELP:
                    showHelp(action.getArg1());
                    break;
                case REPORT_LOW_STOCK:
                    int threshold = parseThreshold(action.getArg1(), userText);
                    runLowStockReport(threshold);
                    break;
                case REPORT_OVERDUE:
                    runOverdueReport();
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            appendAssistant("Action failed: " + t.getClass().getSimpleName() + (t.getMessage() == null ? "" : (": " + t.getMessage())));
        }
    }

    private void navigate(String moduleKey, String sectionKey) {
        if (dashboard == null) return;
        if (moduleKey == null || moduleKey.trim().isEmpty()) return;
        if (sectionKey == null || sectionKey.trim().isEmpty()) dashboard.navigateTo(moduleKey);
        else dashboard.navigateTo(moduleKey, sectionKey);
    }

    private void showHelp(String topicKey) {
        String text = engine.loadHelp(topicKey);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        ModuleTheme.styleTextArea(area);
        JScrollPane sc = new JScrollPane(area);
        sc.setPreferredSize(new Dimension(720, 420));
        JOptionPane.showMessageDialog(this, sc, "Help: " + topicKey, JOptionPane.INFORMATION_MESSAGE);
    }

    private int parseThreshold(String actionArg, String userText) {
        Integer fromArg = tryParseInt(actionArg);
        if (fromArg != null && fromArg > 0) return fromArg;

        // If the rule is regex-based, threshold might be in the user text.
        Integer fromText = extractFirstInt(userText);
        if (fromText != null && fromText > 0) return fromText;

        String raw = JOptionPane.showInputDialog(this, "Low stock threshold (e.g. 2):", "2");
        Integer fromDialog = tryParseInt(raw);
        return (fromDialog != null && fromDialog > 0) ? fromDialog : 2;
    }

    private void runLowStockReport(int threshold) {
        appendAssistant("Generating low stock report (threshold " + threshold + ")...");
        btnExportExcel.setEnabled(false);
        lastRows = new ArrayList<>();
        lastSheetName = "LowStock";

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override protected List<Map<String, Object>> doInBackground() throws Exception {
                return reports.lowStock(threshold);
            }

            @Override protected void done() {
                try {
                    List<Map<String, Object>> rows = get();
                    lastRows = rows;
                    setTableRows(rows);
                    appendAssistant(rows.isEmpty() ? "No low-stock titles found." : ("Found " + rows.size() + " low-stock title(s)."));
                    btnExportExcel.setEnabled(!rows.isEmpty());
                } catch (Exception e) {
                    appendAssistant(formatReportError(e));
                }
            }
        }.execute();
    }

    private void runOverdueReport() {
        appendAssistant("Generating overdue issues report...");
        btnExportExcel.setEnabled(false);
        lastRows = new ArrayList<>();
        lastSheetName = "Overdue";

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override protected List<Map<String, Object>> doInBackground() throws Exception {
                return reports.overdueIssues();
            }

            @Override protected void done() {
                try {
                    List<Map<String, Object>> rows = get();
                    lastRows = rows;
                    setTableRows(rows);
                    appendAssistant(rows.isEmpty() ? "No overdue issues found." : ("Found " + rows.size() + " overdue issue(s)."));
                    btnExportExcel.setEnabled(!rows.isEmpty());
                } catch (Exception e) {
                    appendAssistant(formatReportError(e));
                }
            }
        }.execute();
    }

    private String formatReportError(Exception e) {
        Throwable root = e;
        while (root.getCause() != null) root = root.getCause();
        if (root instanceof SQLException) {
            return "Report failed (DB). If this is a schema/setup issue, run `script.sql` and verify DBConnection settings.";
        }
        return "Report failed: " + root.getClass().getSimpleName() + (root.getMessage() == null ? "" : (": " + root.getMessage()));
    }

    private void exportLastRowsToExcel() {
        if (lastRows == null || lastRows.isEmpty()) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Excel report");
        fc.setSelectedFile(new File(lastSheetName + ".xlsx"));
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        Path out = file.toPath();

        appendAssistant("Exporting to Excel: " + file.getName());
        btnExportExcel.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                reports.exportToExcel(lastSheetName, lastRows, out);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    appendAssistant("Exported: " + file.getAbsolutePath());
                } catch (Exception e) {
                    appendAssistant("Export failed: " + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : (": " + e.getMessage())));
                } finally {
                    btnExportExcel.setEnabled(lastRows != null && !lastRows.isEmpty());
                }
            }
        }.execute();
    }

    private void setTableRows(List<Map<String, Object>> rows) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        if (rows == null || rows.isEmpty()) return;

        Map<String, Object> first = rows.get(0);
        List<String> headers = new ArrayList<>(first.keySet());
        for (String h : headers) tableModel.addColumn(h);

        for (Map<String, Object> row : rows) {
            Object[] values = new Object[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                values[i] = row.get(headers.get(i));
            }
            tableModel.addRow(values);
        }
    }

    private void appendUser(String text) {
        conversation.append("You: " + text + "\n");
        conversation.setCaretPosition(conversation.getDocument().getLength());
    }

    private void appendAssistant(String text) {
        conversation.append("Assistant: " + text + "\n\n");
        conversation.setCaretPosition(conversation.getDocument().getLength());
    }

    private Integer tryParseInt(String raw) {
        if (raw == null) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer extractFirstInt(String text) {
        if (text == null) return null;
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() == 0) return null;
        return tryParseInt(digits.toString());
    }
}

