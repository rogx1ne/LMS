package com.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;

public class ProcurementIdGenerator {

    public String nextSellerId(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TBL_SELLER";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int next = rs.getInt(1) + 1;
            return String.format("SID%07d", next);
        }
    }

    public String nextOrderId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "ORD-" + String.format("%02d", yy);
        String sql = "SELECT COUNT(*) FROM TBL_ORDER_HEADER WHERE ORDER_ID LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int next = rs.getInt(1) + 1;
                return prefix + String.format("%04d", next);
            }
        }
    }
}
