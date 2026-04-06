package com.library.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;

public class AdminService {
    public String validateName(String raw) throws ValidationException {
        String name = normalize(raw);
        if (name.isEmpty()) throw new ValidationException("Name is required.");
        if (name.length() > 50) throw new ValidationException("Name must be <= 50 characters.");
        return name;
    }

    public String validatePassword(String raw) throws ValidationException {
        String password = normalize(raw);
        if (password.isEmpty()) throw new ValidationException("Password is required.");
        if (password.length() < 8) throw new ValidationException("Password must be at least 8 characters.");
        if (password.length() > 64) throw new ValidationException("Password must be <= 64 characters.");
        return password;
    }

    public String validateEmail(String raw) throws ValidationException {
        String email = normalize(raw).toLowerCase(Locale.ENGLISH);
        if (email.isEmpty()) throw new ValidationException("Email is required.");
        if (email.length() > 80) throw new ValidationException("Email must be <= 80 characters.");
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("Invalid email format.");
        }
        return email;
    }

    public String validatePhone(String raw) throws ValidationException {
        String phone = normalize(raw);
        if (!phone.matches("\\d{10}")) throw new ValidationException("Phone must be exactly 10 digits.");
        return phone;
    }

    public Date parseDateOrNull(String raw) throws ValidationException {
        String date = normalize(raw);
        if (date.isEmpty()) return null;
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return java.sql.Date.valueOf(LocalDate.parse(date, dtf));
        } catch (Exception e) {
            throw new ValidationException("Date must be in dd/MM/yyyy format.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
