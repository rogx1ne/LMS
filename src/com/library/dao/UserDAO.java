package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.User;
import java.sql.*;

public class UserDAO {

    // 1. VALIDATE LOGIN
    public boolean validateLogin(String userId, String password) {
        String query = "SELECT * FROM TBL_CREDENTIALS WHERE USER_ID = ? AND PASSWORD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if a record is found

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. ADD USER (SIGN UP)
    public boolean addUser(User user) {
        String query = "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PASSWORD, SQUES, SANS) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getSecurityQuestion());
            pstmt.setString(5, user.getSecurityAnswer());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. FETCH SECURITY QUESTION (For Forgot Password)
    public String getSecurityQuestion(String userId) {
        String query = "SELECT SQUES FROM TBL_CREDENTIALS WHERE USER_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("SQUES");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if user not found
    }

    // 4. RESET PASSWORD
    public boolean updatePassword(String userId, String securityAnswer, String newPassword) {
        // First check if answer matches
        String checkQuery = "SELECT * FROM TBL_CREDENTIALS WHERE USER_ID = ? AND SANS = ?";
        String updateQuery = "UPDATE TBL_CREDENTIALS SET PASSWORD = ? WHERE USER_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

            // Check Answer
            checkStmt.setString(1, userId);
            checkStmt.setString(2, securityAnswer);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Answer correct, proceed to update
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, userId);
                updateStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ... existing code ...

    // NEW: Step 2 of Recovery - Validate the answer ONLY
    public boolean validateSecurityAnswer(String userId, String securityAnswer) {
        String query = "SELECT * FROM TBL_CREDENTIALS WHERE USER_ID = ? AND SANS = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, securityAnswer);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // True if answer matches

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Step 3 of Recovery - Update password (no answer check needed here)
    public boolean updatePasswordOnly(String userId, String newPassword) {
        String query = "UPDATE TBL_CREDENTIALS SET PASSWORD = ? WHERE USER_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Generate the next ID automatically (e.g., LIB01 -> LIB02)
    public String generateNextUserId() {
        String query = "SELECT USER_ID FROM TBL_CREDENTIALS ORDER BY USER_ID DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String lastId = rs.getString("USER_ID");
                // Expecting format "LIBxx"
                if (lastId.startsWith("LIB")) {
                    // Extract the number part (everything after "LIB")
                    String numberPart = lastId.substring(3); 
                    try {
                        int id = Integer.parseInt(numberPart);
                        id++; // Increment
                        // Format back to LIB + 2 digits (e.g., 5 -> "LIB05")
                        return String.format("LIB%02d", id);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Default if table is empty or error occurs
        return "LIB01";
    }

}