package com.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;

public class ProcurementIdGenerator {

    public String nextSellerId(Connection conn) throws SQLException {
        long next = IdCounterService.nextValue(conn, "SELLER", maxNumericSuffix(conn, "TBL_SELLER", "S_ID", "^SID[0-9]{7}$", 4));
        return String.format("SID%07d", next);
    }

    public String peekNextSellerId(Connection conn) throws SQLException {
        long next = IdCounterService.peekNextValue(conn, "SELLER", maxNumericSuffix(conn, "TBL_SELLER", "S_ID", "^SID[0-9]{7}$", 4));
        return String.format("SID%07d", next);
    }

    public String nextOrderId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "ORD-" + String.format("%02d", yy);
        long next = IdCounterService.nextValue(conn, "ORDER_" + yy, maxNumericLike(conn, "TBL_ORDER_HEADER", "ORDER_ID", prefix + "%", 7));
        return prefix + String.format("%04d", next);
    }

    public String peekNextOrderId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "ORD-" + String.format("%02d", yy);
        long next = IdCounterService.peekNextValue(conn, "ORDER_" + yy, maxNumericLike(conn, "TBL_ORDER_HEADER", "ORDER_ID", prefix + "%", 7));
        return prefix + String.format("%04d", next);
    }

    private long maxNumericSuffix(Connection conn, String tableName, String columnName, String regex, int startPosition) throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(" + columnName + ", ?))), 0) " +
            "FROM " + tableName + " WHERE REGEXP_LIKE(" + columnName + ", ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, startPosition);
            ps.setString(2, regex);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long maxNumericLike(Connection conn, String tableName, String columnName, String likePattern, int startPosition) throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(" + columnName + ", ?))), 0) " +
            "FROM " + tableName + " WHERE " + columnName + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, startPosition);
            ps.setString(2, likePattern);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
