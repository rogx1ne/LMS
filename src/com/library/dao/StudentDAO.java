package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.Student;
import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // --- 1. GENERATE CARD ID (PPU-YYNNN) ---
    public String generateCardId() {
        int year = Year.now().getValue() % 100; // Get last 2 digits (e.g., 26)
        String prefix = "PPU-" + year;
        
        String query = "SELECT MAX(CARD_ID) FROM TBL_STUDENT WHERE CARD_ID LIKE '" + prefix + "%'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null) {
                    // Extract NNN part
                    int seq = Integer.parseInt(lastId.substring(6)); 
                    return String.format("PPU-%d%03d", year, seq + 1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // Default start for the year
        return String.format("PPU-%d001", year);
    }

    // --- 2. GENERATE RECEIPT NO (LRYYNNN) ---
    // Note: Assuming DB column is VARCHAR2 to hold "LR". 
    // If strict NUMBER(7), remove "LR" and parse only YYNNN.
    public String generateReceiptNo() {
        int year = Year.now().getValue() % 100;
        String prefix = "LR" + year;
        
        String query = "SELECT MAX(RECEIPT_NO) FROM TBL_STUDENT WHERE RECEIPT_NO LIKE '" + prefix + "%'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                String lastReceipt = rs.getString(1);
                if (lastReceipt != null) {
                    // Extract NNN (LR is index 0-1, YY is 2-3, NNN starts at 4)
                    int seq = Integer.parseInt(lastReceipt.substring(4));
                    return String.format("LR%d%03d", year, seq + 1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        return String.format("LR%d001", year);
    }

    // --- 3. SAVE STUDENT ---
    public boolean addStudent(Student s) {
        String query = "INSERT INTO TBL_STUDENT (CARD_ID, ROLL, NAME, PH_NO, ADDR, COURSE, ACAD_SESSION, " +
                       "RECEIPT_NO, ISSUED_BY, ISSUE_DATE, BOOK_LIMIT, FEE, STATUS) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setString(1, s.getCardId());
            pst.setInt(2, s.getRoll());
            pst.setString(3, s.getName());
            pst.setLong(4, s.getPhone());
            pst.setString(5, s.getAddress());
            pst.setString(6, s.getCourse());
            pst.setString(7, s.getSession());
            pst.setString(8, s.getReceiptNo());
            pst.setString(9, s.getIssuedBy());
            pst.setInt(10, s.getBookLimit());
            pst.setDouble(11, s.getFee());
            pst.setString(12, s.getStatus());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. FETCH ALL (For Table View) ---
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM TBL_STUDENT ORDER BY ISSUE_DATE DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {
                list.add(new Student(
                    rs.getString("CARD_ID"),
                    rs.getInt("ROLL"),
                    rs.getString("NAME"),
                    rs.getLong("PH_NO"),
                    rs.getString("ADDR"),
                    rs.getString("COURSE"),
                    rs.getString("ACAD_SESSION"),
                    rs.getString("RECEIPT_NO"),
                    rs.getString("ISSUED_BY"),
                    rs.getDate("ISSUE_DATE"),
                    rs.getInt("BOOK_LIMIT"),
                    rs.getDouble("FEE"),
                    rs.getString("STATUS")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 1. Method to fetch full details for editing
public Student getStudentByCardId(String cardId) {
    String sql = "SELECT * FROM TBL_STUDENT WHERE CARD_ID = ?";
    try (java.sql.Connection conn = DBConnection.getConnection();
         java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, cardId);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Student(
                    rs.getString("CARD_ID"),
                    rs.getInt("ROLL"),
                    rs.getString("NAME"),
                    rs.getLong("PH_NO"),
                    rs.getString("ADDR"),
                    rs.getString("COURSE"),
                    rs.getString("ACAD_SESSION"),
                    rs.getString("RECEIPT_NO"),
                    rs.getString("ISSUED_BY"),
                    rs.getDate("ISSUE_DATE"),
                    rs.getInt("BOOK_LIMIT"),
                    rs.getDouble("FEE"),
                    rs.getString("STATUS")
                );
            }
        }
    } catch (Exception e) { e.printStackTrace(); }
    return null;
}

// 2. Method to update an existing student
public boolean updateStudent(Student s) {
    String sql = "UPDATE TBL_STUDENT SET NAME=?, ROLL=?, PH_NO=?, ADDR=?, " +
                 "COURSE=?, ACAD_SESSION=?, BOOK_LIMIT=?, FEE=?, STATUS=? " +
                 "WHERE CARD_ID=?";
    try (java.sql.Connection conn = DBConnection.getConnection();
         java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, s.getName());
        ps.setInt(2, s.getRoll());
        ps.setLong(3, s.getPhone());
        ps.setString(4, s.getAddress());
        ps.setString(5, s.getCourse());
        ps.setString(6, s.getSession());
        ps.setInt(7, s.getBookLimit());
        ps.setDouble(8, s.getFee());
        ps.setString(9, s.getStatus());
        ps.setString(10, s.getCardId());
        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}