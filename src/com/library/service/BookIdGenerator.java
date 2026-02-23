package com.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class BookIdGenerator {

    public String nextAccessionNo(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCESS_NO, '[0-9]+'))), 0) AS MAX_NUM FROM TBL_BOOK_INFORMATION";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int next = rs.getInt("MAX_NUM") + 1;
            return String.format("A%05d", next);
        }
    }
}
