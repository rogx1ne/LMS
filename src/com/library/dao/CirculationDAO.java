package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.AvailableBookRow;
import com.library.model.CirculationReportRow;
import com.library.model.IssueReturnResult;
import com.library.model.IssueTransaction;
import com.library.service.AuditLogger;
import com.library.service.CirculationService;
import com.library.service.ValidationException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CirculationDAO {
    private final CirculationService circulationService = new CirculationService();

    public boolean isCirculationSchemaAvailable() {
        String issueTableSql = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'TBL_ISSUE'";
        String bookColumnSql = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'TBL_BOOK_INFORMATION' AND COLUMN_NAME = 'CIRC_STATUS'";
        String issueColsSql =
            "SELECT COUNT(*) FROM USER_TAB_COLUMNS " +
            "WHERE TABLE_NAME = 'TBL_ISSUE' AND COLUMN_NAME IN ('BORROWER_TYPE', 'FACULTY_NAME', 'FACULTY_CONTACT', 'RETURN_CONDITION')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement psIssue = conn.prepareStatement(issueTableSql);
                 ResultSet rsIssue = psIssue.executeQuery();
                 PreparedStatement psCol = conn.prepareStatement(bookColumnSql);
                 ResultSet rsCol = psCol.executeQuery();
                 PreparedStatement psIssueCols = conn.prepareStatement(issueColsSql);
                 ResultSet rsIssueCols = psIssueCols.executeQuery()) {
                boolean issueOk = rsIssue.next() && rsIssue.getInt(1) == 1;
                boolean colOk = rsCol.next() && rsCol.getInt(1) == 1;
                boolean issueColsOk = rsIssueCols.next() && rsIssueCols.getInt(1) == 4;
                return issueOk && colOk && issueColsOk;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public String peekNextIssueId() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return fallbackIssueId();
            return circulationService.peekNextIssueId(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return fallbackIssueId();
        }
    }

    public IssueTransaction issueBook(
        String rawBorrowerType,
        String rawCardId,
        String rawFacultyName,
        String rawFacultyContact,
        String rawAccessionNo,
        String rawIssuedBy
    )
        throws SQLException, ValidationException {
        String borrowerType = circulationService.normalizeBorrowerType(rawBorrowerType);
        String cardId = CirculationService.BORROWER_STUDENT.equals(borrowerType)
            ? circulationService.normalizeCardId(rawCardId)
            : null;
        String borrowerName = CirculationService.BORROWER_STUDENT.equals(borrowerType)
            ? null
            : circulationService.normalizeFacultyName(rawFacultyName);
        String borrowerContact = CirculationService.BORROWER_STUDENT.equals(borrowerType)
            ? null
            : circulationService.normalizeFacultyContact(rawFacultyContact);
        String accessionNo = circulationService.normalizeAccessionNo(rawAccessionNo);
        String issuedBy = circulationService.normalizeIssuedBy(rawIssuedBy);

        String studentSql = "SELECT CARD_ID, NAME, PH_NO, BOOK_LIMIT FROM TBL_STUDENT WHERE CARD_ID = ? AND STATUS = 'ACTIVE'";
        String countIssuedSql = "SELECT COUNT(*) FROM TBL_ISSUE WHERE CARD_ID = ? AND STATUS = 'ISSUED'";
        String bookSql =
            "SELECT ACCESS_NO, BK_TITLE, AUTHOR_NAME, STATUS AS BOOK_STATUS, NVL(CIRC_STATUS, 'AVAILABLE') AS CIRC_STATUS " +
            "FROM TBL_BOOK_INFORMATION WHERE ACCESS_NO = ?";
        String insertIssueSql =
            "INSERT INTO TBL_ISSUE (ISSUE_ID, BORROWER_TYPE, CARD_ID, FACULTY_NAME, FACULTY_CONTACT, ACCESSION_NO, ISSUE_DATE, DUE_DATE, RETURN_DATE, FINE, ISSUED_BY, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, 0, ?, 'ISSUED')";
        String updateBookSql =
            "UPDATE TBL_BOOK_INFORMATION SET CIRC_STATUS = 'ISSUED' " +
            "WHERE ACCESS_NO = ? AND NVL(CIRC_STATUS, 'AVAILABLE') = 'AVAILABLE'";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");

            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (CirculationService.BORROWER_STUDENT.equals(borrowerType)) {
                    int bookLimit;
                    try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
                        ps.setString(1, cardId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new ValidationException("Card ID not found or student is not ACTIVE.");
                            borrowerName = rs.getString("NAME");
                            borrowerContact = rs.getString("PH_NO");
                            bookLimit = rs.getInt("BOOK_LIMIT");
                        }
                    }

                    int issuedCount;
                    try (PreparedStatement ps = conn.prepareStatement(countIssuedSql)) {
                        ps.setString(1, cardId);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            issuedCount = rs.getInt(1);
                        }
                    }
                    if (issuedCount >= bookLimit) {
                        throw new ValidationException("Book limit reached for Card ID " + cardId + ".");
                    }
                }

                String bookTitle;
                String authorName;
                String bookStatus;
                String circStatus;
                try (PreparedStatement ps = conn.prepareStatement(bookSql)) {
                    ps.setString(1, accessionNo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new ValidationException("Accession No does not exist.");
                        bookTitle = rs.getString("BK_TITLE");
                        authorName = rs.getString("AUTHOR_NAME");
                        bookStatus = rs.getString("BOOK_STATUS");
                        circStatus = rs.getString("CIRC_STATUS");
                    }
                }
                if (!"ACTIVE".equalsIgnoreCase(bookStatus)) {
                    throw new ValidationException("Book is not active in catalog.");
                }
                if (!CirculationService.BOOK_CIRC_AVAILABLE.equalsIgnoreCase(circStatus)) {
                    throw new ValidationException("Book is not AVAILABLE for issuing.");
                }

                String issueId = circulationService.nextIssueId(conn);
                Date issueDate = currentDbDate(conn);
                Date dueDate = Date.valueOf(circulationService.calculateDueDate(issueDate.toLocalDate()));

                try (PreparedStatement ps = conn.prepareStatement(insertIssueSql)) {
                    ps.setString(1, issueId);
                    ps.setString(2, borrowerType);
                    ps.setString(3, cardId);
                    ps.setString(4, CirculationService.BORROWER_FACULTY.equals(borrowerType) ? borrowerName : null);
                    ps.setString(5, CirculationService.BORROWER_FACULTY.equals(borrowerType) ? borrowerContact : null);
                    ps.setString(6, accessionNo);
                    ps.setDate(7, issueDate);
                    ps.setDate(8, dueDate);
                    ps.setString(9, issuedBy);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new SQLException("Failed to insert issue record.");
                }

                try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                    ps.setString(1, accessionNo);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new ValidationException("Book status changed by another process. Retry.");
                }

                // Audit log the issue transaction
                String borrowerInfo = CirculationService.BORROWER_STUDENT.equals(borrowerType) 
                    ? "Student " + cardId + " (" + borrowerName + ")" 
                    : "Faculty " + borrowerName;
                AuditLogger.logAction(
                    conn,
                    issuedBy,
                    "Circulation",
                    "Issued book " + accessionNo + " (" + bookTitle + ") to " + borrowerInfo + " - Issue ID: " + issueId
                );

                conn.commit();
                return new IssueTransaction(
                    issueId,
                    borrowerType,
                    cardId,
                    borrowerName,
                    borrowerContact,
                    accessionNo,
                    bookTitle,
                    authorName,
                    issueDate,
                    dueDate,
                    null,
                    null,
                    0.0,
                    issuedBy,
                    CirculationService.ISSUE_STATUS_ISSUED
                );
            } catch (SQLException | ValidationException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public List<IssueTransaction> getOpenIssuedBooks() throws SQLException {
        String sql =
            "SELECT i.ISSUE_ID, i.BORROWER_TYPE, i.CARD_ID, " +
            "CASE WHEN i.BORROWER_TYPE = 'FACULTY' THEN i.FACULTY_NAME ELSE s.NAME END AS BORROWER_NAME, " +
            "CASE WHEN i.BORROWER_TYPE = 'FACULTY' THEN i.FACULTY_CONTACT ELSE TO_CHAR(s.PH_NO) END AS BORROWER_CONTACT, " +
            "i.ACCESSION_NO, b.BK_TITLE, b.AUTHOR_NAME, i.ISSUE_DATE, i.DUE_DATE, i.RETURN_DATE, i.RETURN_CONDITION, i.FINE, i.ISSUED_BY, i.STATUS " +
            "FROM TBL_ISSUE i " +
            "JOIN TBL_BOOK_INFORMATION b ON b.ACCESS_NO = i.ACCESSION_NO " +
            "LEFT JOIN TBL_STUDENT s ON s.CARD_ID = i.CARD_ID " +
            "WHERE i.STATUS = 'ISSUED' " +
            "ORDER BY i.ISSUE_DATE ASC, i.ISSUE_ID ASC";

        List<IssueTransaction> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            while (rs.next()) {
                out.add(mapIssueTransaction(rs));
            }
        }
        return out;
    }

    public IssueReturnResult returnBook(String rawIssueId, String rawReturnCondition, String performedBy) throws SQLException, ValidationException {
        String issueId = circulationService.normalizeIssueId(rawIssueId);
        String returnCondition = circulationService.normalizeReturnCondition(rawReturnCondition);

        String fetchOpenIssueSql =
            "SELECT i.ISSUE_ID, i.ACCESSION_NO, i.DUE_DATE, i.STATUS, i.BORROWER_TYPE, b.U_PRICE " +
            "FROM TBL_ISSUE i " +
            "JOIN TBL_BOOK_INFORMATION b ON b.ACCESS_NO = i.ACCESSION_NO " +
            "WHERE i.ISSUE_ID = ? FOR UPDATE";
        String updateIssueSql =
            "UPDATE TBL_ISSUE SET RETURN_DATE = ?, RETURN_CONDITION = ?, FINE = ?, STATUS = 'RETURNED' " +
            "WHERE ISSUE_ID = ? AND STATUS = 'ISSUED'";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");

            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String accessionNo;
                Date dueDate;
                String status;
                String borrowerType;
                BigDecimal bookPrice;

                try (PreparedStatement ps = conn.prepareStatement(fetchOpenIssueSql)) {
                    ps.setString(1, issueId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new ValidationException("Issue ID not found.");
                        accessionNo = rs.getString("ACCESSION_NO");
                        dueDate = rs.getDate("DUE_DATE");
                        status = rs.getString("STATUS");
                        borrowerType = rs.getString("BORROWER_TYPE");
                        bookPrice = rs.getBigDecimal("U_PRICE");
                    }
                }

                if (!CirculationService.ISSUE_STATUS_ISSUED.equalsIgnoreCase(status)) {
                    throw new ValidationException("Selected record is already RETURNED.");
                }

                Date returnDate = currentDbDate(conn);
                BigDecimal lateFine = circulationService.calculateWeeklyFine(dueDate.toLocalDate(), returnDate.toLocalDate());
                BigDecimal inspectionFine = circulationService.calculateInspectionFine(
                    borrowerType,
                    returnCondition,
                    lateFine,
                    bookPrice
                );
                BigDecimal totalFine = CirculationService.RETURN_CONDITION_LOST.equals(returnCondition)
                    ? inspectionFine
                    : lateFine.add(inspectionFine);

                try (PreparedStatement ps = conn.prepareStatement(updateIssueSql)) {
                    ps.setDate(1, returnDate);
                    ps.setString(2, returnCondition);
                    ps.setBigDecimal(3, totalFine);
                    ps.setString(4, issueId);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new SQLException("Failed to update issue row.");
                }

                String updateBookSql;
                if (CirculationService.RETURN_CONDITION_DAMAGED.equals(returnCondition)) {
                    updateBookSql =
                        "UPDATE TBL_BOOK_INFORMATION SET STATUS = 'DAMAGED', CIRC_STATUS = 'AVAILABLE' WHERE ACCESS_NO = ?";
                } else if (CirculationService.RETURN_CONDITION_LOST.equals(returnCondition)) {
                    updateBookSql =
                        "UPDATE TBL_BOOK_INFORMATION SET STATUS = 'LOST', CIRC_STATUS = 'AVAILABLE' WHERE ACCESS_NO = ?";
                } else {
                    updateBookSql =
                        "UPDATE TBL_BOOK_INFORMATION SET STATUS = 'ACTIVE', CIRC_STATUS = 'AVAILABLE' WHERE ACCESS_NO = ?";
                }

                try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                    ps.setString(1, accessionNo);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new SQLException("Failed to update book return status.");
                }

                // Audit log the return transaction
                AuditLogger.logAction(
                    conn,
                    performedBy,
                    "Circulation",
                    "Returned book " + accessionNo + " - Issue ID: " + issueId + 
                    ", Condition: " + returnCondition + ", Fine: " + totalFine
                );

                conn.commit();
                return new IssueReturnResult(issueId, accessionNo, returnDate, returnCondition, lateFine, inspectionFine, totalFine);
            } catch (SQLException | ValidationException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public List<CirculationReportRow> getCirculationReportRows() throws SQLException {
        String sql =
            "SELECT i.ISSUE_ID, i.BORROWER_TYPE, i.CARD_ID, " +
            "CASE WHEN i.BORROWER_TYPE = 'FACULTY' THEN i.FACULTY_NAME ELSE s.NAME END AS BORROWER_NAME, " +
            "CASE WHEN i.BORROWER_TYPE = 'FACULTY' THEN i.FACULTY_CONTACT ELSE TO_CHAR(s.PH_NO) END AS BORROWER_CONTACT, " +
            "i.ACCESSION_NO, b.BK_TITLE, b.AUTHOR_NAME, i.ISSUE_DATE, i.DUE_DATE, i.RETURN_DATE, i.RETURN_CONDITION, i.FINE, i.ISSUED_BY, i.STATUS " +
            "FROM TBL_ISSUE i " +
            "LEFT JOIN TBL_BOOK_INFORMATION b ON b.ACCESS_NO = i.ACCESSION_NO " +
            "LEFT JOIN TBL_STUDENT s ON s.CARD_ID = i.CARD_ID " +
            "ORDER BY i.ISSUE_DATE DESC, i.ISSUE_ID DESC";

        List<CirculationReportRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            while (rs.next()) {
                rows.add(new CirculationReportRow(
                    rs.getString("ISSUE_ID"),
                    rs.getString("BORROWER_TYPE"),
                    rs.getString("CARD_ID"),
                    rs.getString("BORROWER_NAME"),
                    rs.getString("BORROWER_CONTACT"),
                    rs.getString("ACCESSION_NO"),
                    rs.getString("BK_TITLE"),
                    rs.getString("AUTHOR_NAME"),
                    rs.getDate("ISSUE_DATE"),
                    rs.getDate("DUE_DATE"),
                    rs.getDate("RETURN_DATE"),
                    rs.getString("RETURN_CONDITION"),
                    rs.getBigDecimal("FINE"),
                    rs.getString("ISSUED_BY"),
                    rs.getString("STATUS")
                ));
            }
        }
        return rows;
    }

    private IssueTransaction mapIssueTransaction(ResultSet rs) throws SQLException {
        return new IssueTransaction(
            rs.getString("ISSUE_ID"),
            rs.getString("BORROWER_TYPE"),
            rs.getString("CARD_ID"),
            rs.getString("BORROWER_NAME"),
            rs.getString("BORROWER_CONTACT"),
            rs.getString("ACCESSION_NO"),
            rs.getString("BK_TITLE"),
            rs.getString("AUTHOR_NAME"),
            rs.getDate("ISSUE_DATE"),
            rs.getDate("DUE_DATE"),
            rs.getDate("RETURN_DATE"),
            rs.getString("RETURN_CONDITION"),
            rs.getObject("FINE") == null ? null : rs.getDouble("FINE"),
            rs.getString("ISSUED_BY"),
            rs.getString("STATUS")
        );
    }

    public List<AvailableBookRow> getAvailableBooks() throws SQLException {
        String sql =
            "SELECT ACCESS_NO, BK_TITLE, AUTHOR_NAME " +
            "FROM TBL_BOOK_INFORMATION " +
            "WHERE STATUS = 'ACTIVE' AND NVL(CIRC_STATUS, 'AVAILABLE') = 'AVAILABLE' " +
            "ORDER BY BK_TITLE ASC, AUTHOR_NAME ASC, ACCESS_NO ASC";

        List<AvailableBookRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (conn == null) throw new SQLException("DB connection is null.");
            while (rs.next()) {
                rows.add(new AvailableBookRow(
                    rs.getString("ACCESS_NO"),
                    rs.getString("BK_TITLE"),
                    rs.getString("AUTHOR_NAME")
                ));
            }
        }
        return rows;
    }

    private Date currentDbDate(Connection conn) throws SQLException {
        String sql = "SELECT TRUNC(SYSDATE) AS DT FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDate("DT");
        }
    }

    private String fallbackIssueId() {
        int yy = LocalDate.now().getYear() % 100;
        return "LID" + String.format("%02d", yy) + "00001";
    }
}
