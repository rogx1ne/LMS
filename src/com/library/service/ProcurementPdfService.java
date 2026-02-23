package com.library.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.library.model.OrderDetail;
import com.library.model.OrderHeader;
import com.library.model.Seller;

import javax.swing.JTable;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProcurementPdfService {

    public void exportTable(JTable table, String title, Path outputPath) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath.toFile()));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        Font head = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

        Paragraph p = new Paragraph(title, titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(6);
        doc.add(p);

        doc.add(new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), normal));
        doc.add(new Paragraph(" "));

        PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
        pdfTable.setWidthPercentage(100);

        for (int c = 0; c < table.getColumnCount(); c++) {
            PdfPCell h = new PdfPCell(new Phrase(table.getColumnName(c), head));
            h.setHorizontalAlignment(Element.ALIGN_CENTER);
            h.setPadding(5);
            pdfTable.addCell(h);
        }

        for (int r = 0; r < table.getRowCount(); r++) {
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object value = table.getValueAt(r, c);
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : String.valueOf(value), normal));
                cell.setPadding(4);
                pdfTable.addCell(cell);
            }
        }

        doc.add(pdfTable);
        doc.close();
    }

    public void generateOrderReceiptPdf(OrderHeader header, Seller seller, List<OrderDetail> details, Path outputPath) throws Exception {
        Document doc = new Document(PageSize.A4, 28, 28, 28, 28);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath.toFile()));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font head = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Library Procurement Order Receipt", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        doc.add(title);

        doc.add(new Paragraph("Order ID: " + header.getOrderId(), head));
        doc.add(new Paragraph("Order Date: " + header.getOrderDate(), normal));
        doc.add(new Paragraph("Seller ID: " + seller.getSellerId(), normal));
        doc.add(new Paragraph("Company: " + seller.getCompanyName(), normal));
        doc.add(new Paragraph("Company Mail: " + seller.getCompanyMail(), normal));
        doc.add(new Paragraph("Contact Person: " + seller.getContactPerson() + " (" + seller.getContactPersonMail() + ")", normal));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 35, 20, 20, 15});

        addHead(table, "S.No", head);
        addHead(table, "Book Title", head);
        addHead(table, "Author", head);
        addHead(table, "Publication", head);
        addHead(table, "Qty", head);

        int i = 1;
        for (OrderDetail d : details) {
            table.addCell(new Phrase(String.valueOf(i++), normal));
            table.addCell(new Phrase(d.getBookTitle(), normal));
            table.addCell(new Phrase(d.getAuthor(), normal));
            table.addCell(new Phrase(d.getPublication(), normal));
            table.addCell(new Phrase(String.valueOf(d.getQuantity()), normal));
        }

        doc.add(table);
        doc.close();
    }

    private void addHead(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
