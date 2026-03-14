package com.library.assistant.reports;

import com.library.database.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AssistantReportDAO {

    public List<Map<String, Object>> fetchLowStock(int threshold) throws SQLException {
        String sql =
            "SELECT BK_TITLE, AUTHOR_NAME, EDITION, PUBLISHER || ', ' || PUB_PLACE AS PUBLICATION, COUNT(*) AS QTY " +
            "FROM TBL_BOOK_INFORMATION " +
            "WHERE STATUS = 'ACTIVE' " +
            "GROUP BY BK_TITLE, AUTHOR_NAME, EDITION, PUBLISHER, PUB_PLACE " +
            "HAVING COUNT(*) < ? " +
            "ORDER BY BK_TITLE, AUTHOR_NAME, EDITION";

        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("DB connection is null.");
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("Title", rs.getString("BK_TITLE"));
                    row.put("Author", rs.getString("AUTHOR_NAME"));
                    row.put("Edition", rs.getInt("EDITION"));
                    row.put("Publication", rs.getString("PUBLICATION"));
                    row.put("Quantity", rs.getInt("QTY"));
                    row.put("Threshold", threshold);
                    out.add(row);
                }
            }
        }
        return out;
    }

    public List<Map<String, Object>> fetchOverdueIssues() throws SQLException {
        String sql =
            "SELECT i.ISSUE_ID, i.CARD_ID, s.NAME AS STUDENT_NAME, i.ACCESSION_NO, " +
            "b.BK_TITLE, b.AUTHOR_NAME, i.ISSUE_DATE, i.DUE_DATE, (TRUNC(SYSDATE) - i.DUE_DATE) AS DAYS_OVERDUE, " +
            "i.ISSUED_BY " +
            "FROM TBL_ISSUE i " +
            "JOIN TBL_STUDENT s ON s.CARD_ID = i.CARD_ID " +
            "JOIN TBL_BOOK_INFORMATION b ON b.ACCESS_NO = i.ACCESSION_NO " +
            "WHERE i.STATUS = 'ISSUED' AND i.DUE_DATE < TRUNC(SYSDATE) " +
            "ORDER BY i.DUE_DATE ASC, i.ISSUE_ID ASC";

        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("DB connection is null.");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("Issue ID", rs.getString("ISSUE_ID"));
                    row.put("Card ID", rs.getString("CARD_ID"));
                    row.put("Student", rs.getString("STUDENT_NAME"));
                    row.put("Accession No", rs.getString("ACCESSION_NO"));
                    row.put("Title", rs.getString("BK_TITLE"));
                    row.put("Author", rs.getString("AUTHOR_NAME"));
                    Date issueDate = rs.getDate("ISSUE_DATE");
                    Date dueDate = rs.getDate("DUE_DATE");
                    row.put("Issue Date", issueDate);
                    row.put("Due Date", dueDate);
                    row.put("Days Overdue", rs.getInt("DAYS_OVERDUE"));
                    row.put("Issued By", rs.getString("ISSUED_BY"));
                    out.add(row);
                }
            }
        }
        return out;
    }
}

