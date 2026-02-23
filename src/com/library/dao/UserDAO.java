package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.User;
import java.sql.*;

public class UserDAO {

    // 1. VALIDATE LOGIN
    public boolean validateLogin(String userId, String password) {
        String query = "SELECT 1 FROM TBL_CREDENTIALS WHERE USER_ID = ? AND PSWD = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDisplayName(String userId) {
        String query = "SELECT NAME FROM TBL_CREDENTIALS WHERE USER_ID = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getString("NAME");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserRole(String userId) {
        String fallback = "ADMIN".equalsIgnoreCase(userId) ? "ADMIN" : "LIBRARIAN";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return fallback;
            if (!roleColumnExists(conn)) return fallback;

            String query = "SELECT NVL(ROLE, 'LIBRARIAN') AS ROLE FROM TBL_CREDENTIALS WHERE USER_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("ROLE");
                        if ("ADMIN".equalsIgnoreCase(role)) return "ADMIN";
                        return "LIBRARIAN";
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return fallback;
    }

    // 2. ADD USER (SIGN UP)
    public boolean addUser(User user) {
        String query = "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PSWD, EMAIL, PHNO, STATUS) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPassword());
                pstmt.setString(4, user.getEmail());
                pstmt.setLong(5, user.getPhoneNumber());
                pstmt.setString(6, user.getStatus() == null ? "ACTIVE" : user.getStatus());
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Generate the next ID automatically (e.g., U001 -> U002)
    public String generateNextUserId() {
        String query = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(USER_ID, 2))), 0) AS MAX_ID " +
                       "FROM TBL_CREDENTIALS WHERE REGEXP_LIKE(USER_ID, '^U[0-9]{3}$')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "U001";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int id = rs.getInt("MAX_ID") + 1;
                    return String.format("U%03d", id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "U001";
    }

    public boolean resetPassword(String userId, String email, long phone, String newPassword) {
        String query =
            "UPDATE TBL_CREDENTIALS SET PSWD = ? " +
            "WHERE USER_ID = ? AND EMAIL = ? AND PHNO = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, userId);
                pstmt.setString(3, email);
                pstmt.setLong(4, phone);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getActiveUserEmail(String userId) {
        String query = "SELECT EMAIL FROM TBL_CREDENTIALS WHERE USER_ID = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getString("EMAIL");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePasswordByUserId(String userId, String newPassword) {
        String query = "UPDATE TBL_CREDENTIALS SET PSWD = ? WHERE USER_ID = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, userId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean roleColumnExists(Connection conn) {
        String query =
            "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'TBL_CREDENTIALS' AND COLUMN_NAME = 'ROLE'";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

}
