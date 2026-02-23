package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.time.Year;

public class StudentView extends JPanel {

    // --- LAYOUT COMPONENTS ---
    public CardLayout cardLayout;
    public JPanel contentPanel;

    // --- HEADER NAVIGATION ---
    private JButton navRegister;
    private JButton navView;
    private JLabel lblFormTitle;

    // --- INPUT FIELDS ---
    private JTextField txtName, txtRoll, txtPhone;
    private JTextArea txtAddr;
    private JComboBox<String> cmbCourse, cmbSession, cmbBookLimit, cmbStatus;
    private JTextField txtIssuedBy, txtIssueDate, txtFee;

    // --- REGISTER BUTTONS ---
    private JButton btnSaveForm;

    // --- VIEW / TABLE COMPONENTS ---
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTotalCount;

    // --- VIEW BUTTONS ---
    private JButton btnPdf, btnEdit, btnResetFilters;

    // --- FILTER FIELDS ---
    private JTextField fltCardId, fltName, fltRoll, fltIssueBy;
    private JComboBox<String> fltSession, fltCourse, fltBookLimit;
    private JSpinner fltFromDate, fltToDate;

    // --- COLORS (Updated) ---
    private static final Color COLOR_BLUE_DARK = new Color(31, 62, 109);
    private static final Color COLOR_WHITE     = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(80, 80, 80);

    // NEW COLORS FOR THEME
    private static final Color COLOR_LIGHT_GREEN_BG = new Color(235, 250, 235); // Very light mint green
    private static final Color COLOR_TABLE_HEADER   = new Color(200, 230, 200); // Slightly darker green for header

    public StudentView() {
        setLayout(new BorderLayout());
        setBackground(COLOR_WHITE);

        // 1. TOP HEADER (Navigation)
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. MAIN CONTENT (Card Layout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_WHITE);

        contentPanel.add(createRegisterPanel(), "FORM");
        contentPanel.add(createViewPanel(), "LIST");

        add(contentPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        header.setBackground(COLOR_WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        navRegister = createNavButton("NEW REGISTRATION");
        navView = createNavButton("STUDENT INFO");

        header.add(navRegister);
        header.add(navView);

        return header;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(COLOR_HEADER_TEXT);
        btn.setBackground(COLOR_WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setForeground(COLOR_BLUE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setForeground(COLOR_HEADER_TEXT); }
        });
        return btn;
    }

    // ==========================================
    // 2. REGISTER PANEL
    // ==========================================
    private JPanel createRegisterPanel() {
        JPanel pnlRegister = new JPanel(new BorderLayout());
        pnlRegister.setBackground(COLOR_WHITE);
        pnlRegister.setBorder(new EmptyBorder(20, 60, 20, 60));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(COLOR_WHITE);
        lblFormTitle = new JLabel("ENTER STUDENT DETAILS");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(COLOR_BLUE_DARK);
        titlePanel.add(lblFormTitle);

        JPanel form = new JPanel(new GridLayout(6, 2, 40, 20));
        form.setBackground(COLOR_WHITE);
        form.setBorder(new EmptyBorder(20, 0, 20, 0));

        txtName = createSharpField();
        txtRoll = createSharpField();
        ((AbstractDocument) txtRoll.getDocument()).setDocumentFilter(new NumericLimitFilter(5));
        txtPhone = createSharpField();
        ((AbstractDocument) txtPhone.getDocument()).setDocumentFilter(new NumericLimitFilter(10));

        txtAddr = new JTextArea(3, 20);
        txtAddr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtAddr.setLineWrap(true);
        txtAddr.setBackground(Color.WHITE);
        txtAddr.setForeground(Color.BLACK);

        cmbCourse = new JComboBox<>(new String[]{"BCA", "BBA", "MCA"});
        cmbSession = new JComboBox<>(generateDefaultSessions());
        cmbBookLimit = new JComboBox<>(new String[]{"2", "1"});
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        txtIssuedBy = createSharpField();
        txtIssuedBy.setEditable(false);
        txtIssueDate = createSharpField();
        txtIssueDate.setEditable(false);
        txtFee = createSharpField();
        txtFee.setEditable(false);

        styleComboBox(cmbCourse); styleComboBox(cmbSession);
        styleComboBox(cmbBookLimit); styleComboBox(cmbStatus);

        form.add(createInputGroup("Full Name", txtName));
        form.add(createInputGroup("Course", cmbCourse));
        form.add(createInputGroup("Academic Session", cmbSession));
        form.add(createInputGroup("Roll Number", txtRoll));
        form.add(createInputGroup("Phone Number", txtPhone));
        form.add(createInputGroup("Book Limit", cmbBookLimit));
        form.add(createInputGroup("Fee (Auto)", txtFee));
        form.add(createInputGroup("Issued By (Auto)", txtIssuedBy));
        form.add(createInputGroup("Issue Date (Auto)", txtIssueDate));
        form.add(createInputGroup("Status", cmbStatus));
        form.add(createInputGroup("Address", new JScrollPane(txtAddr)));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(COLOR_WHITE);
        btnSaveForm = new JButton("PREVIEW & SAVE");
        stylePrimaryButton(btnSaveForm);
        footer.add(btnSaveForm);

        pnlRegister.add(titlePanel, BorderLayout.NORTH);
        pnlRegister.add(form, BorderLayout.CENTER);
        pnlRegister.add(footer, BorderLayout.SOUTH);
        return pnlRegister;
    }

    // ==========================================
    // 3. VIEW PANEL (LIST) - UPDATED COLORS
    // ==========================================
    private JPanel createViewPanel() {
        JPanel pnlView = new JPanel(new BorderLayout());
        pnlView.setBackground(COLOR_WHITE);
        pnlView.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- FILTERS (WHITE BACKGROUND) ---
        JPanel pnlFilters = new JPanel(new GridBagLayout());
        pnlFilters.setBackground(COLOR_WHITE); // Explicitly White
        pnlFilters.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Search & Filters", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 11), Color.BLACK));

        fltCardId = createFilterField(110);
        fltName = createFilterField(120);

        fltRoll = createFilterField(80);
        fltIssueBy = createFilterField(80);
        fltCourse = new JComboBox<>(new String[]{"All", "BCA", "MCA", "BBA"});
        fltSession = new JComboBox<>(generateSessionFiltersDefault());
        fltBookLimit = new JComboBox<>(new String[]{"All", "1", "2"});
        styleFilterCombo(fltCourse); styleFilterCombo(fltSession); styleFilterCombo(fltBookLimit);

        fltFromDate = createDateSpinner();
        fltToDate = createDateSpinner();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -10);
        cal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        fltFromDate.setValue(cal.getTime());
        fltToDate.setValue(new java.util.Date());

        btnResetFilters = new JButton("Reset");
        btnResetFilters.setBackground(Color.WHITE);
        btnResetFilters.setForeground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 15); gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridy = 0;
        addFilterLabel(pnlFilters, "Card ID:", 0, 0, gbc);   gbc.gridx = 1; pnlFilters.add(fltCardId, gbc);
        addFilterLabel(pnlFilters, "Name:", 2, 0, gbc);      gbc.gridx = 3; pnlFilters.add(fltName, gbc);
        addFilterLabel(pnlFilters, "Roll:", 4, 0, gbc);      gbc.gridx = 5; pnlFilters.add(fltRoll, gbc);
        addFilterLabel(pnlFilters, "Course:", 6, 0, gbc);    gbc.gridx = 7; pnlFilters.add(fltCourse, gbc);

