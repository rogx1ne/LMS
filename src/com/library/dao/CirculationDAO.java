package com.library.dao;

import com.library.database.DBConnection;
import com.library.model.IssueReturnResult;
import com.library.model.IssueTransaction;
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
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement psIssue = conn.prepareStatement(issueTableSql);
                 ResultSet rsIssue = psIssue.executeQuery();
                 PreparedStatement psCol = conn.prepareStatement(bookColumnSql);
                 ResultSet rsCol = psCol.executeQuery()) {
                boolean issueOk = rsIssue.next() && rsIssue.getInt(1) == 1;
                boolean colOk = rsCol.next() && rsCol.getInt(1) == 1;
                return issueOk && colOk;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public String peekNextIssueId() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return fallbackIssueId();
            return circulationService.nextIssueId(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return fallbackIssueId();
        }
    }

    public IssueTransaction issueBook(String rawCardId, String rawAccessionNo, String rawIssuedBy)
        throws SQLException, ValidationException {
        String cardId = circulationService.normalizeCardId(rawCardId);
        String accessionNo = circulationService.normalizeAccessionNo(rawAccessionNo);
        String issuedBy = circulationService.normalizeIssuedBy(rawIssuedBy);

        String studentSql = "SELECT CARD_ID, BOOK_LIMIT FROM TBL_STUDENT WHERE CARD_ID = ? AND STATUS = 'ACTIVE'";
        String countIssuedSql = "SELECT COUNT(*) FROM TBL_ISSUE WHERE CARD_ID = ? AND STATUS = 'ISSUED'";
        String bookSql =
            "SELECT ACCESS_NO, BK_TITLE, AUTHOR_NAME, STATUS AS BOOK_STATUS, NVL(CIRC_STATUS, 'AVAILABLE') AS CIRC_STATUS " +
            "FROM TBL_BOOK_INFORMATION WHERE ACCESS_NO = ?";
        String insertIssueSql =
            "INSERT INTO TBL_ISSUE (ISSUE_ID, CARD_ID, ACCESSION_NO, ISSUE_DATE, DUE_DATE, RETURN_DATE, FINE, ISSUED_BY, STATUS) " +
            "VALUES (?, ?, ?, ?, ?, NULL, 0, ?, 'ISSUED')";
        String updateBookSql =
            "UPDATE TBL_BOOK_INFORMATION SET CIRC_STATUS = 'ISSUED' " +
            "WHERE ACCESS_NO = ? AND NVL(CIRC_STATUS, 'AVAILABLE') = 'AVAILABLE'";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");

            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int bookLimit;
                try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
                    ps.setString(1, cardId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new ValidationException("Card ID not found or student is not ACTIVE.");
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
                    ps.setString(2, cardId);
                    ps.setString(3, accessionNo);
                    ps.setDate(4, issueDate);
                    ps.setDate(5, dueDate);
                    ps.setString(6, issuedBy);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new SQLException("Failed to insert issue record.");
                }

                try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                    ps.setString(1, accessionNo);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new ValidationException("Book status changed by another process. Retry.");
                }

                conn.commit();
                return new IssueTransaction(
                    issueId,
                    cardId,
                    accessionNo,
                    bookTitle,
                    authorName,
                    issueDate,
                    dueDate,
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

    public List<IssueTransaction> getIssuedBooksByCard(String rawCardId) throws SQLException, ValidationException {
        String cardId = circulationService.normalizeCardId(rawCardId);
        String sql =
            "SELECT i.ISSUE_ID, i.CARD_ID, i.ACCESSION_NO, b.BK_TITLE, b.AUTHOR_NAME, " +
            "i.ISSUE_DATE, i.DUE_DATE, i.RETURN_DATE, i.FINE, i.ISSUED_BY, i.STATUS " +
            "FROM TBL_ISSUE i " +
            "JOIN TBL_BOOK_INFORMATION b ON b.ACCESS_NO = i.ACCESSION_NO " +
            "WHERE i.CARD_ID = ? AND i.STATUS = 'ISSUED' " +
            "ORDER BY i.ISSUE_DATE DESC, i.ISSUE_ID DESC";

        List<IssueTransaction> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("DB connection is null.");
            ps.setString(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new IssueTransaction(
                        rs.getString("ISSUE_ID"),
                        rs.getString("CARD_ID"),
                        rs.getString("ACCESSION_NO"),
                        rs.getString("BK_TITLE"),
                        rs.getString("AUTHOR_NAME"),
                        rs.getDate("ISSUE_DATE"),
                        rs.getDate("DUE_DATE"),
                        rs.getDate("RETURN_DATE"),
                        rs.getObject("FINE") == null ? null : rs.getDouble("FINE"),
                        rs.getString("ISSUED_BY"),
                        rs.getString("STATUS")
                    ));
                }
            }
        }
        return out;
    }

    public IssueReturnResult returnBook(String rawIssueId) throws SQLException, ValidationException {
        String issueId = circulationService.normalizeIssueId(rawIssueId);

        String fetchOpenIssueSql =
            "SELECT ISSUE_ID, ACCESSION_NO, DUE_DATE, STATUS " +
            "FROM TBL_ISSUE WHERE ISSUE_ID = ? FOR UPDATE";
        String updateIssueSql =
            "UPDATE TBL_ISSUE SET RETURN_DATE = ?, FINE = ?, STATUS = 'RETURNED' " +
            "WHERE ISSUE_ID = ? AND STATUS = 'ISSUED'";
        String updateBookSql =
            "UPDATE TBL_BOOK_INFORMATION SET CIRC_STATUS = 'AVAILABLE' WHERE ACCESS_NO = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("DB connection is null.");

            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String accessionNo;
                Date dueDate;
                String status;

                try (PreparedStatement ps = conn.prepareStatement(fetchOpenIssueSql)) {
                    ps.setString(1, issueId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new ValidationException("Issue ID not found.");
                        accessionNo = rs.getString("ACCESSION_NO");
                        dueDate = rs.getDate("DUE_DATE");
                        status = rs.getString("STATUS");
                    }
                }

                if (!CirculationService.ISSUE_STATUS_ISSUED.equalsIgnoreCase(status)) {
                    throw new ValidationException("Selected record is already RETURNED.");
                }

                Date returnDate = currentDbDate(conn);
                BigDecimal fine = circulationService.calculateWeeklyFine(dueDate.toLocalDate(), returnDate.toLocalDate());

                try (PreparedStatement ps = conn.prepareStatement(updateIssueSql)) {
                    ps.setDate(1, returnDate);
                    ps.setBigDecimal(2, fine);
                    ps.setString(3, issueId);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new SQLException("Failed to update issue row.");
                }

                try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                    ps.setString(1, accessionNo);
                    int rows = ps.executeUpdate();
                    if (rows != 1) throw new SQLException("Failed to set book status AVAILABLE.");
                }

                conn.commit();
                return new IssueReturnResult(issueId, accessionNo, returnDate, fine);
            } catch (SQLException | ValidationException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
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
