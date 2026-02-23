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
    public static final String ISSUE_STATUS_ISSUED = "ISSUED";
    public static final String ISSUE_STATUS_RETURNED = "RETURNED";
    public static final String BOOK_CIRC_AVAILABLE = "AVAILABLE";
    public static final String BOOK_CIRC_ISSUED = "ISSUED";
    public static final BigDecimal WEEKLY_FINE_RATE = new BigDecimal("50.00");

    public String normalizeCardId(String raw) throws ValidationException {
        return normalizeId(raw, "Card ID");
    }

    public String normalizeAccessionNo(String raw) throws ValidationException {
        return normalizeId(raw, "Accession No");
    }

    public String normalizeIssueId(String raw) throws ValidationException {
        return normalizeId(raw, "Issue ID");
    }

    public String normalizeIssuedBy(String raw) throws ValidationException {
        String issuedBy = raw == null ? "" : raw.trim();
        if (issuedBy.isEmpty()) throw new ValidationException("Issued By is required.");
        return issuedBy.toUpperCase(Locale.ENGLISH);
    }

    public String nextIssueId(Connection conn) throws SQLException {
        int yy = Year.now().getValue() % 100;
        String prefix = "LID" + String.format("%02d", yy);
        String sql = "SELECT COUNT(*) FROM TBL_ISSUE WHERE ISSUE_ID LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int next = rs.getInt(1) + 1;
                return prefix + String.format("%05d", next);
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

    private String normalizeId(String raw, String field) throws ValidationException {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) throw new ValidationException(field + " is required.");
        return value.toUpperCase(Locale.ENGLISH);
    }
}
