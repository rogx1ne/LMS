package com.library.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class StudentLogic {

    public static final String COURSE_BCA = "BCA";
    public static final String COURSE_BBA = "BBA";
    public static final String COURSE_MCA = "MCA";

    public static final String STATUS_ACTIVE = "ACTIVE";

    private StudentLogic() {}

    public static String toTitleCaseName(String raw) throws ValidationException {
        String name = (raw == null) ? "" : raw.trim();
        if (name.isEmpty()) throw new ValidationException("Name is required.");
        if (!name.matches("[A-Za-z\\s]+")) throw new ValidationException("Name must contain only letters and spaces.");

        String[] words = name.toLowerCase(Locale.ENGLISH).split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            out.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return out.toString().trim();
    }

    public static int durationYearsForCourse(String course) throws ValidationException {
        if (COURSE_BCA.equals(course) || COURSE_BBA.equals(course)) return 3;
        if (COURSE_MCA.equals(course)) return 2;
        throw new ValidationException("Invalid course selected.");
    }

    public static List<String> generateSessions(String course, int currentYearInclusive, int count) throws ValidationException {
        int duration = durationYearsForCourse(course);
        if (count <= 0) return new ArrayList<>();

        List<String> sessions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int startYear = currentYearInclusive - i;
            int endYear = startYear + duration;
            sessions.add(startYear + "-" + endYear);
        }
        return sessions;
    }

    public static List<String> generateAllSessionsForFilters(int currentYearInclusive, int countEach) {
        Set<String> merged = new LinkedHashSet<>();
        try {
            merged.addAll(generateSessions(COURSE_BCA, currentYearInclusive, countEach));
            merged.addAll(generateSessions(COURSE_BBA, currentYearInclusive, countEach));
            merged.addAll(generateSessions(COURSE_MCA, currentYearInclusive, countEach));
        } catch (ValidationException ignored) {
            // constants above are valid; ignore
        }
        return new ArrayList<>(merged);
    }

    public static int parseSessionStartYear(String session) throws ValidationException {
        if (session == null) throw new ValidationException("Session is required.");
        String s = session.trim();
        if (!s.matches("\\d{4}-\\d{4}")) throw new ValidationException("Session format must be YYYY-YYYY.");
        return Integer.parseInt(s.substring(0, 4));
    }

    public static void validateRollNumber(String rollStr, String session) throws ValidationException {
        String roll = (rollStr == null) ? "" : rollStr.trim();
        if (!roll.matches("\\d{5}")) throw new ValidationException("Roll No must be exactly 5 digits.");

        int startYear = parseSessionStartYear(session);
        String yy = String.format("%02d", startYear % 100);
        if (!roll.startsWith(yy)) {
            throw new ValidationException("Roll No must start with '" + yy + "' for session " + session + ".");
        }
    }

    public static long parseAndValidateContact(String contactStr) throws ValidationException {
        String contact = (contactStr == null) ? "" : contactStr.trim();
        if (!contact.matches("\\d{10}")) throw new ValidationException("Contact must be exactly 10 digits.");
        try {
            return Long.parseLong(contact);
        } catch (NumberFormatException e) {
            throw new ValidationException("Contact must contain only digits.");
        }
    }

    public static int parseAndValidateBookLimit(String bookLimitStr) throws ValidationException {
        String s = (bookLimitStr == null) ? "" : bookLimitStr.trim();
        if (!s.matches("\\d+")) throw new ValidationException("Book Limit must be 1 or 2.");
        int limit = Integer.parseInt(s);
        if (limit != 1 && limit != 2) throw new ValidationException("Book Limit must be 1 or 2.");
        return limit;
    }

    public static double feeForBookLimit(int limit) throws ValidationException {
        if (limit == 2) return 800.0;
        if (limit == 1) return 500.0;
        throw new ValidationException("Book Limit must be 1 or 2.");
    }

    public static String defaultStatus() {
        return STATUS_ACTIVE;
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static int currentYear() {
        return Year.now().getValue();
    }
}