        // Row 2
        gbc.gridy = 1;
        addFilterLabel(pnlFilters, "From:", 0, 1, gbc);      gbc.gridx = 1; pnlFilters.add(fltFromDate, gbc);
        addFilterLabel(pnlFilters, "To:", 2, 1, gbc);        gbc.gridx = 3; pnlFilters.add(fltToDate, gbc);
        addFilterLabel(pnlFilters, "Session:", 4, 1, gbc);   gbc.gridx = 5; pnlFilters.add(fltSession, gbc);
        addFilterLabel(pnlFilters, "Issued By:", 6, 1, gbc); gbc.gridx = 7; pnlFilters.add(fltIssueBy, gbc);

        gbc.gridy = 2;
        addFilterLabel(pnlFilters, "Book Limit:", 0, 2, gbc); gbc.gridx = 1; pnlFilters.add(fltBookLimit, gbc);
        gbc.gridx = 7; pnlFilters.add(btnResetFilters, gbc);

        // --- TABLE (LIGHT GREEN BACKGROUND + BLACK TEXT) ---
        String[] cols = {"S.No", "Card ID", "Name", "Course", "Roll No", "Session", "Issue Date", "Issued By", "Book Limit", "Fee", "Status"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        table = new JTable(tableModel);
        table.setRowHeight(28);

        // Table Body Styling
        table.setBackground(COLOR_LIGHT_GREEN_BG); // Light Green Body
        table.setForeground(Color.BLACK);          // Black Text
        table.setSelectionBackground(new Color(180, 220, 180)); // Slightly darker green selection
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        // Table Header Styling
        table.getTableHeader().setBackground(COLOR_TABLE_HEADER); // Greenish Header
        table.getTableHeader().setForeground(Color.BLACK);        // Black Header Text
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // ScrollPane Viewport (Fills empty space with green)
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(COLOR_LIGHT_GREEN_BG);

        // Footer Actions
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(COLOR_WHITE);
        lblTotalCount = new JLabel("Total Records: 0");
        lblTotalCount.setForeground(Color.BLACK);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setBackground(COLOR_WHITE);

        btnEdit = new JButton("Edit / Update"); styleFooterButton(btnEdit); btnEdit.setBackground(new Color(255, 140, 0));
        btnPdf = new JButton("Download PDF"); styleFooterButton(btnPdf);

        pnlActions.add(btnEdit); pnlActions.add(btnPdf);
        pnlFooter.add(lblTotalCount, BorderLayout.WEST); pnlFooter.add(pnlActions, BorderLayout.EAST);

        pnlView.add(pnlFilters, BorderLayout.NORTH);
        pnlView.add(scroll, BorderLayout.CENTER);
        pnlView.add(pnlFooter, BorderLayout.SOUTH);
        return pnlView;
    }

