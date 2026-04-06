package com.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class IdCounterService {
    private IdCounterService() {}

    public static long peekNextValue(Connection conn, String counterKey, long observedMax) throws SQLException {
        long current = syncCurrentValue(conn, counterKey, observedMax, false);
        return current + 1L;
    }

    public static long nextValue(Connection conn, String counterKey, long observedMax) throws SQLException {
        long current = syncCurrentValue(conn, counterKey, observedMax, true);
        long next = current + 1L;

        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE TBL_ID_COUNTER SET NEXT_VAL = ? WHERE COUNTER_KEY = ?"
        )) {
            ps.setLong(1, next);
            ps.setString(2, counterKey);
            ps.executeUpdate();
        }
        return next;
    }

    private static long syncCurrentValue(Connection conn, String counterKey, long observedMax, boolean lockRow) throws SQLException {
        ensureRow(conn, counterKey, observedMax);

        String sql = "SELECT NEXT_VAL FROM TBL_ID_COUNTER WHERE COUNTER_KEY = ?" + (lockRow ? " FOR UPDATE" : "");
        long current;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counterKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Counter row missing for key: " + counterKey);
                current = rs.getLong(1);
            }
        }

        if (current < observedMax) {
            try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE TBL_ID_COUNTER SET NEXT_VAL = ? WHERE COUNTER_KEY = ?"
            )) {
                ps.setLong(1, observedMax);
                ps.setString(2, counterKey);
                ps.executeUpdate();
            }
            current = observedMax;
        }

        return current;
    }

    private static void ensureRow(Connection conn, String counterKey, long initialValue) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO TBL_ID_COUNTER (COUNTER_KEY, NEXT_VAL) VALUES (?, ?)"
        )) {
            ps.setString(1, counterKey);
            ps.setLong(2, Math.max(0L, initialValue));
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() != 1) { // ORA-00001 unique constraint
                throw e;
            }
        }
    }
}
