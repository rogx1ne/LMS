package com.library.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;

public class AdminService {
    public String validateName(String raw) throws ValidationException {
        String name = normalize(raw);
        if (name.isEmpty()) throw new ValidationException("Name is required.");
        if (name.length() > 20) throw new ValidationException("Name must be <= 20 characters.");
        return name;
    }

    public String validatePassword(String raw) throws ValidationException {
        String password = normalize(raw);
        if (password.isEmpty()) throw new ValidationException("Password is required.");
        if (password.length() > 10) throw new ValidationException("Password must be <= 10 characters.");
        return password;
    }

    public String validateEmail(String raw) throws ValidationException {
        String email = normalize(raw).toLowerCase(Locale.ENGLISH);
        if (email.isEmpty()) throw new ValidationException("Email is required.");
        if (email.length() > 20) throw new ValidationException("Email must be <= 20 characters.");
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
            return Date.valueOf(LocalDate.parse(date));
        } catch (Exception e) {
            throw new ValidationException("Date must be in yyyy-MM-dd format.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
