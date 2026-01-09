package com.library.ui;

import com.library.dao.StudentDAO;
import com.library.model.Student;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;   // <--- ADDED THIS
import java.io.IOException;  // <--- ADDED THIS
import javax.imageio.ImageIO;
import java.text.SimpleDateFormat;
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
    private static final String CURRENT_USER = "ADMIN"; 

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
        view.getBtnPrint().addActionListener(e -> printTable());
        view.getBtnExport().addActionListener(e -> exportToCSV());
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
        view.showCard("FORM");
    }

    private void prepareUpdate(int row) {
        isEditMode = true;
        editingCardId = view.getTable().getValueAt(row, 0).toString();
        
        Student s = dao.getStudentByCardId(editingCardId);
        if (s != null) {
            view.setInputName(s.getName());
            view.setInputRoll(String.valueOf(s.getRoll()));
            view.setInputPhone(String.valueOf(s.getPhone()));
            view.setInputAddr(s.getAddress());
            view.setInputCourse(s.getCourse());
            view.setInputSession(s.getSession());
            view.setInputBookLimit(String.valueOf(s.getBookLimit()));
            view.setInputStatus(s.getStatus());
            
            view.setStatusEnabled(true);
            view.setFormTitle("UPDATE STUDENT DETAILS");
            view.setSaveButtonText("UPDATE RECORD");
            view.showCard("FORM");
        }
    }

    private void handleSaveOrUpdate() {
        // 1. VALIDATION
        String name = view.getInputName().trim();
        String rollStr = view.getInputRoll().trim();
        String phoneStr = view.getInputPhone();
        String addr = view.getInputAddr();

        if (name.isEmpty() || rollStr.isEmpty() || phoneStr.isEmpty() || addr.isEmpty()) {
            JOptionPane.showMessageDialog(view, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate and convert name to Title Case
        if (!name.matches("[a-zA-Z\\s]+")) {
            JOptionPane.showMessageDialog(view, "Name must contain only letters and spaces.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        name = toTitleCase(name);
        
        // Validate Roll Number
        String session = view.getInputSession();
        if (!validateRollNumber(rollStr, session)) {
            JOptionPane.showMessageDialog(view, 
                "Roll No must:\n" +
                "- Start with session year (e.g., 24 for 2024-2027)\n" +
                "- Be exactly 5 digits\n" +
                "- Format: YY###", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (phoneStr.length() != 10) {
            JOptionPane.showMessageDialog(view, "Phone must be 10 digits.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int roll = Integer.parseInt(rollStr);
        long phone = Long.parseLong(phoneStr);
        int limit = view.getInputBookLimit();
        double fee = (limit == 1) ? 500.00 : 800.00;
        String course = view.getInputCourse();

        // 2. EXECUTION
        if (isEditMode) {
            // --- UPDATE EXISTING ---
            Student s = new Student(editingCardId, roll, name, phone, addr, course, session, "EXISTING", CURRENT_USER, null, limit, fee, view.getInputStatus());

            if (dao.updateStudent(s)) {
                JOptionPane.showMessageDialog(view, "Updated Successfully!");
                refreshTable();
                view.showCard("LIST");
            } else {
                JOptionPane.showMessageDialog(view, "Update Failed.");
            }
        } else {
            // --- NEW REGISTRATION ---
            String cardId = dao.generateCardId();
            String receipt = dao.generateReceiptNo();
            java.sql.Date regDate = new java.sql.Date(System.currentTimeMillis());
            
            Student s = new Student(cardId, roll, name, phone, addr, course, session, receipt, CURRENT_USER, regDate, limit, fee, "ACTIVE");

            if (dao.addStudent(s)) {
                view.clearForm();
                refreshTable();
                // Show the "Digital Card" dialog
                showLibraryCardPreview(s);
                view.showCard("LIST");
            } else {
                JOptionPane.showMessageDialog(view, "Registration Failed (Database Error).");
            }
        }
    }
    
    // ==========================================
    // LOGIC: LIBRARY CARD PREVIEW & IMAGE GENERATION
    // ==========================================
    private void showLibraryCardPreview(Student s) {
        String htmlCard = String.format(
            "<html><body style='width: 250px; font-family: sans-serif;'>" +
            "<div style='border: 2px solid #1F3E6D; padding: 10px; background-color: #f5f8fa;'>" +
            "<h3 style='text-align: center; color: #1F3E6D; margin: 0;'>DIGITAL CARD GENERATED</h3>" +
            "<hr>" +
            "<b>ID:</b> <font color='red'>%s</font><br>" +
            "<b>Name:</b> %s<br>" +
            "<b>Course:</b> %s" +
            "</div></body></html>",
            s.getCardId(), s.getName(), s.getCourse()
        );

        Object[] options = {"Download Card Image (PNG)", "Close"};

        int choice = JOptionPane.showOptionDialog(view,
            htmlCard,
            "Registration Successful!",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            exportLibraryCardImage(s);
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
        List<Student> list = dao.getAllStudents();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for(Student s : list) {
            view.getTableModel().addRow(new Object[]{ 
                s.getCardId(), s.getName(), s.getRoll(), s.getCourse(), s.getSession(), 
                (s.getIssueDate() != null ? sdf.format(s.getIssueDate()) : ""), 
                s.getIssuedBy(), s.getBookLimit(), s.getFee(), s.getStatus() 
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
        view.getFltName().getDocument().addDocumentListener(dl);
        view.getFltRoll().getDocument().addDocumentListener(dl);
        view.getFltIssueBy().getDocument().addDocumentListener(dl);
        view.getFltIssueDate().getDocument().addDocumentListener(dl);
        
        view.getFltCourse().addActionListener(e -> applyFilters());
        view.getFltSession().addActionListener(e -> applyFilters());
        view.getFltBookLimit().addActionListener(e -> applyFilters());
    }
    
    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        addTextFilter(filters, view.getFltName(), 1);
        addTextFilter(filters, view.getFltRoll(), 2);
        addComboFilter(filters, view.getFltCourse(), 3);
        addComboFilter(filters, view.getFltSession(), 4);
        addTextFilter(filters, view.getFltIssueDate(), 5);
        addTextFilter(filters, view.getFltIssueBy(), 6);
        addComboFilter(filters, view.getFltBookLimit(), 7);
        view.getSorter().setRowFilter(RowFilter.andFilter(filters));
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
    
    private void resetFilters() {
        view.getFltName().setText(""); view.getFltRoll().setText(""); 
        view.getFltIssueBy().setText(""); view.getFltIssueDate().setText("");
        view.getFltCourse().setSelectedIndex(0); view.getFltBookLimit().setSelectedIndex(0);
        if(view.getFltSession().getItemCount()>1) view.getFltSession().setSelectedIndex(1);
    }

    // ==========================================
    // LOGIC: EXPORTS (MAIN TABLE)
    // ==========================================
    private void printTable() {
        // Show field selection dialog
        Map<String, Integer> fieldSelection = showFieldSelectionDialog();
        
        if (fieldSelection == null) return; // User cancelled
        
        // Create filtered table model with selected columns
        JTable filteredTable = createFilteredTableForPrint(fieldSelection);
        
        try {
            filteredTable.print();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Error: " + e.getMessage());
        }
    }
    
    private Map<String, Integer> showFieldSelectionDialog() {
        String[] allColumns = {"Card ID", "Name", "Roll", "Course", "Session", "Issue Date", "Issue By", "Limit", "Fee", "Status"};
        
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
    
    private JTable createFilteredTableForPrint(Map<String, Integer> selectedFields) {
        String[] selectedColumns = selectedFields.keySet().toArray(new String[0]);
        DefaultTableModel filteredModel = new DefaultTableModel(selectedColumns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        // Add data from current filtered table
        for (int i = 0; i < view.getTable().getRowCount(); i++) {
            Object[] row = new Object[selectedColumns.length];
            int colIndex = 0;
            for (Integer originalCol : selectedFields.values()) {
                row[colIndex++] = view.getTable().getValueAt(i, originalCol);
            }
            filteredModel.addRow(row);
        }
        
        JTable filteredTable = new JTable(filteredModel);
        filteredTable.setRowHeight(28);
        // Use java.awt.Font for Swing components
        filteredTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        return filteredTable;
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

    private void exportToCSV() {
        // Show field selection dialog
        Map<String, Integer> fieldSelection = showFieldSelectionDialog();
        if (fieldSelection == null) return;
        
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("students.csv"));
        if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                // Write headers
                String[] selectedColumns = fieldSelection.keySet().toArray(new String[0]);
                for (int i = 0; i < selectedColumns.length; i++) {
                    fw.write(selectedColumns[i]);
                    if (i < selectedColumns.length - 1) fw.write(",");
                }
                fw.write("\n");
                
                // Write data
                for (int i = 0; i < view.getTable().getRowCount(); i++) {
                    int colCount = 0;
                    for (Integer colIndex : fieldSelection.values()) {
                        fw.write(view.getTable().getValueAt(i, colIndex).toString());
                        colCount++;
                        if (colCount < selectedColumns.length) fw.write(",");
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(view, "CSV Saved!");
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage()); 
            }
        }
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

        // Add Enter key listener to Name field -> go to Roll
        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    txtRoll.requestFocus();
                }
            }
        });

        // Add Enter key listener to Roll field -> go to Phone
        txtRoll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    txtPhone.requestFocus();
                }
            }
        });

        // Add Enter key listener to Phone field -> go to Address
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    txtAddr.requestFocus();
                }
            }
        });

        // Add Enter key listener to Address field -> go to Course
        txtAddr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    cmbCourse.requestFocus();
                }
            }
        });

        // Disable Tab key in Address field (prevent tabs)
        txtAddr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                    e.consume(); // Prevent tab insertion
                    cmbCourse.requestFocus(); // Move to next field instead
                }
            }
        });

        // Add Enter key listener to Course combo -> go to Session
        cmbCourse.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    cmbSession.requestFocus();
                }
            }
        });

        // Add Enter key listener to Session combo -> go to Book Limit
        cmbSession.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    cmbBookLimit.requestFocus();
                }
            }
        });

        // Add Enter key listener to Book Limit combo -> go to Save button
        cmbBookLimit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    btnSave.requestFocus();
                }
            }
        });

        // Add Enter key listener to Save button -> save form
        btnSave.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    handleSaveOrUpdate();
                }
            }
        });
    }
}