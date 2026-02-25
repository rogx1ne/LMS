package com.library.ui;

import com.library.dao.StudentDAO;
import com.library.model.Student;
import com.library.service.CurrentUserContext;
import com.library.service.StudentLogic;
import com.library.service.StudentPdfService;
import com.library.service.ValidationException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Font;
import com.itextpdf.text.Chunk;

public class StudentController {

    private StudentView view;
    private StudentDAO dao;

    // State
    private boolean isEditMode = false;
    private String editingCardId = null;
    private final StudentPdfService pdfService = new StudentPdfService();

    // Colors for the Card Design
    private static final Color CARD_BLUE_DARK = new Color(31, 62, 109);
    private static final Color CARD_BLUE_LIGHT = new Color(65, 105, 225);
    private static final Color CARD_BG_START = new Color(235, 245, 255);
    private static final Color CARD_BG_END = Color.WHITE;
    private static final Color CARD_TEXT_GRAY = new Color(80, 80, 80);

    public StudentController(StudentView view) {
        this.view = view;
        this.dao = new StudentDAO();
        initController();
        configureTableColumns();

        // Default View: Show List
        refreshTable();
        view.showCard("LIST");
    }

    private void initController() {
        // --- 1. TOP NAVIGATION ---
        view.getNavRegister().addActionListener(e -> prepareRegistration());
        view.getNavView().addActionListener(e -> { refreshTable(); view.showCard("LIST"); });

        // --- 2. REGISTER ACTIONS ---
        view.getBtnSaveForm().addActionListener(e -> handleSaveOrUpdate());

        // --- 3. VIEW ACTIONS ---
        view.getBtnPdf().addActionListener(e -> exportToPDF());

        view.getBtnEdit().addActionListener(e -> {
            int row = view.getTable().getSelectedRow();
            if(row == -1) JOptionPane.showMessageDialog(view, "Please select a student to edit.");
            else prepareUpdate(view.getTable().convertRowIndexToModel(row));
        });

        // --- 4. FILTERS ---
        setupFilters();
        view.getBtnResetFilters().addActionListener(e -> resetFilters());

        setupFormKeyNavigation();  // Add this line

        // --- 5. FORM DYNAMICS ---
        view.getInputCourseCombo().addActionListener(e -> updateSessionsForSelectedCourse());
        view.getInputBookLimitCombo().addActionListener(e -> updateDerivedFields());

        // --- 6. ROW DOUBLE-CLICK PREVIEW ---
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int viewRow = view.getTable().rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                int modelRow = view.getTable().convertRowIndexToModel(viewRow);
                String cardId = String.valueOf(view.getTableModel().getValueAt(modelRow, 1));
                openStudentPreview(cardId);
            }
        });
    }

    private void configureTableColumns() {
        JTable t = view.getTable();
        // Hide extra columns (keep only: S.No, Card ID, Name, Course, Roll No)
        for (int c = 5; c < t.getColumnCount(); c++) {
            javax.swing.table.TableColumn col = t.getColumnModel().getColumn(c);
            col.setMinWidth(0);
            col.setMaxWidth(0);
            col.setPreferredWidth(0);
            col.setResizable(false);
        }
        // Friendly widths for visible columns
        t.getColumnModel().getColumn(0).setPreferredWidth(50);
        t.getColumnModel().getColumn(1).setPreferredWidth(120);
        t.getColumnModel().getColumn(2).setPreferredWidth(220);
        t.getColumnModel().getColumn(3).setPreferredWidth(80);
        t.getColumnModel().getColumn(4).setPreferredWidth(80);
    }

    // ==========================================
    // LOGIC: FORM HANDLING & SAVING
    // ==========================================
    private void prepareRegistration() {
        isEditMode = false;
        editingCardId = null;
        view.setFormTitle("NEW STUDENT REGISTRATION");
        view.setSaveButtonText("REGISTER STUDENT");
        view.clearForm();
        view.setStatusEnabled(false);
        view.setInputStatus("ACTIVE");
        updateSessionsForSelectedCourse();
        updateDerivedFields();
        view.showCard("FORM");
    }

    private void prepareUpdate(int row) {
        isEditMode = true;
        editingCardId = view.getTableModel().getValueAt(row, 1).toString();

        Student s = dao.getStudentByCardId(editingCardId);
        if (s != null) {
            view.setInputName(s.getName());
            view.setInputRoll(String.valueOf(s.getRoll()));
            view.setInputPhone(String.valueOf(s.getPhone()));
            view.setInputAddr(s.getAddress());
            view.setInputCourse(s.getCourse());
            updateSessionsForSelectedCourse();
            view.setInputSession(s.getSession());
            view.setInputBookLimit(String.valueOf(s.getBookLimit()));
            view.setInputStatus(s.getStatus());
            view.setIssuedByText(s.getIssuedBy());
            view.setIssueDateText(formatDate(s.getIssueDate()));
            view.setFeeText(String.valueOf(s.getFee()));

            view.setStatusEnabled(true);
            view.setFormTitle("UPDATE STUDENT DETAILS");
            view.setSaveButtonText("UPDATE RECORD");
            view.showCard("FORM");
        }
    }

    private void prepareUpdateByCardId(String cardId) {
        // Select the row if it's visible in table (best-effort), but always load from DB.
        this.isEditMode = true;
        this.editingCardId = cardId;

        Student s = dao.getStudentByCardId(cardId);
        if (s == null) {
            JOptionPane.showMessageDialog(view, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        view.setInputName(s.getName());
        view.setInputRoll(String.valueOf(s.getRoll()));
        view.setInputPhone(String.valueOf(s.getPhone()));
        view.setInputAddr(s.getAddress());
        view.setInputCourse(s.getCourse());
        updateSessionsForSelectedCourse();
        view.setInputSession(s.getSession());
        view.setInputBookLimit(String.valueOf(s.getBookLimit()));
        view.setInputStatus(s.getStatus());
        view.setIssuedByText(s.getIssuedBy());
        view.setIssueDateText(formatDate(s.getIssueDate()));
        view.setFeeText(String.valueOf(s.getFee()));

        view.setStatusEnabled(true);
        view.setFormTitle("UPDATE STUDENT DETAILS");
        view.setSaveButtonText("UPDATE RECORD");
        view.showCard("FORM");
    }

    private void openStudentPreview(String cardId) {
        Student s = dao.getStudentByCardId(cardId);
        if (s == null) {
            JOptionPane.showMessageDialog(view, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        StudentPreviewDialog dlg = new StudentPreviewDialog(
            SwingUtilities.getWindowAncestor(view),
            s,
            dao.getBorrowHistory(cardId),
            pdfService,
            () -> prepareUpdateByCardId(cardId)
        );
        dlg.setVisible(true);
    }

    private void handleSaveOrUpdate() {
        String rawName = view.getInputName();
        String rollStr = view.getInputRoll();
        String phoneStr = view.getInputPhone();
        String addr = view.getInputAddr();
        String course = view.getInputCourse();
        String session = view.getInputSession();

        if (addr == null || addr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Address is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String name = StudentLogic.toTitleCaseName(rawName);
            StudentLogic.validateRollNumber(rollStr, session);
            int roll = Integer.parseInt(rollStr);
            long phone = StudentLogic.parseAndValidateContact(phoneStr);

            int limit = view.getInputBookLimit();
            double fee = StudentLogic.feeForBookLimit(limit);

            if (isEditMode) {
                if (dao.isRollTakenInCourseSession(roll, course, session, editingCardId)) {
                    throw new ValidationException("Roll No already exists for this course and session.");
                }

                Student existing = dao.getStudentByCardId(editingCardId);
                if (existing == null) {
                    JOptionPane.showMessageDialog(view, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Student updated = new Student(
                    existing.getCardId(),
                    roll,
                    name,
                    phone,
                    addr.trim(),
                    course,
                    session,
                    existing.getReceiptNo(),
                    existing.getIssuedBy(),
                    existing.getIssueDate(),
                    limit,
                    fee,
                    view.getInputStatus()
                );

                if (dao.updateStudent(updated)) {
                    JOptionPane.showMessageDialog(view, "Updated Successfully!");
                    refreshTable();
                    view.showCard("LIST");
                } else {
                    JOptionPane.showMessageDialog(view, "Update Failed.");
                }
                return;
            }

            if (dao.isRollTakenInCourseSession(roll, course, session, null)) {
                throw new ValidationException("Roll No already exists for this course and session.");
            }

            Student created = dao.registerStudent(
                roll,
                name,
                phone,
                addr.trim(),
                course,
                session,
                CurrentUserContext.getUserId(),
                limit,
                fee,
                "ACTIVE"
            );

            refreshTable();
            prepareRegistration();
            showRegistrationSuccessDialog(created);

        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(view, ve.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // LOGIC: LIBRARY CARD PREVIEW & IMAGE GENERATION
    // ==========================================
    private void showRegistrationSuccessDialog(Student s) {
        String msg =
            "<html><body style='width: 320px; font-family: sans-serif;'>" +
            "<h2 style='margin:0 0 10px 0;'>Registration Successful</h2>" +
            "<b>Card ID:</b> <span style='color:#1F3E6D;'>" + s.getCardId() + "</span><br>" +
            "<b>Receipt No:</b> <span style='color:#1F3E6D;'>" + s.getReceiptNo() + "</span><br><br>" +
            "Download PDFs now?" +
            "</body></html>";

        Object[] options = {"Download Receipt (PDF)", "Download Library Card (PDF)", "Close"};
        while (true) {
            int choice = JOptionPane.showOptionDialog(
                view,
                msg,
                "Saved",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 0) {
                downloadReceiptPdf(s);
            } else if (choice == 1) {
                downloadLibraryCardPdf(s);
            } else {
                break;
            }
        }
    }

    private void downloadReceiptPdf(Student s) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(s.getReceiptNo() + "_Receipt.pdf"));
        if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            try {
                pdfService.generateReceiptPdf(s, fc.getSelectedFile().toPath());
                JOptionPane.showMessageDialog(view, "Receipt PDF saved.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(view, "Error saving receipt PDF: " + ex.getMessage());
            }
        }
    }

    private void downloadLibraryCardPdf(Student s) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(s.getCardId() + "_LibraryCard.pdf"));
        if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            try {
                pdfService.generateLibraryCardPdf(s, fc.getSelectedFile().toPath());
                JOptionPane.showMessageDialog(view, "Library Card PDF saved.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(view, "Error saving library card PDF: " + ex.getMessage());
            }
        }
    }

    // --- NEW: GENERATE CARD AS PNG IMAGE ---
    private void exportLibraryCardImage(Student s) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(s.getCardId().replace(" ", "_") + "_Card.png"));

        if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            try {
                // 1. Setup Image Dimensions (Standard ID Card ratio roughly)
                int width = 600;
                int height = 400;
                BufferedImage cardImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = cardImage.createGraphics();

                // Enable Anti-aliasing for smooth text and shapes
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // 2. Draw Background (Subtle Blue Gradient)
                GradientPaint gp = new GradientPaint(0, 0, CARD_BG_START, width, height, CARD_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, width, height);
                // Add a subtle border
                g2.setColor(CARD_BLUE_DARK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(1, 1, width-2, height-2);

                // 3. Draw Header (Blue curved area)
                g2.setColor(CARD_BLUE_DARK);
                g2.fillRect(0, 0, width, 60);

                // 4. Add Logo & Title to Header
                try {
                    ImageIcon icon = new ImageIcon("lib/icons/clglogo.png");
                    Image logo = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    g2.drawImage(logo, 15, 10, null);
                } catch (Exception e) { /* Ignore missing logo */ }

                g2.setColor(Color.WHITE);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
                g2.drawString("ARCADE BUSINESS COLLEGE", 65, 38);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                g2.drawString("Official Library Card", 65, 53);

                // 5. Photo Placeholder (Left Side)
                int photoX = 25; int photoY = 85; int photoW = 100; int photoH = 120;
                // g2.setColor(Color.LIGHT_GRAY);
                // g2.fillRect(photoX, photoY, photoW, photoH);
                // g2.setColor(CARD_TEXT_GRAY);
                // g2.drawRect(photoX, photoY, photoW, photoH);
                // g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                // g2.drawString("PHOTO", photoX + 25, photoY + 65);

                // Card ID below photo
                g2.setColor(CARD_BLUE_DARK);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                g2.drawString("ID: " + s.getCardId(), photoX, photoY + photoH + 25);

                // 6. Student Details (Right Side)
                int textX = 150; int textY = 100; int lineHeight = 30;

                drawCardField(g2, "Name:", s.getName(), textX, textY);
                drawCardField(g2, "Roll No:", String.valueOf(s.getRoll()), textX, textY + lineHeight);
                drawCardField(g2, "Receipt No:", String.valueOf(s.getReceiptNo()), textX, textY + lineHeight*2);
                drawCardField(g2, "Course:", s.getCourse(), textX, textY + lineHeight*3);
                drawCardField(g2, "Session:", s.getSession(), textX, textY + lineHeight*4);
                drawCardField(g2, "Phone:", String.valueOf(s.getPhone()), textX, textY + lineHeight*5);
                drawCardField(g2, "Book Limit:", String.valueOf(s.getBookLimit()), textX, textY + lineHeight*6);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                String dateStr = (s.getIssueDate() != null) ? sdf.format(s.getIssueDate()) : sdf.format(new Date());
                drawCardField(g2, "Issued:", dateStr, textX, textY + lineHeight*7);

                // 7. Footer / Signature Area
                int footerY = height - 40;
                g2.setColor(CARD_TEXT_GRAY);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(width - 180, footerY, width - 30, footerY); // Signature line
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
                g2.drawString("Authorized Signature", width - 150, footerY + 15);

                // Subtle bottom accent
                g2.setColor(CARD_BLUE_LIGHT);
                g2.fillRect(0, height - 5, width, 5);

                // 8. Clean up and Save as PNG
                g2.dispose();
                ImageIO.write(cardImage, "png", fc.getSelectedFile());
                JOptionPane.showMessageDialog(view, "Card Image Saved Successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(view, "Error creating image: " + ex.getMessage());
            }
        }
    }

    // Helper to draw label and value pairs
    private void drawCardField(Graphics2D g2, String label, String value, int x, int y) {
        g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        g2.setColor(CARD_BLUE_DARK);
        g2.drawString(label, x, y);

        g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        g2.setColor(Color.BLACK);
        g2.drawString(value, x + 80, y);
    }

    // ==========================================
    // LOGIC: TABLE & FILTERS
    // ==========================================
    private void refreshTable() {
        view.getTableModel().setRowCount(0);
        List<Student> list = dao.getActiveStudents();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        int sno = 1;
        for(Student s : list) {
            view.getTableModel().addRow(new Object[]{
                sno++,
                s.getCardId(),
                s.getName(),
                s.getCourse(),
                s.getRoll(),
                s.getSession(),
                (s.getIssueDate() != null ? sdf.format(s.getIssueDate()) : ""),
                s.getIssuedBy(),
                s.getBookLimit(),
                s.getFee(),
                s.getStatus()
            });
        }
        view.updateTotalCount(list.size());
    }

    private void setupFilters() {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        };
        view.getFltCardId().getDocument().addDocumentListener(dl);
        view.getFltName().getDocument().addDocumentListener(dl);
        view.getFltRoll().getDocument().addDocumentListener(dl);
        view.getFltIssueBy().getDocument().addDocumentListener(dl);

        view.getFltCourse().addActionListener(e -> applyFilters());
        view.getFltSession().addActionListener(e -> applyFilters());
        view.getFltBookLimit().addActionListener(e -> applyFilters());
        view.getFltFromDate().addChangeListener(e -> applyFilters());
        view.getFltToDate().addChangeListener(e -> applyFilters());
    }

    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        addTextFilter(filters, view.getFltCardId(), 1);
        addTextFilter(filters, view.getFltName(), 2);
        addTextFilter(filters, view.getFltRoll(), 4);
        addComboFilter(filters, view.getFltCourse(), 3);
        addComboFilter(filters, view.getFltSession(), 5);
        addTextFilter(filters, view.getFltIssueBy(), 7);
        addComboFilter(filters, view.getFltBookLimit(), 8);

        RowFilter<Object, Object> dateRange = buildIssueDateRangeFilter();
        if (dateRange != null) filters.add(dateRange);

        view.getSorter().setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        view.updateTotalCount(view.getTable().getRowCount());
    }

    private void addTextFilter(List<RowFilter<Object, Object>> filters, JTextField field, int col) {
        String txt = field.getText().trim();
        if(!txt.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(txt), col));
    }

    private void addComboFilter(List<RowFilter<Object, Object>> filters, JComboBox<String> box, int col) {
        String val = (String) box.getSelectedItem();
        if(val != null && !val.equals("All")) filters.add(RowFilter.regexFilter("^" + Pattern.quote(val) + "$", col));
    }

    private RowFilter<Object, Object> buildIssueDateRangeFilter() {
        java.util.Date from = (java.util.Date) view.getFltFromDate().getValue();
        java.util.Date to = (java.util.Date) view.getFltToDate().getValue();
        if (from == null || to == null) return null;

        LocalDate fromDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (fromDate.isAfter(toDate)) {
            LocalDate tmp = fromDate;
            fromDate = toDate;
            toDate = tmp;
        }

        final LocalDate f = fromDate;
        final LocalDate t = toDate;

        return new RowFilter<Object, Object>() {
            @Override
            public boolean include(Entry<?, ?> entry) {
                Object v = entry.getValue(6); // Issue Date column
                if (v == null) return false;
                String s = v.toString().trim();
                if (s.isEmpty()) return false;
                try {
                    LocalDate d = LocalDate.parse(s);
                    return (!d.isBefore(f)) && (!d.isAfter(t));
                } catch (Exception ignored) {
                    return false;
                }
            }
        };
    }

    private void updateSessionsForSelectedCourse() {
        String course = view.getInputCourse();
        try {
            List<String> sessions = StudentLogic.generateSessions(course, StudentLogic.currentYear(), 3);
            view.setSessionOptions(sessions.toArray(new String[0]));
        } catch (ValidationException e) {
            // If course is invalid, do not change sessions.
        }
    }

    private void updateDerivedFields() {
        int limit = view.getInputBookLimit();
        try {
            view.setFeeText(String.valueOf(StudentLogic.feeForBookLimit(limit)));
        } catch (ValidationException e) {
            view.setFeeText("");
        }

        if (!isEditMode) {
            view.setIssuedByText(CurrentUserContext.getDisplayName());
            view.setIssueDateText(formatDate(new java.util.Date()));
            view.setInputStatus("ACTIVE");
        }
    }

    private String formatDate(java.util.Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private void resetFilters() {
        view.getFltCardId().setText("");
        view.getFltName().setText(""); view.getFltRoll().setText("");
        view.getFltIssueBy().setText("");
        view.getFltCourse().setSelectedIndex(0); view.getFltBookLimit().setSelectedIndex(0);
        view.getFltSession().setSelectedIndex(0);
    }

    private Map<String, Integer> showFieldSelectionDialog() {
        String[] allColumns = {"S.No", "Card ID", "Name", "Course", "Roll No", "Session", "Issue Date", "Issued By", "Book Limit", "Fee", "Status"};

        JPanel panel = new JPanel(new GridLayout(allColumns.length, 1, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JCheckBox[] checkboxes = new JCheckBox[allColumns.length];
        for (int i = 0; i < allColumns.length; i++) {
            checkboxes[i] = new JCheckBox(allColumns[i], true); // All selected by default
            panel.add(checkboxes[i]);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(300, 350));

        int result = JOptionPane.showConfirmDialog(view, scroll,
            "Select Fields to Print",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        Map<String, Integer> selection = new LinkedHashMap<>();
        for (int i = 0; i < allColumns.length; i++) {
            if (checkboxes[i].isSelected()) {
                selection.put(allColumns[i], i);
            }
        }

        return selection.isEmpty() ? null : selection;
    }

    // Replace the exportToPDF() method to add footer before doc.close():

    private void exportToPDF() {
        // Show field selection dialog
        Map<String, Integer> fieldSelection = showFieldSelectionDialog();
        if (fieldSelection == null) return;

        // Ask for Orientation
        String[] options = {"Portrait", "Landscape"};
        int choice = JOptionPane.showOptionDialog(view,
            "Select Page Orientation:",
            "PDF Settings",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        if (choice == -1) return;

        boolean isLandscape = (choice == 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("Student_Report"+ dateFormat +".pdf"));

        if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {

            Document doc = isLandscape ?
                new Document(com.itextpdf.text.PageSize.A4.rotate()) :
                new Document(com.itextpdf.text.PageSize.A4);

            try {
                PdfWriter.getInstance(doc, new FileOutputStream(fc.getSelectedFile()));
                doc.open();

                float bannerHeight = 0;

                // --- ADD BANNER IMAGE ---
                try {
                    String imgPath = "lib/icons/header.png";
                    com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(imgPath);

                    float pageWidth = doc.getPageSize().getWidth();
                    float pageHeight = doc.getPageSize().getHeight();

                    float scaler = (pageWidth / img.getWidth()) * 100;
                    img.scalePercent(scaler);

                    bannerHeight = img.getScaledHeight();
                    img.setAbsolutePosition(0, pageHeight - bannerHeight);

                    doc.add(img);

                } catch (Exception e) {
                    System.err.println("Header image not found: " + e.getMessage());
                }

                Paragraph spacer = new Paragraph(" ");
                spacer.setSpacingAfter(bannerHeight + 20);
                doc.add(spacer);

                // --- ADD HEADER ELEMENT (Logo, Date, Time) ---
                doc.add(createPDFHeader());

                // --- ADD REPORT TITLE ---
                Paragraph title = new Paragraph("Student Monitoring Report");
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // Create table with selected fields only
                String[] selectedColumns = fieldSelection.keySet().toArray(new String[0]);
                PdfPTable pdfTable = new PdfPTable(selectedColumns.length);
                pdfTable.setWidthPercentage(100);

                // Headers
                for (String col : selectedColumns) {
                    PdfPCell cell = new PdfPCell(new Phrase(col));
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }

                // Data
                for (int i = 0; i < view.getTable().getRowCount(); i++) {
                    for (Integer colIndex : fieldSelection.values()) {
                        Object val = view.getTable().getValueAt(i, colIndex);
                        pdfTable.addCell(val != null ? val.toString() : "");
                    }
                }

                doc.add(pdfTable);

                // --- NEW: ADD FOOTER WITH SIGNATURE ---
                doc.add(createPDFFooter());

                JOptionPane.showMessageDialog(view, "PDF Exported Successfully!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                doc.close();
            }
        }
    }

    // --- NEW: CREATE PDF FOOTER WITH AUTHORITY SIGNATURE ---
    private com.itextpdf.text.Element createPDFFooter() {
        com.itextpdf.text.pdf.PdfPTable footerTable = new com.itextpdf.text.pdf.PdfPTable(3);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(30);

        // --- LEFT CELL: DATE ---
        com.itextpdf.text.pdf.PdfPCell dateCell = new com.itextpdf.text.pdf.PdfPCell();
        dateCell.setBorder(0);
        dateCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        dateCell.setPadding(10);

        Font smallFont = new Font(Font.FontFamily.HELVETICA, 9);
        Phrase datePhrase = new Phrase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        datePhrase.add(new Chunk("Date: " + dateFormat.format(new Date()) + "\n\n\n", smallFont));
        datePhrase.add(new Chunk("_".repeat(20), smallFont));
        datePhrase.add(new Chunk("\nPrepared By", smallFont));
        dateCell.addElement(datePhrase);
        footerTable.addCell(dateCell);

        // --- MIDDLE CELL: EMPTY ---
        com.itextpdf.text.pdf.PdfPCell emptyCell = new com.itextpdf.text.pdf.PdfPCell();
        emptyCell.setBorder(0);
        emptyCell.setPadding(10);
        footerTable.addCell(emptyCell);

        // --- RIGHT CELL: AUTHORITY SIGNATURE ---
        com.itextpdf.text.pdf.PdfPCell signatureCell = new com.itextpdf.text.pdf.PdfPCell();
        signatureCell.setBorder(0);
        signatureCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        signatureCell.setPadding(10);

        Phrase signaturePhrase = new Phrase();
        signaturePhrase.add(new Chunk("Authorized By:\n\n\n", smallFont));
        signaturePhrase.add(new Chunk("_".repeat(20), smallFont));
        signaturePhrase.add(new Chunk("\n", smallFont));

        Font nameFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        signaturePhrase.add(new Chunk("Library Authority\n", nameFont));
        signaturePhrase.add(new Chunk("(Signature & Seal)", smallFont));

        signatureCell.addElement(signaturePhrase);
        footerTable.addCell(signatureCell);

        return footerTable;
    }

    // --- NEW: CREATE PDF HEADER WITH LOGO, DATE & TIME ---
    private com.itextpdf.text.Element createPDFHeader() {
        com.itextpdf.text.pdf.PdfPTable headerTable = new com.itextpdf.text.pdf.PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(5);

        // Get current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String reportDate = dateFormat.format(now);
        String reportTime = timeFormat.format(now);

        // --- LEFT CELL: LOGO ---
        com.itextpdf.text.pdf.PdfPCell logoCell = new com.itextpdf.text.pdf.PdfPCell();
        try {
            com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("lib/icons/clglogo.png");
            logo.scalePercent(10); // Scale to 30% of original size
            logoCell.addElement(logo);
        } catch (Exception e) {
            // Fallback if logo not found
            logoCell.addElement(new Phrase("Logo"));
        }
        logoCell.setBorder(0);
        logoCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
        logoCell.setPadding(5);
        headerTable.addCell(logoCell);

        // --- MIDDLE CELL: EMPTY SPACE ---
        com.itextpdf.text.pdf.PdfPCell spacerCell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(""));
        spacerCell.setBorder(0);
        spacerCell.setPadding(5);
        headerTable.addCell(spacerCell);

        // --- RIGHT CELL: DATE & TIME ---
        com.itextpdf.text.pdf.PdfPCell dateTimeCell = new com.itextpdf.text.pdf.PdfPCell();
        dateTimeCell.setBorder(0);
        dateTimeCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        dateTimeCell.setPadding(5);

        Phrase datePhrase = new Phrase();
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

        datePhrase.add(new Chunk("Report Generated On:\n", boldFont));
        datePhrase.add(new Chunk(reportDate + " | " + reportTime, normalFont));
        dateTimeCell.addElement(datePhrase);

        headerTable.addCell(dateTimeCell);

        // Add a separator line
        com.itextpdf.text.Paragraph separator = new com.itextpdf.text.Paragraph("_".repeat(40));
        separator.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        separator.setSpacingAfter(10);

        // Create a container for both table and separator
        com.itextpdf.text.pdf.PdfPTable container = new com.itextpdf.text.pdf.PdfPTable(1);
        com.itextpdf.text.pdf.PdfPCell containerCell = new com.itextpdf.text.pdf.PdfPCell(headerTable);
        containerCell.setBorder(0);
        containerCell.setPadding(0);
        container.addCell(containerCell);

        containerCell = new com.itextpdf.text.pdf.PdfPCell(separator);
        containerCell.setBorder(0);
        containerCell.setPadding(0);
        container.addCell(containerCell);

        return container;
    }

    private String toTitleCase(String input) {
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                         .append(word.substring(1))
                         .append(" ");
            }
        }
        return titleCase.toString().trim();
    }

    private boolean validateRollNumber(String rollStr, String session) {
        // Roll must be exactly 5 digits
        if (!rollStr.matches("\\d{5}")) {
            return false;
        }

        // Extract session year (e.g., "2024-2027" -> 24)
        String[] parts = session.split("-");
        if (parts.length < 1) return false;

        String sessionYear = parts[0];
        String lastTwoDigits = sessionYear.substring(Math.max(0, sessionYear.length() - 2));

        // Roll must start with session year's last two digits
        return rollStr.startsWith(lastTwoDigits);
    }

    private void setupFormKeyNavigation() {
        JTextField txtName = view.getInputNameField();
        JTextField txtRoll = view.getInputRollField();
        JTextField txtPhone = view.getInputPhoneField();
        JTextArea txtAddr = view.getInputAddressField();
        JComboBox<String> cmbCourse = view.getInputCourseCombo();
        JComboBox<String> cmbSession = view.getInputSessionCombo();
        JComboBox<String> cmbBookLimit = view.getInputBookLimitCombo();
        JButton btnSave = view.getSaveButton();

        bindEnterToNextFocus(txtName, txtRoll);
        bindEnterToNextFocus(txtRoll, txtPhone);
        bindEnterToNextFocus(txtPhone, txtAddr);
        bindEnterToNextFocus(txtAddr, cmbCourse);
        bindEnterToNextFocus(cmbCourse, cmbSession);
        bindEnterToNextFocus(cmbSession, cmbBookLimit);
        bindEnterToNextFocus(cmbBookLimit, btnSave);

        InputMap saveInputMap = btnSave.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap saveActionMap = btnSave.getActionMap();
        saveInputMap.put(KeyStroke.getKeyStroke("ENTER"), "submitForm");
        saveActionMap.put("submitForm", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                handleSaveOrUpdate();
            }
        });
    }

    private void bindEnterToNextFocus(JComponent source, Component nextFocus) {
        InputMap inputMap = source.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = source.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "moveFocusNext");
        actionMap.put("moveFocusNext", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nextFocus.requestFocusInWindow();
            }
        });
    }
}
