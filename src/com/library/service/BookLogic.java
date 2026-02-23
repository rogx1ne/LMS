package com.library.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;

public final class BookLogic {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";
    public static final String STATUS_LOST = "LOST";
    public static final String STATUS_DAMAGED = "DAMAGED";

    private BookLogic() {}

    public static String normalizeAuthorName(String raw) throws ValidationException {
        String author = safe(raw);
        if (author.isEmpty()) throw new ValidationException("Author Name is required.");
        if (!author.contains(",")) {
            throw new ValidationException("Author must be entered as 'Last Name, First Name'.");
        }
        String[] parts = author.split(",", 2);
        if (safe(parts[0]).isEmpty() || safe(parts[1]).isEmpty()) {
            throw new ValidationException("Author format must be 'Last Name, First Name'.");
        }
        return toTitleCase(parts[0]) + ", " + toTitleCase(parts[1]);
    }

    public static String normalizeTitle(String raw) throws ValidationException {
        String title = safe(raw);
        if (title.isEmpty()) throw new ValidationException("Book Title is required.");
        return title;
    }

    public static int validateEdition(String raw) throws ValidationException {
        int edition = parsePositiveInt(raw, "Edition");
        if (edition > 99) throw new ValidationException("Edition must be <= 99.");
        return edition;
    }

    public static Integer validateVolumeNullable(String raw) throws ValidationException {
        String s = safe(raw);
        if (s.isEmpty()) return null;
        int v = parsePositiveInt(s, "Volume");
        if (v > 99) throw new ValidationException("Volume must be <= 99.");
        return v;
    }

    public static String normalizePublisher(String raw) throws ValidationException {
        String s = safe(raw);
        if (s.isEmpty()) throw new ValidationException("Publisher is required.");
        return s;
    }

    public static String normalizePublicationPlace(String raw) throws ValidationException {
        String s = safe(raw);
        if (s.isEmpty()) throw new ValidationException("Publisher Place is required.");
        return s;
    }

    public static int validatePublicationYear(String raw) throws ValidationException {
        int y = parsePositiveInt(raw, "Year of Publication");
        int current = LocalDate.now().getYear();
        if (y < 1500 || y > current) {
            throw new ValidationException("Year of Publication must be between 1500 and " + current + ".");
        }
        return y;
    }

    public static int validatePages(String raw) throws ValidationException {
        int p = parsePositiveInt(raw, "Pages");
        if (p > 99999) throw new ValidationException("Pages must be <= 99999.");
        return p;
    }

    public static String normalizeSource(String raw) throws ValidationException {
        String s = safe(raw);
        if (s.isEmpty()) throw new ValidationException("Source is required.");
        return s.toUpperCase(Locale.ENGLISH);
    }

    public static String normalizeClassNo(String raw) {
        return safe(raw).toUpperCase(Locale.ENGLISH);
    }

    public static String generateBookNo(String authorName) throws ValidationException {
        String author = normalizeAuthorName(authorName);
        String letters = author.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ENGLISH);
        if (letters.length() >= 3) return letters.substring(0, 3);
        if (letters.isEmpty()) throw new ValidationException("Unable to generate Book No from Author Name.");
        if (letters.length() == 1) return letters + "XX";
        return letters + "X";
    }

    public static BigDecimal validateCost(String raw) throws ValidationException {
        String s = safe(raw);
        if (s.isEmpty()) throw new ValidationException("Cost is required.");
        try {
            BigDecimal cost = new BigDecimal(s);
            if (cost.signum() <= 0) throw new ValidationException("Cost must be greater than 0.");
            return cost.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Cost must be numeric.");
        } catch (ArithmeticException ex) {
            throw new ValidationException("Invalid cost precision.");
        }
    }

    public static String normalizeBillNo(String raw) {
        return safe(raw).toUpperCase(Locale.ENGLISH);
    }

    public static String normalizeRemarks(String raw) {
        return safe(raw);
    }

    public static String deriveStatus(LocalDate withdrawnDate) {
        return withdrawnDate == null ? STATUS_ACTIVE : STATUS_WITHDRAWN;
    }

    private static int parsePositiveInt(String raw, String field) throws ValidationException {
        String s = safe(raw);
        if (!s.matches("\\d+")) throw new ValidationException(field + " must be numeric.");
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) throw new ValidationException(field + " must be > 0.");
            return v;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " is out of valid range.");
        }
    }

    private static String toTitleCase(String text) {
        String[] words = text.trim().toLowerCase(Locale.ENGLISH).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
