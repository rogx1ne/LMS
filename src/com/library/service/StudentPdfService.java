package com.library.service;

import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StudentPdfService {
    private static final Font TITLE = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font H = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    private static final Font N = new Font(Font.FontFamily.HELVETICA, 11);
    private static final Font S = new Font(Font.FontFamily.HELVETICA, 9);

    public void generateReceiptPdf(Student s, Path outPath) throws Exception {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(outPath.toFile()));
        doc.open();

        doc.add(center(title("Library Registration Receipt")));
        doc.add(center(new Paragraph("Receipt No: " + s.getReceiptNo(), H)));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{30, 70});

        addRow(table, "Card ID", s.getCardId());
        addRow(table, "Name", s.getName());
        addRow(table, "Course", s.getCourse());
        addRow(table, "Session", s.getSession());
        addRow(table, "Roll No", String.valueOf(s.getRoll()));
        addRow(table, "Contact", String.valueOf(s.getPhone()));
        addRow(table, "Book Limit", String.valueOf(s.getBookLimit()));
        addRow(table, "Fee", String.valueOf(s.getFee()));
        addRow(table, "Issued By", s.getIssuedBy());
        addRow(table, "Issue Date", formatDate(s.getIssueDate()));
        addRow(table, "Status", s.getStatus());
        doc.add(table);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Generated on: " + new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()), S));

        doc.close();
    }

    public void generateLibraryCardPdf(Student s, Path outPath) throws Exception {
        Document doc = new Document(new com.itextpdf.text.Rectangle(420, 260), 18, 18, 18, 18);
        PdfWriter.getInstance(doc, new FileOutputStream(outPath.toFile()));
        doc.open();

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{20, 80});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(0);
        try {
            Image logo = Image.getInstance("lib/icons/clglogo.png");
            logo.scaleToFit(40, 40);
            logoCell.addElement(logo);
        } catch (Exception ignored) {
            logoCell.addElement(new Phrase(" "));
        }
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(0);
        Paragraph p = new Paragraph("LIBRARY CARD", TITLE);
        p.setAlignment(Element.ALIGN_LEFT);
        titleCell.addElement(p);
        titleCell.addElement(new Paragraph("Card ID: " + s.getCardId(), H));
        header.addCell(titleCell);

        doc.add(header);
        doc.add(new Paragraph(" "));

        PdfPTable body = new PdfPTable(2);
        body.setWidthPercentage(100);
        body.setWidths(new float[]{30, 70});
        addRow(body, "Name", s.getName());
        addRow(body, "Course", s.getCourse());
        addRow(body, "Session", s.getSession());
        addRow(body, "Roll No", String.valueOf(s.getRoll()));
        addRow(body, "Contact", String.valueOf(s.getPhone()));
        addRow(body, "Book Limit", String.valueOf(s.getBookLimit()));
        addRow(body, "Issue Date", formatDate(s.getIssueDate()));
        doc.add(body);

        doc.close();
    }

    public void generateBorrowHistoryPdf(Student s, List<BorrowRecord> history, Path outPath) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(outPath.toFile()));
        doc.open();

        doc.add(center(title("Borrowing History")));
        doc.add(center(new Paragraph(s.getCardId() + " - " + s.getName(), H)));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        addHeader(table, "Access No");
        addHeader(table, "Title");
        addHeader(table, "Author");
        addHeader(table, "Issued");
        addHeader(table, "Due");
        addHeader(table, "Returned");
        addHeader(table, "Fine");

        for (BorrowRecord r : history) {
            table.addCell(cell(r.getAccessNo()));
            table.addCell(cell(r.getBookTitle()));
            table.addCell(cell(r.getAuthorName()));
            table.addCell(cell(formatDate(r.getIssueDate())));
            table.addCell(cell(formatDate(r.getDueDate())));
            table.addCell(cell(formatDate(r.getReturnDate())));
            table.addCell(cell(r.getFineAmount() == null ? "" : String.valueOf(r.getFineAmount())));
        }

        doc.add(table);
        doc.close();
    }

    private static Paragraph title(String text) {
        Paragraph p = new Paragraph(text, TITLE);
        p.setSpacingAfter(4);
        return p;
    }

    private static Paragraph center(Paragraph p) {
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private static void addRow(PdfPTable table, String k, String v) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, H));
        c1.setBackgroundColor(new BaseColor(245, 248, 250));
        c1.setPadding(6);
        PdfPCell c2 = new PdfPCell(new Phrase(v == null ? "" : v, N));
        c2.setPadding(6);
        table.addCell(c1);
        table.addCell(c2);
    }

    private static void addHeader(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, H));
        c.setBackgroundColor(new BaseColor(220, 230, 240));
        c.setPadding(6);
        table.addCell(c);
    }

    private static PdfPCell cell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, S));
        c.setPadding(5);
        return c;
    }

    private static String formatDate(java.util.Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }
}
