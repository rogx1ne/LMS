package com.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class BookIdGenerator {
    private static final String COUNTER_KEY = "ACCESSION";

    public String nextAccessionNo(Connection conn) throws SQLException {
        long next = IdCounterService.nextValue(conn, COUNTER_KEY, observedMax(conn));
        return String.format("%06d", next);
    }

    public String peekNextAccessionNo(Connection conn) throws SQLException {
        long next = IdCounterService.peekNextValue(conn, COUNTER_KEY, observedMax(conn));
        return String.format("%06d", next);
    }

    private long observedMax(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(ACCESS_NO)), 0) AS MAX_NUM FROM TBL_BOOK_INFORMATION";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong("MAX_NUM");
        }
    }
}