    // Helper to add black labels
    private void addFilterLabel(JPanel p, String text, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y;
        JLabel l = new JLabel(text);
        l.setForeground(Color.BLACK);
        p.add(l, gbc);
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public TableRowSorter<DefaultTableModel> getSorter() { return sorter; }

    public JButton getNavRegister() { return navRegister; }
    public JButton getNavView() { return navView; }

    public JButton getBtnSaveForm() { return btnSaveForm; }
    public JButton getBtnPdf() { return btnPdf; }
    public JButton getBtnEdit() { return btnEdit; }
    public JButton getBtnResetFilters() { return btnResetFilters; }

    public String getInputName() { return txtName.getText().trim(); }
    public String getInputRoll() { return txtRoll.getText().trim(); }
    public String getInputPhone() { return txtPhone.getText().trim(); }
    public String getInputAddr() { return txtAddr.getText().trim(); }
    public String getInputCourse() { return (String) cmbCourse.getSelectedItem(); }
    public String getInputSession() { return (String) cmbSession.getSelectedItem(); }
    public int getInputBookLimit() { return Integer.parseInt((String) cmbBookLimit.getSelectedItem()); }
    public String getInputStatus() { return (String) cmbStatus.getSelectedItem(); }
    public String getIssuedByText() { return txtIssuedBy.getText(); }
    public String getIssueDateText() { return txtIssueDate.getText(); }
    public String getFeeText() { return txtFee.getText(); }

    public void setInputName(String s) { txtName.setText(s); }
    public void setInputRoll(String s) { txtRoll.setText(s); }
    public void setInputPhone(String s) { txtPhone.setText(s); }
    public void setInputAddr(String s) { txtAddr.setText(s); }
    public void setInputCourse(String s) { cmbCourse.setSelectedItem(s); }
    public void setInputSession(String s) { cmbSession.setSelectedItem(s); }
    public void setInputBookLimit(String s) { cmbBookLimit.setSelectedItem(s); }
    public void setInputStatus(String s) { cmbStatus.setSelectedItem(s); }
    public void setIssuedByText(String s) { txtIssuedBy.setText(s); }
    public void setIssueDateText(String s) { txtIssueDate.setText(s); }
    public void setFeeText(String s) { txtFee.setText(s); }

    public void setSessionOptions(String[] sessions) {
        cmbSession.removeAllItems();
        for (String s : sessions) cmbSession.addItem(s);
        if (cmbSession.getItemCount() > 0) cmbSession.setSelectedIndex(0);
    }

    public void showCard(String name) { cardLayout.show(contentPanel, name); }
    public void setFormTitle(String title) { lblFormTitle.setText(title); }
    public void setSaveButtonText(String text) { btnSaveForm.setText(text); }
    public void setStatusEnabled(boolean enabled) { cmbStatus.setEnabled(enabled); }
    public void updateTotalCount(int count) { lblTotalCount.setText("Total Records: " + count); }

    public void clearForm() {
        txtName.setText(""); txtRoll.setText(""); txtPhone.setText(""); txtAddr.setText("");
        if(cmbSession.getItemCount()>0) cmbSession.setSelectedIndex(0);
        cmbBookLimit.setSelectedItem("2");
        cmbStatus.setSelectedItem("ACTIVE");
        if (txtIssuedBy != null) txtIssuedBy.setText("");
        if (txtIssueDate != null) txtIssueDate.setText("");
        if (txtFee != null) txtFee.setText("");
    }

    public JTextField getFltCardId() { return fltCardId; }
    public JTextField getFltName() { return fltName; }
    public JTextField getFltRoll() { return fltRoll; }
    public JTextField getFltIssueBy() { return fltIssueBy; }
    public JComboBox<String> getFltCourse() { return fltCourse; }
    public JComboBox<String> getFltSession() { return fltSession; }
    public JComboBox<String> getFltBookLimit() { return fltBookLimit; }
    public JSpinner getFltFromDate() { return fltFromDate; }
    public JSpinner getFltToDate() { return fltToDate; }

    public JTextField getInputNameField() {
        return txtName;
    }

    public JTextField getInputRollField() {
        return txtRoll;
    }

    public JTextField getInputPhoneField() {
        return txtPhone;
    }

    public JTextArea getInputAddressField() {
        return txtAddr;
    }

    public JComboBox<String> getInputCourseCombo() {
        return cmbCourse;
    }

    public JComboBox<String> getInputSessionCombo() {
        return cmbSession;
    }

    public JComboBox<String> getInputBookLimitCombo() {
        return cmbBookLimit;
    }
    public JComboBox<String> getInputStatusCombo() { return cmbStatus; }

    public JButton getSaveButton() {
        return btnSaveForm;
    }

    // ==========================================
    // UI FACTORIES & STYLING (The Fix)
    // ==========================================
    private JTextField createSharpField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 30));

        // --- FORCE WHITE BOX FIX ---
        field.setOpaque(true);                // 1. Force non-transparent
        field.setBackground(Color.WHITE);     // 2. Background White
        field.setForeground(Color.BLACK);     // 3. Text Black
        field.setCaretColor(Color.BLACK);     // 4. Blinking Cursor Black
        // ---------------------------

        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private JTextField createFilterField(int w) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(w, 25));

        // --- FORCE WHITE BOX FIX ---
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        // ---------------------------

        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        return field;
    }

    private void styleComboBox(JComboBox<?> box) {
        // --- FORCE WHITE BOX FIX ---
        box.setOpaque(true);
        box.setBackground(Color.WHITE);
        box.setForeground(Color.BLACK);
        // ---------------------------

        box.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        box.setPreferredSize(new Dimension(200, 30));

        // Special Fix for Linux Dropdowns
        if (box.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField editor = (JTextField) box.getEditor().getEditorComponent();
            editor.setOpaque(true);
            editor.setBackground(Color.WHITE);
            editor.setForeground(Color.BLACK);
            editor.setCaretColor(Color.BLACK);
        }
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(COLOR_BLUE_DARK);
        btn.setForeground(COLOR_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(180, 40));
    }

    private void styleFooterButton(JButton btn) {
        btn.setBackground(COLOR_BLUE_DARK);
        btn.setForeground(COLOR_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }


    private void styleFilterCombo(JComboBox<?> box) {

        box.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        box.setBackground(COLOR_WHITE);
        box.setForeground(Color.BLACK);
        box.setPreferredSize(new Dimension(90, 25));
    }

    private JPanel createInputGroup(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(COLOR_BLUE_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private String[] generateDefaultSessions() {
        int y = Year.now().getValue();
        return new String[]{ (y)+"-"+(y+3), (y-1)+"-"+(y+2), (y-2)+"-"+(y+1) };
    }

    private String[] generateSessionFiltersDefault() {
        int y = Year.now().getValue();
        // Merge UG (3-year) and PG (2-year) sessions so filtering works for all courses.
        java.util.LinkedHashSet<String> sessions = new java.util.LinkedHashSet<>();
        sessions.add((y) + "-" + (y + 3));
        sessions.add((y - 1) + "-" + (y + 2));
        sessions.add((y - 2) + "-" + (y + 1));
        sessions.add((y) + "-" + (y + 2));
        sessions.add((y - 1) + "-" + (y + 1));
        sessions.add((y - 2) + "-" + (y));

        String[] f = new String[sessions.size() + 1];
        f[0] = "All";
        int i = 1;
        for (String s : sessions) f[i++] = s;
        return f;
    }

    private JSpinner createDateSpinner() {
        java.util.Date now = new java.util.Date();
        SpinnerDateModel model = new SpinnerDateModel(now, null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "yyyy-MM-dd");
        sp.setEditor(editor);
        sp.setPreferredSize(new Dimension(110, 25));
        return sp;
    }

    private class NumericLimitFilter extends DocumentFilter {
        private int limit;
        public NumericLimitFilter(int limit) { this.limit = limit; }
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
            if ((fb.getDocument().getLength() + str.length()) <= limit && str.matches("[0-9]+")) super.insertString(fb, offset, str, attr);
        }
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            if ((fb.getDocument().getLength() - length + str.length()) <= limit && str.matches("[0-9]+")) super.replace(fb, offset, length, str, attrs);
        }
    }
}
