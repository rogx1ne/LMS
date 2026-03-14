package com.library.assistant.reports;

import com.library.service.ExcelService;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AssistantReportService {
    private final AssistantReportDAO dao = new AssistantReportDAO();
    private final ExcelService excel = new ExcelService();

    public List<Map<String, Object>> lowStock(int threshold) throws SQLException {
        int t = threshold <= 0 ? 2 : threshold;
        return dao.fetchLowStock(t);
    }

    public List<Map<String, Object>> overdueIssues() throws SQLException {
        return dao.fetchOverdueIssues();
    }

    public void exportToExcel(String sheetName, List<Map<String, Object>> rows, Path outputFile) throws IOException {
        excel.exportRows(sheetName, rows, outputFile);
    }
}

