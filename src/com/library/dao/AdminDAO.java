package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.AuditLogEntry;
import com.library.model.User;
import com.library.service.AuditLogger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {
    public static final String TABLE_STUDENTS = "Students";
    public static final String TABLE_BOOKS = "Books";
    public static final String TABLE_ORDERS = "Orders";

    public List<String> getSupportedTableNames() {
        List<String> names = new ArrayList<>();
        names.add(TABLE_STUDENTS);
        names.add(TABLE_BOOKS);
        names.add(TABLE_ORDERS);
        return names;
    }

    public boolean isAdminSchemaAvailable() {
        String auditSql = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'TBL_AUDIT_LOG'";
        String statusSql = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'TBL_CREDENTIALS' AND COLUMN_NAME = 'STATUS'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps1 = conn.prepareStatement(auditSql);
                 ResultSet rs1 = ps1.executeQuery();
                 PreparedStatement ps2 = conn.prepareStatement(statusSql);
                 ResultSet rs2 = ps2.executeQuery()) {
                return rs1.next() && rs1.getInt(1) == 1 && rs2.next() && rs2.getInt(1) == 1;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public String nextUserId(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(USER_ID, 2))), 0) AS MAX_ID " +
                     "FROM TBL_CREDENTIALS WHERE REGEXP_LIKE(USER_ID, '^U[0-9]{3}$')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int next = rs.getInt("MAX_ID") + 1;
            return String.format("U%03d", next);
        }
    }

    public String peekNextUserId() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "U001";
            return nextUserId(conn);
        } catch (SQLException e) {
            return "U001";
        }
    }

    public User createUser(String name, String password, String email, String phone, String performedBy) throws SQLException {
        String sql =
            "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PSWD, EMAIL, PHNO, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String userId = nextUserId(conn);
                long phno = Long.parseLong(phone.trim());
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, userId);
                    ps.setString(2, name.trim());
                    ps.setString(3, password.trim());
                    ps.setString(4, email.trim());
                    ps.setLong(5, phno);
                    ps.executeUpdate();
                }
                AuditLogger.logAction(conn, performedBy, "Admin", "Created user " + userId);
                conn.commit();
                return new User(userId, name.trim(), password.trim(), email.trim(), phno, "ACTIVE");
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException(e.getMessage(), e);
            } finally {
                conn.setAutoCommit(oldAuto);
            }
        }
    }

    public boolean deactivateUser(String userId, String performedBy) {
        String sql = "UPDATE TBL_CREDENTIALS SET STATUS = 'INACTIVE' WHERE USER_ID = ? AND STATUS = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    AuditLogger.logAction(conn, performedBy, "Admin", "Deactivated user " + userId);
                }
                conn.commit();
                conn.setAutoCommit(oldAuto);
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(oldAuto);
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> out = new ArrayList<>();
        String sql = "SELECT USER_ID, NAME, PSWD, EMAIL, PHNO, NVL(STATUS, 'ACTIVE') AS STATUS " +
                     "FROM TBL_CREDENTIALS ORDER BY USER_ID";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return out;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new User(
                    rs.getString("USER_ID"),
                    rs.getString("NAME"),
                    rs.getString("PSWD"),
                    rs.getString("EMAIL"),
                    rs.getLong("PHNO"),
                    rs.getString("STATUS")
                ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<AuditLogEntry> getAuditLogs(String userId, String module, Date fromDate, Date toDate) {
        List<AuditLogEntry> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT LOG_ID, USER_ID, MODULE, ACTION_DESCRIPTION, LOG_TS FROM TBL_AUDIT_LOG WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (userId != null && !userId.trim().isEmpty()) {
            sql.append(" AND UPPER(USER_ID) LIKE ?");
            params.add("%" + userId.trim().toUpperCase() + "%");
        }
        if (module != null && !module.trim().isEmpty() && !"All".equalsIgnoreCase(module.trim())) {
            sql.append(" AND UPPER(MODULE) = ?");
            params.add(module.trim().toUpperCase());
        }
        if (fromDate != null) {
            sql.append(" AND LOG_TS >= ?");
            params.add(Timestamp.valueOf(fromDate.toLocalDate().atStartOfDay()));
        }
        if (toDate != null) {
            sql.append(" AND LOG_TS < ?");
            LocalDate end = toDate.toLocalDate().plusDays(1);
            params.add(Timestamp.valueOf(end.atStartOfDay()));
        }
        sql.append(" ORDER BY LOG_TS DESC, LOG_ID DESC");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return out;
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new AuditLogEntry(
                            rs.getLong("LOG_ID"),
                            rs.getString("USER_ID"),
                            rs.getString("MODULE"),
                            rs.getString("ACTION_DESCRIPTION"),
                            rs.getTimestamp("LOG_TS")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<Map<String, Object>> fetchTableData(String uiTableName) throws SQLException {
        String tableName = resolvePhysicalTable(uiTableName);
        String sql = "SELECT * FROM " + tableName;
        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return out;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    out.add(row);
                }
            }
        }
        return out;
    }

    public int importRows(String uiTableName, List<Map<String, String>> rows, String performedBy) throws SQLException {
        if (rows == null || rows.isEmpty()) return 0;
        String tableName = resolvePhysicalTable(uiTableName);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int inserted;
                if ("TBL_STUDENT".equals(tableName)) {
                    inserted = importStudents(conn, rows);
                } else if ("TBL_BOOK_INFORMATION".equals(tableName)) {
                    inserted = importBooks(conn, rows);
                } else if ("TBL_ORDER_HEADER".equals(tableName)) {
                    inserted = importOrders(conn, rows);
                } else {
                    throw new SQLException("Unsupported table: " + tableName);
                }
                AuditLogger.logAction(conn, performedBy, "Admin", "Imported " + inserted + " rows into " + tableName);
                conn.commit();
                conn.setAutoCommit(oldAuto);
                return inserted;
            } catch (Exception e) {
                conn.rollback();
                conn.setAutoCommit(oldAuto);
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    private int importStudents(Connection conn, List<Map<String, String>> rows) throws SQLException {
        String sql =
            "INSERT INTO TBL_STUDENT (CARD_ID, ROLL, NAME, PH_NO, ADDR, COURSE, ACAD_SESSION, " +
            "RECEIPT_NO, ISSUED_BY, ISSUE_DATE, BOOK_LIMIT, FEE, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, String> row : rows) {
                ps.setString(1, req(row, "CARD_ID"));
                ps.setInt(2, parseInt(row, "ROLL"));
                ps.setString(3, req(row, "NAME"));
                ps.setLong(4, parseLong(row, "PH_NO"));
                ps.setString(5, req(row, "ADDR"));
                ps.setString(6, req(row, "COURSE"));
                ps.setString(7, req(row, "ACAD_SESSION"));
                ps.setString(8, req(row, "RECEIPT_NO"));
                ps.setString(9, req(row, "ISSUED_BY"));
                ps.setDate(10, parseDate(row, "ISSUE_DATE"));
                ps.setInt(11, parseInt(row, "BOOK_LIMIT"));
                ps.setBigDecimal(12, parseDecimal(row, "FEE"));
                ps.setString(13, req(row, "STATUS"));
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            return counts.length;
        }
    }

    private int importBooks(Connection conn, List<Map<String, String>> rows) throws SQLException {
        String mergeCatalogSql =
            "MERGE INTO TBL_BOOK_CATALOG c " +
            "USING (SELECT ? AS AUTHOR_NAME, ? AS BK_TITLE, ? AS EDITION FROM dual) src " +
            "ON (c.AUTHOR_NAME = src.AUTHOR_NAME AND c.BK_TITLE = src.BK_TITLE AND c.EDITION = src.EDITION) " +
            "WHEN NOT MATCHED THEN INSERT (AUTHOR_NAME, BK_TITLE, EDITION) VALUES (src.AUTHOR_NAME, src.BK_TITLE, src.EDITION)";
        String insertSql =
            "INSERT INTO TBL_BOOK_INFORMATION (ACCESS_NO, AUTHOR_NAME, BK_TITLE, VOLUME, EDITION, PUBLISHER, PUB_PLACE, PUB_YEAR, " +
            "PAGES, SOURCE, CLASS_NO, BOOK_NO, U_PRICE, B_NO, B_DATE, WITHDRAWN, REMARKS, STATUS, CIRC_STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int count = 0;
        try (PreparedStatement mergePs = conn.prepareStatement(mergeCatalogSql);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (Map<String, String> row : rows) {
                String author = req(row, "AUTHOR_NAME");
                String title = req(row, "BK_TITLE");
                int edition = parseInt(row, "EDITION");

                mergePs.setString(1, author);
                mergePs.setString(2, title);
                mergePs.setInt(3, edition);
                mergePs.executeUpdate();

                ps.setString(1, req(row, "ACCESS_NO"));
                ps.setString(2, author);
                ps.setString(3, title);
                Integer volume = parseNullableInt(row, "VOLUME");
                if (volume == null) ps.setNull(4, java.sql.Types.NUMERIC); else ps.setInt(4, volume);
                ps.setInt(5, edition);
                ps.setString(6, req(row, "PUBLISHER"));
                ps.setString(7, req(row, "PUB_PLACE"));
                ps.setInt(8, parseInt(row, "PUB_YEAR"));
                ps.setInt(9, parseInt(row, "PAGES"));
                ps.setString(10, req(row, "SOURCE"));
                ps.setString(11, opt(row, "CLASS_NO"));
                ps.setString(12, req(row, "BOOK_NO"));
                ps.setBigDecimal(13, parseDecimal(row, "U_PRICE"));
                ps.setString(14, opt(row, "B_NO"));
                ps.setDate(15, parseNullableDate(row, "B_DATE"));
                ps.setDate(16, parseNullableDate(row, "WITHDRAWN"));
                ps.setString(17, opt(row, "REMARKS"));
                ps.setString(18, req(row, "STATUS"));
                ps.setString(19, opt(row, "CIRC_STATUS").isEmpty() ? "AVAILABLE" : opt(row, "CIRC_STATUS"));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
        }
        return count;
    }

    private int importOrders(Connection conn, List<Map<String, String>> rows) throws SQLException {
        String sql = "INSERT INTO TBL_ORDER_HEADER (ORDER_ID, S_ID, ORDER_DATE) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, String> row : rows) {
                ps.setString(1, req(row, "ORDER_ID"));
                ps.setString(2, req(row, "S_ID"));
                ps.setDate(3, parseDate(row, "ORDER_DATE"));
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            return counts.length;
        }
    }

    private String resolvePhysicalTable(String uiTableName) throws SQLException {
        if (TABLE_STUDENTS.equalsIgnoreCase(uiTableName)) return "TBL_STUDENT";
        if (TABLE_BOOKS.equalsIgnoreCase(uiTableName)) return "TBL_BOOK_INFORMATION";
        if (TABLE_ORDERS.equalsIgnoreCase(uiTableName)) return "TBL_ORDER_HEADER";
        throw new SQLException("Unsupported table selection: " + uiTableName);
    }

    private String req(Map<String, String> row, String key) throws SQLException {
        String value = findCaseInsensitive(row, key);
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Missing required value for " + key);
        }
        return value.trim();
    }

    private String opt(Map<String, String> row, String key) {
        String value = findCaseInsensitive(row, key);
        return value == null ? "" : value.trim();
    }

    private int parseInt(Map<String, String> row, String key) throws SQLException {
        try {
            return Integer.parseInt(req(row, key));
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid integer for " + key);
        }
    }

    private Integer parseNullableInt(Map<String, String> row, String key) throws SQLException {
        String value = opt(row, key);
        if (value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid integer for " + key);
        }
    }

    private long parseLong(Map<String, String> row, String key) throws SQLException {
        try {
            return Long.parseLong(req(row, key));
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid number for " + key);
        }
    }

    private BigDecimal parseDecimal(Map<String, String> row, String key) throws SQLException {
        try {
            return new BigDecimal(req(row, key));
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid decimal for " + key);
        }
    }

    private Date parseDate(Map<String, String> row, String key) throws SQLException {
        try {
            return Date.valueOf(req(row, key));
        } catch (Exception e) {
            throw new SQLException("Invalid date for " + key + ". Expected yyyy-MM-dd");
        }
    }

    private Date parseNullableDate(Map<String, String> row, String key) throws SQLException {
        String value = opt(row, key);
        if (value.isEmpty()) return null;
        try {
            return Date.valueOf(value);
        } catch (Exception e) {
            throw new SQLException("Invalid date for " + key + ". Expected yyyy-MM-dd");
        }
    }

    private String findCaseInsensitive(Map<String, String> row, String key) {
        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().trim().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
