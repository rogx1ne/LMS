package com.library.service;

import com.library.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AuditLogger {
    private AuditLogger() {}

    public static void logAction(String userId, String module, String actionDescription) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            insertLog(conn, userId, module, actionDescription);
        } catch (SQLException ignored) {
        }
    }

    public static void logAction(Connection conn, String userId, String module, String actionDescription) throws SQLException {
        insertLog(conn, userId, module, actionDescription);
    }

    private static void insertLog(Connection conn, String userId, String module, String actionDescription) throws SQLException {
        String sql =
            "INSERT INTO TBL_AUDIT_LOG (USER_ID, MODULE, ACTION_DESCRIPTION, LOG_TS) " +
            "VALUES (?, ?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trim(userId, 20));
            ps.setString(2, trim(module, 30));
            ps.setString(3, trim(actionDescription, 400));
            ps.executeUpdate();
        }
    }

    private static String trim(String value, int size) {
        String safe = value == null ? "" : value.trim();
        if (safe.length() <= size) return safe;
        return safe.substring(0, size);
    }
}
