package com.library.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.BaseFont;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class PdfReportService {

    public static class PageNumberer extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                
                String text = "Page " + writer.getPageNumber();
                float pageWidth = document.getPageSize().getWidth();
                float pageHeight = document.getPageSize().getHeight();
                
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
                cb.beginText();
                cb.setFontAndSize(bf, 9);
                cb.showTextAligned(Element.ALIGN_CENTER, text, pageWidth / 2, 20, 0);
                cb.endText();
                
                cb.restoreState();
            } catch (Exception e) {
                System.err.println("Error adding page number: " + e.getMessage());
            }
        }
    }

    public void exportToPdf(Component parent, JTable table, String reportTitle, String defaultFileNamePrefix) {
        // 1. Show field selection dialog
        Map<String, Integer> fieldSelection = showFieldSelectionDialog(parent, table);
        if (fieldSelection == null) return;

        // 2. Ask for Orientation
        String[] options = {"Portrait", "Landscape"};
        int choice = JOptionPane.showOptionDialog(parent,
            "Select Page Orientation:",
            "PDF Settings",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        if (choice == -1) return;

        boolean isLandscape = (choice == 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");

        JFileChooser fc = new JFileChooser();
        String defaultFileName = defaultFileNamePrefix + "_" + dateFormat.format(new java.util.Date()) + ".pdf";
        fc.setSelectedFile(new java.io.File(defaultFileName));

        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            Document doc = isLandscape ?
                new Document(PageSize.A4.rotate()) :
                new Document(PageSize.A4);

            try {
                PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fc.getSelectedFile()));
                writer.setPageEvent(new PageNumberer());
                doc.open();

                // --- ADD BANNER IMAGE ---
                addBannerImage(doc);

                // --- ADD HEADER ELEMENT (Logo, Date, Time) ---
                doc.add(createPDFHeader());

                // --- ADD REPORT TITLE ---
                Paragraph title = new Paragraph(reportTitle, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // Create table with selected fields only
                String[] selectedColumns = fieldSelection.keySet().toArray(new String[0]);
                PdfPTable pdfTable = new PdfPTable(selectedColumns.length);
                pdfTable.setWidthPercentage(100);

                // Headers
                for (String col : selectedColumns) {
                    PdfPCell cell = new PdfPCell(new Phrase(col, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    pdfTable.addCell(cell);
                }

                // Data
                for (int i = 0; i < table.getRowCount(); i++) {
                    int modelRow = table.convertRowIndexToModel(i);
                    for (Integer colIndex : fieldSelection.values()) {
                        Object val = table.getModel().getValueAt(modelRow, colIndex);
                        PdfPCell cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", new Font(Font.FontFamily.HELVETICA, 9)));
                        cell.setPadding(4);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        pdfTable.addCell(cell);
                    }
                }

                doc.add(pdfTable);

                // --- ADD FOOTER WITH SIGNATURE ---
                doc.add(createPDFFooter());

                doc.close();
                JOptionPane.showMessageDialog(parent, "PDF Exported Successfully!");

            } catch (Exception ex) {
                if (doc.isOpen()) {
                    try { doc.close(); } catch (Exception ignored) {}
                }
                JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void addBannerImage(Document doc) {
        try {
            String imgPath = "lib/icons/header.png";
            Image img = Image.getInstance(imgPath);

            float pageWidth = doc.getPageSize().getWidth();
            float pageHeight = doc.getPageSize().getHeight();

            float scaler = (pageWidth / img.getWidth()) * 100;
            img.scalePercent(scaler);

            float bannerHeight = img.getScaledHeight();
            img.setAbsolutePosition(0, pageHeight - bannerHeight);

            doc.add(img);

            Paragraph spacer = new Paragraph(" ");
            spacer.setSpacingAfter(bannerHeight + 3);
            doc.add(spacer);
        } catch (Exception e) {
            System.err.println("Header image not found: " + e.getMessage());
        }
    }

    private Map<String, Integer> showFieldSelectionDialog(Component parent, JTable table) {
        int colCount = table.getColumnCount();
        String[] allColumns = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            allColumns[i] = table.getColumnName(i);
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JCheckBox[] checkboxes = new JCheckBox[allColumns.length];
        for (int i = 0; i < allColumns.length; i++) {
            checkboxes[i] = new JCheckBox(allColumns[i], true);
            panel.add(checkboxes[i]);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(300, 350));

        int result = JOptionPane.showConfirmDialog(parent, scroll,
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

    public static Element createPDFFooter() {
        PdfPTable footerTable = new PdfPTable(3);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(30);

        Font smallFont = new Font(Font.FontFamily.HELVETICA, 9);

        // --- LEFT CELL: PREPARED BY ---
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(0);
        leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        leftCell.setPadding(10);
        Phrase p1 = new Phrase();
        p1.add(new Chunk("\n\n\n", smallFont));
        p1.add(new Chunk("____________________", smallFont));
        p1.add(new Chunk("\nPrepared By", smallFont));
        leftCell.addElement(p1);
        footerTable.addCell(leftCell);

        // --- MIDDLE CELL: EMPTY ---
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(0);
        footerTable.addCell(emptyCell);

        // --- RIGHT CELL: AUTHORITY SIGNATURE ---
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(0);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rightCell.setPadding(10);
        Phrase p2 = new Phrase();
        p2.add(new Chunk("Authorized By:\n\n\n", smallFont));
        p2.add(new Chunk("____________________", smallFont));
        p2.add(new Chunk("\n", smallFont));
        p2.add(new Chunk("Library Authority\n", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD)));
        p2.add(new Chunk("(Signature & Seal)", smallFont));
        rightCell.addElement(p2);
        footerTable.addCell(rightCell);

        return footerTable;
    }

    public static Element createPDFHeader() {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(10);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(0);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        Phrase p = new Phrase();
        p.add(new Chunk("Report Generated On:\n", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        p.add(new Chunk(dateFormat.format(now) + " | " + timeFormat.format(now), new Font(Font.FontFamily.HELVETICA, 10)));
        leftCell.addElement(p);
        headerTable.addCell(leftCell);

        return headerTable;
    }
}
