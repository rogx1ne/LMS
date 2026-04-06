package com.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;

public final class StudentIdGenerator {

    public String nextCardId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "PPU-" + String.format("%02d", yy);
        long seq = IdCounterService.nextValue(conn, "STUDENT_CARD_" + yy, maxSuffixByPrefix(conn, "CARD_ID", prefix + "%", 7));
        return prefix + String.format("%03d", seq);
    }

    public String nextReceiptNo(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "LR-" + String.format("%02d", yy);
        long seq = IdCounterService.nextValue(conn, "STUDENT_RECEIPT_" + yy, maxSuffixByPrefix(conn, "RECEIPT_NO", prefix + "%", 6));
        return prefix + String.format("%03d", seq);
    }

    private long maxSuffixByPrefix(Connection conn, String columnName, String likeValue, int startPosition) throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(" + columnName + ", ?))), 0) " +
            "FROM TBL_STUDENT WHERE " + columnName + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, startPosition);
            ps.setString(2, likeValue);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
