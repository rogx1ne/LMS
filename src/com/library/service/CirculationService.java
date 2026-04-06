package com.library.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class CirculationService {
    public static final String BORROWER_STUDENT = "STUDENT";
    public static final String BORROWER_FACULTY = "FACULTY";
    public static final String ISSUE_STATUS_ISSUED = "ISSUED";
    public static final String ISSUE_STATUS_RETURNED = "RETURNED";
    public static final String RETURN_CONDITION_GOOD = "GOOD";
    public static final String RETURN_CONDITION_DAMAGED = "DAMAGED";
    public static final String RETURN_CONDITION_LOST = "LOST";
    public static final String BOOK_CIRC_AVAILABLE = "AVAILABLE";
    public static final String BOOK_CIRC_ISSUED = "ISSUED";
    public static final String BOOK_STATUS_ACTIVE = "ACTIVE";
    public static final String BOOK_STATUS_DAMAGED = "DAMAGED";
    public static final String BOOK_STATUS_LOST = "LOST";
    public static final BigDecimal WEEKLY_FINE_RATE = new BigDecimal("50.00");

    public String normalizeCardId(String raw) throws ValidationException {
        return normalizeId(raw, "Card ID");
    }

    public String normalizeBorrowerType(String raw) throws ValidationException {
        String borrowerType = raw == null ? "" : raw.trim().toUpperCase(Locale.ENGLISH);
        if (!BORROWER_STUDENT.equals(borrowerType) && !BORROWER_FACULTY.equals(borrowerType)) {
            throw new ValidationException("Borrower Type must be Student or Faculty.");
        }
        return borrowerType;
    }

    public String normalizeAccessionNo(String raw) throws ValidationException {
        return normalizeId(raw, "Accession No");
    }

    public String normalizeIssueId(String raw) throws ValidationException {
        return normalizeId(raw, "Issue ID");
    }

    public String normalizeReturnCondition(String raw) throws ValidationException {
        String condition = raw == null ? "" : raw.trim().toUpperCase(Locale.ENGLISH);
        if (!RETURN_CONDITION_GOOD.equals(condition)
            && !RETURN_CONDITION_DAMAGED.equals(condition)
            && !RETURN_CONDITION_LOST.equals(condition)) {
            throw new ValidationException("Inspection result must be Good, Damaged, or Lost.");
        }
        return condition;
    }

    public String normalizeIssuedBy(String raw) throws ValidationException {
        String issuedBy = raw == null ? "" : raw.trim();
        if (issuedBy.isEmpty()) throw new ValidationException("Issued By is required.");
        return issuedBy.toUpperCase(Locale.ENGLISH);
    }

    public String normalizeFacultyName(String raw) throws ValidationException {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) throw new ValidationException("Faculty Name is required.");
        return name;
    }

    public String normalizeFacultyContact(String raw) throws ValidationException {
        String contact = raw == null ? "" : raw.trim();
        if (!contact.matches("\\d{10}")) {
            throw new ValidationException("Faculty Contact must be 10 digits.");
        }
        return contact;
    }

    public String nextIssueId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "LID" + String.format("%02d", yy);
        long next = IdCounterService.nextValue(conn, "ISSUE_" + yy, observedIssueMax(conn, prefix + "%"));
        return prefix + String.format("%05d", next);
    }

    public String peekNextIssueId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "LID" + String.format("%02d", yy);
        long next = IdCounterService.peekNextValue(conn, "ISSUE_" + yy, observedIssueMax(conn, prefix + "%"));
        return prefix + String.format("%05d", next);
    }

    private long observedIssueMax(Connection conn, String prefixLike) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(ISSUE_ID, 6))), 0) FROM TBL_ISSUE WHERE ISSUE_ID LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefixLike);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public LocalDate calculateDueDate(LocalDate issueDate) {
        return issueDate.plusDays(7);
    }

    public BigDecimal calculateWeeklyFine(LocalDate dueDate, LocalDate returnDate) {
        long lateDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (lateDays <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        long lateWeeks = (lateDays + 6) / 7;
        return WEEKLY_FINE_RATE.multiply(BigDecimal.valueOf(lateWeeks)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateInspectionFine(
        String borrowerType,
        String returnCondition,
        BigDecimal lateFine,
        BigDecimal bookPrice
    ) throws ValidationException {
        BigDecimal normalizedLateFine = amountOrZero(lateFine);
        BigDecimal normalizedBookPrice = amountOrZero(bookPrice);

        if (RETURN_CONDITION_GOOD.equals(returnCondition)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (RETURN_CONDITION_DAMAGED.equals(returnCondition)) {
            return BORROWER_STUDENT.equals(borrowerType)
                ? normalizedLateFine
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (normalizedBookPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Book price is not available for lost-book fine calculation.");
        }
        return normalizedBookPrice;
    }

    private BigDecimal amountOrZero(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeId(String raw, String field) throws ValidationException {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) throw new ValidationException(field + " is required.");
        return value.toUpperCase(Locale.ENGLISH);
    }
}
