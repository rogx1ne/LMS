package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.AuditLogEntry;
import com.library.model.User;
import com.library.service.AuditLogger;
import com.library.service.PasswordHasher;

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
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(TRIM(USER_ID), 2))), 0) AS MAX_ID " +
                     "FROM TBL_CREDENTIALS WHERE REGEXP_LIKE(TRIM(USER_ID), '^U[0-9]{4}$')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int next = rs.getInt("MAX_ID") + 1;
            return String.format("U%04d", next);
        }
    }

    public String peekNextUserId() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "U0001";
            return nextUserId(conn);
        } catch (SQLException e) {
            return "U0001";
        }
    }

    public User createUser(String name, String password, String email, String phone, String performedBy) throws SQLException {
        String sql =
            "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PSWD, EMAIL, PHNO, ROLE, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, 'LIBRARIAN', 'ACTIVE')";
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
                    ps.setString(3, PasswordHasher.hashPassword(password));
                    ps.setString(4, email.trim());
                    ps.setLong(5, phno);
                    ps.executeUpdate();
                }
                AuditLogger.logAction(conn, performedBy, "Admin", "Created user " + userId);
                conn.commit();
                return new User(userId, name.trim(), "[PROTECTED]", email.trim(), phno, "ACTIVE");
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
        String sql = "UPDATE TBL_CREDENTIALS SET STATUS = 'INACTIVE' WHERE TRIM(USER_ID) = ? AND STATUS = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId.trim());
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
                    rs.getString("USER_ID").trim(),
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
        sql.append(" ORDER BY LOG_TS ASC, LOG_ID ASC");

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
        String tableName = resolveExportTarget(uiTableName);
        String sql = exportSqlFor(tableName);
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
        String tableName = resolveImportTarget(uiTableName);

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
            "PAGES, SOURCE, CLASS_NO, BOOK_NO, U_PRICE, B_NO, B_DATE, BK_SUBJECT, BK_COURSE, BK_YEAR, BK_TYPE, WITHDRAWN, REMARKS, STATUS, CIRC_STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                ps.setString(16, opt(row, "BK_SUBJECT"));
                ps.setString(17, opt(row, "BK_COURSE"));
                ps.setString(18, opt(row, "BK_YEAR"));
                ps.setString(19, opt(row, "BK_TYPE").isEmpty() ? "BOOK" : opt(row, "BK_TYPE"));
                ps.setDate(20, parseNullableDate(row, "WITHDRAWN"));
                ps.setString(21, opt(row, "REMARKS"));
                ps.setString(22, req(row, "STATUS"));
                ps.setString(23, opt(row, "CIRC_STATUS").isEmpty() ? "AVAILABLE" : opt(row, "CIRC_STATUS"));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
        }
        return count;
    }

    private int importOrders(Connection conn, List<Map<String, String>> rows) throws SQLException {
        String headerSql = "INSERT INTO TBL_ORDER_HEADER (ORDER_ID, S_ID, ORDER_DATE) VALUES (?, ?, ?)";
        String detailSql = "INSERT INTO TBL_ORDER_DETAILS (ORDER_ID, BOOK_TITLE, AUTHOR, PUBLICATION, QUANTITY) VALUES (?, ?, ?, ?, ?)";
        Map<String, Map<String, String>> headers = new LinkedHashMap<>();

        for (Map<String, String> row : rows) {
            String orderId = req(row, "ORDER_ID");
            Map<String, String> existing = headers.get(orderId);
            if (existing == null) {
                headers.put(orderId, row);
                continue;
            }
            if (!req(existing, "S_ID").equals(req(row, "S_ID")) || !req(existing, "ORDER_DATE").equals(req(row, "ORDER_DATE"))) {
                throw new SQLException("Conflicting header values found for order " + orderId);
            }
        }

        try (PreparedStatement headerPs = conn.prepareStatement(headerSql);
             PreparedStatement detailPs = conn.prepareStatement(detailSql)) {
            for (Map<String, String> header : headers.values()) {
                headerPs.setString(1, req(header, "ORDER_ID"));
                headerPs.setString(2, req(header, "S_ID"));
                headerPs.setDate(3, parseDate(header, "ORDER_DATE"));
                headerPs.addBatch();
            }
            headerPs.executeBatch();

            for (Map<String, String> row : rows) {
                detailPs.setString(1, req(row, "ORDER_ID"));
                detailPs.setString(2, req(row, "BOOK_TITLE"));
                detailPs.setString(3, req(row, "AUTHOR"));
                detailPs.setString(4, req(row, "PUBLICATION"));
                detailPs.setInt(5, parseInt(row, "QUANTITY"));
                detailPs.addBatch();
            }
            int[] counts = detailPs.executeBatch();
            return counts.length;
        }
    }

    private String resolveExportTarget(String uiTableName) throws SQLException {
        if (TABLE_STUDENTS.equalsIgnoreCase(uiTableName)) return "TBL_STUDENT";
        if (TABLE_BOOKS.equalsIgnoreCase(uiTableName)) return "TBL_BOOK_INFORMATION";
        if (TABLE_ORDERS.equalsIgnoreCase(uiTableName)) return "TBL_ORDER_EXPORT";
        throw new SQLException("Unsupported table selection: " + uiTableName);
    }

    private String resolveImportTarget(String uiTableName) throws SQLException {
        if (TABLE_STUDENTS.equalsIgnoreCase(uiTableName)) return "TBL_STUDENT";
        if (TABLE_BOOKS.equalsIgnoreCase(uiTableName)) return "TBL_BOOK_INFORMATION";
        if (TABLE_ORDERS.equalsIgnoreCase(uiTableName)) return "TBL_ORDER_HEADER";
        throw new SQLException("Unsupported table selection: " + uiTableName);
    }

    private String exportSqlFor(String tableName) throws SQLException {
        if ("TBL_STUDENT".equals(tableName)) return "SELECT * FROM TBL_STUDENT ORDER BY CARD_ID ASC";
        if ("TBL_BOOK_INFORMATION".equals(tableName)) return "SELECT * FROM TBL_BOOK_INFORMATION ORDER BY ACCESS_NO ASC";
        if ("TBL_ORDER_EXPORT".equals(tableName)) {
            return
                "SELECT h.ORDER_ID, h.S_ID, h.ORDER_DATE, d.BOOK_TITLE, d.AUTHOR, d.PUBLICATION, d.QUANTITY " +
                "FROM TBL_ORDER_HEADER h JOIN TBL_ORDER_DETAILS d ON d.ORDER_ID = h.ORDER_ID " +
                "ORDER BY h.ORDER_DATE ASC, h.ORDER_ID ASC, d.BOOK_TITLE ASC";
        }
        throw new SQLException("Unsupported table export: " + tableName);
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
            String val = req(row, key);
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return Date.valueOf(LocalDate.parse(val, dtf));
        } catch (Exception e) {
            throw new SQLException("Invalid date for " + key + ". Expected dd/MM/yyyy");
        }
    }

    private Date parseNullableDate(Map<String, String> row, String key) throws SQLException {
        String value = opt(row, key);
        if (value.isEmpty()) return null;
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return Date.valueOf(LocalDate.parse(value, dtf));
        } catch (Exception e) {
            throw new SQLException("Invalid date for " + key + ". Expected dd/MM/yyyy");
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

    /**
     * Fetches data from all supported tables for export.
     * @return Map of UI table name to list of rows
     * @throws SQLException if fetch fails
     */
    public Map<String, List<Map<String, Object>>> fetchAllTablesData() throws SQLException {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        
        for (String uiTableName : getSupportedTableNames()) {
            List<Map<String, Object>> data = fetchTableData(uiTableName);
            result.put(uiTableName, data);
        }
        
        return result;
    }

    /**
     * Imports data for all tables from a map structure.
     * @param tablesData Map of UI table name to list of rows
     * @param performedBy User ID performing the import
     * @return Total number of rows inserted across all tables
     * @throws SQLException if import fails (transaction will be rolled back)
     */
    public int importAllTables(Map<String, List<Map<String, String>>> tablesData, String performedBy) throws SQLException {
        int totalInserted = 0;
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            try {
                // Import each table
                for (Map.Entry<String, List<Map<String, String>>> entry : tablesData.entrySet()) {
                    String uiTableName = entry.getKey();
                    List<Map<String, String>> rows = entry.getValue();
                    
                    if (rows == null || rows.isEmpty()) {
                        continue;
                    }
                    
                    String tableName = resolveImportTarget(uiTableName);
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
                    
                    totalInserted += inserted;
                    AuditLogger.logAction(conn, performedBy, "Admin", 
                        "Imported " + inserted + " rows into " + tableName);
                }
                
                conn.commit();
                return totalInserted;
                
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException(e.getMessage(), e);
            } finally {
                conn.setAutoCommit(oldAuto);
            }
        }
    }
}
