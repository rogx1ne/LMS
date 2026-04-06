package com.library.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ExcelService {
    private static final DataFormatter FORMATTER = new DataFormatter();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public void exportRows(String sheetName, List<Map<String, Object>> rows, Path outputFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName == null || sheetName.trim().isEmpty() ? "Data" : sheetName.trim());

            List<String> headers = resolveHeaders(rows);
            writeHeader(sheet, headers, workbook);
            writeRows(sheet, headers, rows);
            autoSize(sheet, headers.size());

            try (OutputStream out = Files.newOutputStream(outputFile)) {
                workbook.write(out);
            }
        }
    }

    public List<Map<String, String>> readRows(Path inputFile) throws IOException {
        try (InputStream in = Files.newInputStream(inputFile);
             Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return new ArrayList<>();

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(readCellText(headerRow.getCell(i)).trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> data = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c);
                    if (header == null || header.trim().isEmpty()) continue;
                    String value = readCellText(row.getCell(c)).trim();
                    if (!value.isEmpty()) hasValue = true;
                    data.put(header.trim(), value);
                }
                if (hasValue) rows.add(data);
            }
            return rows;
        }
    }

    private void writeHeader(Sheet sheet, List<String> headers, Workbook workbook) {
        Row header = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
        }
    }

    private void writeRows(Sheet sheet, List<String> headers, List<Map<String, Object>> rows) {
        int rowIndex = 1;
        for (Map<String, Object> rowData : rows) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                String key = headers.get(i);
                Object value = rowData.get(key);
                row.createCell(i).setCellValue(toExcelText(value));
            }
        }
    }

    private List<String> resolveHeaders(List<Map<String, Object>> rows) {
        List<String> headers = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return headers;
        headers.addAll(rows.get(0).keySet());
        return headers;
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String toExcelText(Object value) {
        if (value == null) return "";
        if (value instanceof Timestamp) return TS_FORMAT.format((Timestamp) value);
        if (value instanceof Date) return DATE_FORMAT.format((Date) value);
        return String.valueOf(value);
    }

    private String readCellText(Cell cell) {
        if (cell == null) return "";
        return FORMATTER.formatCellValue(cell);
    }

    /**
     * Exports multiple tables to a ZIP file, each table as a separate Excel file.
     * @param tablesData Map of table name to list of rows
     * @param outputZipFile Path to output ZIP file
     * @param dateStamp Date stamp to include in Excel filenames
     * @throws IOException if export fails
     */
    public void exportMultipleTablesToZip(Map<String, List<Map<String, Object>>> tablesData, 
                                           Path outputZipFile, String dateStamp) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZipFile))) {
            for (Map.Entry<String, List<Map<String, Object>>> entry : tablesData.entrySet()) {
                String tableName = entry.getKey();
                List<Map<String, Object>> rows = entry.getValue();
                
                // Create Excel file in memory
                byte[] excelBytes = createExcelBytes(tableName, rows);
                
                // Add to ZIP
                String excelFileName = tableName + "_" + dateStamp + ".xlsx";
                ZipEntry zipEntry = new ZipEntry(excelFileName);
                zos.putNextEntry(zipEntry);
                zos.write(excelBytes);
                zos.closeEntry();
            }
        }
    }

    /**
     * Imports multiple tables from a ZIP file containing Excel files.
     * @param inputZipFile Path to input ZIP file
     * @return Map of table name (extracted from filename) to list of row data
     * @throws IOException if import fails
     */
    public Map<String, List<Map<String, String>>> importFromZip(Path inputZipFile) throws IOException {
        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(inputZipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith(".xlsx")) {
                    zis.closeEntry();
                    continue;
                }
                
                // Extract table name from filename (e.g., "Students_2026-04-05.xlsx" -> "Students")
                String fileName = entry.getName();
                int lastSlash = fileName.lastIndexOf('/');
                if (lastSlash >= 0) fileName = fileName.substring(lastSlash + 1);
                int underscore = fileName.indexOf('_');
                String tableName = underscore > 0 ? fileName.substring(0, underscore) : 
                                   fileName.replace(".xlsx", "").replace(".XLSX", "");
                
                // Read Excel data from ZIP entry
                List<Map<String, String>> rows = readRowsFromStream(zis);
                result.put(tableName, rows);
                
                zis.closeEntry();
            }
        }
        
        return result;
    }

    /**
     * Creates an Excel file as byte array in memory.
     */
    private byte[] createExcelBytes(String sheetName, List<Map<String, Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName == null || sheetName.trim().isEmpty() ? "Data" : sheetName.trim());
            
            List<String> headers = resolveHeaders(rows);
            writeHeader(sheet, headers, workbook);
            writeRows(sheet, headers, rows);
            autoSize(sheet, headers.size());
            
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Reads Excel rows from an input stream (used for ZIP entries).
     */
    private List<Map<String, String>> readRowsFromStream(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return new ArrayList<>();

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(readCellText(headerRow.getCell(i)).trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> data = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c);
                    if (header == null || header.trim().isEmpty()) continue;
                    String value = readCellText(row.getCell(c)).trim();
                    if (!value.isEmpty()) hasValue = true;
                    data.put(header.trim(), value);
                }
                if (hasValue) rows.add(data);
            }
            return rows;
        }
    }
}
