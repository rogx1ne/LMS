package com.library.setup;

import com.library.service.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Creates administrative user during setup
 */
public class AdminUserCreator {
    
    private Connection connection;
    
    public AdminUserCreator(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Check if username already exists
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TBL_CREDENTIALS WHERE NAME = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        
        rs.close();
        stmt.close();
        return exists;
    }
    
    /**
     * Get next user ID from ID counter
     */
    private String getNextUserId() throws SQLException {
        String sql = "SELECT NEXT_VAL FROM TBL_ID_COUNTER WHERE COUNTER_KEY = 'USER_ID'";
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        int currentValue = 0;
        if (rs.next()) {
            currentValue = rs.getInt(1);
        }
        rs.close();
        stmt.close();
        
        // Increment counter
        int nextValue = currentValue + 1;
        String updateSql = "UPDATE TBL_ID_COUNTER SET NEXT_VAL = ? WHERE COUNTER_KEY = 'USER_ID'";
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setInt(1, nextValue);
        updateStmt.executeUpdate();
        updateStmt.close();
        
        return String.format("%05d", nextValue);
    }
    
    /**
     * Create admin user
     */
    public String createAdminUser(String username, String password, String fullName, String email) throws Exception {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        
        // Check if username exists
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // Hash password
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        // Get next user ID
        String userId = getNextUserId();
        
        // Insert user
        String sql = "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PSWD, ROLE, EMAIL, PHNO, STATUS) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
        
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setString(2, username);
        stmt.setString(3, hashedPassword);
        stmt.setString(4, "ADMIN");
        stmt.setString(5, email != null && !email.trim().isEmpty() ? email : "noemail@lms.local");
        stmt.setInt(6, 0); // Placeholder phone number
        
        stmt.executeUpdate();
        stmt.close();
        
        return userId;
    }
    
    /**
     * Validate password strength
     */
    public static PasswordValidation validatePassword(String password) {
        PasswordValidation result = new PasswordValidation();
        
        if (password == null || password.isEmpty()) {
            result.isValid = false;
            result.message = "Password cannot be empty";
            return result;
        }
        
        if (password.length() < 6) {
            result.isValid = false;
            result.message = "Password must be at least 6 characters";
            return result;
        }
        
        if (password.length() > 50) {
            result.isValid = false;
            result.message = "Password is too long (max 50 characters)";
            return result;
        }
        
        // Check for at least one letter and one number (optional, for better security)
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            result.isValid = true; // Still valid, but warn
            result.message = "Weak password. Consider using letters and numbers.";
            result.isWeak = true;
            return result;
        }
        
        result.isValid = true;
        result.message = "Strong password";
        return result;
    }
    
    public static class PasswordValidation {
        public boolean isValid;
        public boolean isWeak;
        public String message;
    }
}
