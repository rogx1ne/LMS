package com.library.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.JTable;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BookPdfService {

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

        Paragraph generated = new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), normal);
        generated.setSpacingAfter(10);
        doc.add(generated);

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
}
