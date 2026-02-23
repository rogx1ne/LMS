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
        int seq = countByPrefix(conn, "SELECT COUNT(*) FROM TBL_STUDENT WHERE CARD_ID LIKE ?", prefix + "%") + 1;
        return prefix + String.format("%03d", seq);
    }

    public String nextReceiptNo(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "LR-" + String.format("%02d", yy);
        int seq = countByPrefix(conn, "SELECT COUNT(*) FROM TBL_STUDENT WHERE RECEIPT_NO LIKE ?", prefix + "%") + 1;
        return prefix + String.format("%03d", seq);
    }

    private int countByPrefix(Connection conn, String sql, String likeValue) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, likeValue);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
