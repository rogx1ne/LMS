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

        PdfReportService.addBannerImage(doc);
        doc.add(PdfReportService.createPDFHeader());

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        Font head = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

        Paragraph p = new Paragraph(title, titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20);
        doc.add(p);

        PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
        pdfTable.setWidthPercentage(100);

        for (int c = 0; c < table.getColumnCount(); c++) {
            PdfPCell h = new PdfPCell(new Phrase(table.getColumnName(c), head));
            h.setHorizontalAlignment(Element.ALIGN_CENTER);
            h.setVerticalAlignment(Element.ALIGN_MIDDLE);
            h.setPadding(5);
            h.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            pdfTable.addCell(h);
        }

        for (int r = 0; r < table.getRowCount(); r++) {
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object value = table.getValueAt(r, c);
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : String.valueOf(value), normal));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfTable.addCell(cell);
            }
        }

        doc.add(pdfTable);
        doc.add(PdfReportService.createPDFFooter());
        doc.close();
    }

    public void generateOrderReceiptPdf(OrderHeader header, Seller seller, List<OrderDetail> details, Path outputPath) throws Exception {
        Document doc = new Document(PageSize.A4, 28, 28, 28, 28);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath.toFile()));
        doc.open();

        PdfReportService.addBannerImage(doc);
        doc.add(PdfReportService.createPDFHeader());

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font head = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("Library Procurement Order Receipt", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        doc.add(title);

        doc.add(new Paragraph("Order ID: " + header.getOrderId(), head));
        doc.add(new Paragraph("Order Date: " + header.getOrderDate(), normal));
        doc.add(new Paragraph("Supplier ID: " + seller.getSellerId(), normal));
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
            PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(i++), normal));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(c1);
            
            PdfPCell c2 = new PdfPCell(new Phrase(d.getBookTitle(), normal));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(c2);
            
            PdfPCell c3 = new PdfPCell(new Phrase(d.getAuthor(), normal));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(c3);
            
            PdfPCell c4 = new PdfPCell(new Phrase(d.getPublication(), normal));
            c4.setHorizontalAlignment(Element.ALIGN_CENTER);
            c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(c4);
            
            PdfPCell c5 = new PdfPCell(new Phrase(String.valueOf(d.getQuantity()), normal));
            c5.setHorizontalAlignment(Element.ALIGN_CENTER);
            c5.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(c5);
        }

        doc.add(table);
        doc.add(PdfReportService.createPDFFooter());
        doc.close();
    }

    private void addHead(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
