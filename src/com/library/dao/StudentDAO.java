package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.library.service.StudentIdGenerator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private final StudentIdGenerator idGenerator = new StudentIdGenerator();

    private boolean existsRollInCourseSession(
        Connection conn,
        int roll,
        String course,
        String session,
        String excludeCardId
    ) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM TBL_STUDENT " +
            "WHERE ROLL = ? AND COURSE = ? AND ACAD_SESSION = ? " +
            (excludeCardId != null ? "AND CARD_ID <> ?" : "");

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, roll);
            pst.setString(2, course);
            pst.setString(3, session);
            if (excludeCardId != null) {
                pst.setString(4, excludeCardId);
            }
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isRollTakenInCourseSession(int roll, String course, String session, String excludeCardId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            return existsRollInCourseSession(conn, roll, course, session, excludeCardId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Student registerStudent(
        int roll,
        String name,
        long phone,
        String address,
        String course,
        String session,
        String issuedBy,
        int bookLimit,
        double fee,
        String status
    ) throws SQLException {
        String insertSql =
            "INSERT INTO TBL_STUDENT (CARD_ID, ROLL, NAME, PH_NO, ADDR, COURSE, ACAD_SESSION, " +
            "RECEIPT_NO, ISSUED_BY, ISSUE_DATE, BOOK_LIMIT, FEE, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (existsRollInCourseSession(conn, roll, course, session, null)) {
                    throw new SQLException("Roll No already exists for selected course and session.");
                }

                String cardId = idGenerator.nextCardId(conn);
                String receiptNo = idGenerator.nextReceiptNo(conn);

                try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                    pst.setString(1, cardId);
                    pst.setInt(2, roll);
                    pst.setString(3, name);
                    pst.setLong(4, phone);
                    pst.setString(5, address);
                    pst.setString(6, course);
                    pst.setString(7, session);
                    pst.setString(8, receiptNo);
                    pst.setString(9, issuedBy);
                    pst.setInt(10, bookLimit);
                    pst.setDouble(11, fee);
                    pst.setString(12, status);

                    int rows = pst.executeUpdate();
                    if (rows != 1) throw new SQLException("Insert failed (rows=" + rows + ").");
                }

                conn.commit();

                java.sql.Date issueDate = new java.sql.Date(System.currentTimeMillis());
                return new Student(
                    cardId, roll, name, phone, address, course, session, receiptNo, issuedBy, issueDate, bookLimit, fee, status
                );
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }

    // --- 3. SAVE STUDENT ---
    public boolean addStudent(Student s) {
        String query = "INSERT INTO TBL_STUDENT (CARD_ID, ROLL, NAME, PH_NO, ADDR, COURSE, ACAD_SESSION, " +
                       "RECEIPT_NO, ISSUED_BY, ISSUE_DATE, BOOK_LIMIT, FEE, STATUS) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            if (existsRollInCourseSession(conn, s.getRoll(), s.getCourse(), s.getSession(), null)) {
                return false;
            }

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

    // --- 4. FETCH ACTIVE STUDENTS (For Table View) ---
    public List<Student> getActiveStudents() {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM TBL_STUDENT WHERE STATUS = 'ACTIVE' ORDER BY ISSUE_DATE DESC";
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
        if (existsRollInCourseSession(conn, s.getRoll(), s.getCourse(), s.getSession(), s.getCardId())) {
            return false;
        }
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

    public List<BorrowRecord> getBorrowHistory(String cardId) {
        List<BorrowRecord> out = new ArrayList<>();
        String sql =
            "SELECT i.ACCESSION_NO AS ACCESS_NO, b.BK_TITLE, b.AUTHOR_NAME, i.ISSUE_DATE AS ISSU_DATE, " +
            "i.DUE_DATE, i.RETURN_DATE AS RTR_DATE, i.FINE AS FINE_AMT " +
            "FROM TBL_ISSUE i " +
            "JOIN TBL_BOOK_INFORMATION b ON b.ACCESS_NO = i.ACCESSION_NO " +
            "WHERE i.CARD_ID = ? " +
            "ORDER BY i.ISSUE_DATE DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new BorrowRecord(
                        rs.getString("ACCESS_NO"),
                        rs.getString("BK_TITLE"),
                        rs.getString("AUTHOR_NAME"),
                        rs.getDate("ISSU_DATE"),
                        rs.getDate("DUE_DATE"),
                        rs.getDate("RTR_DATE"),
                        (rs.getObject("FINE_AMT") == null) ? null : rs.getDouble("FINE_AMT")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}
