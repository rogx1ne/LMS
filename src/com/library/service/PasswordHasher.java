package com.library.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

public final class PasswordHasher {
    private static final String PBKDF2_PREFIX = "PBKDF2";
    private static final String SHA256_PREFIX = "SHA256";
    private static final int PBKDF2_ITERATIONS = 65_536;
    private static final int PBKDF2_KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    public static String hashPassword(String rawPassword) {
        String password = normalize(rawPassword);
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS);
        return PBKDF2_PREFIX
            + "$" + PBKDF2_ITERATIONS
            + "$" + Base64.getEncoder().encodeToString(salt)
            + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String rawPassword, String storedPassword) {
        String password = normalize(rawPassword);
        String stored = normalize(storedPassword);
        if (password.isEmpty() || stored.isEmpty()) return false;

        if (stored.startsWith(PBKDF2_PREFIX + "$")) {
            return verifyPbkdf2(password, stored);
        }
        if (stored.startsWith(SHA256_PREFIX + "$")) {
            return verifySha256(password, stored);
        }
        return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), stored.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean needsUpgrade(String storedPassword) {
        String stored = normalize(storedPassword);
        return !stored.startsWith(PBKDF2_PREFIX + "$");
    }

    private static boolean verifyPbkdf2(String rawPassword, String storedPassword) {
        try {
            String[] parts = storedPassword.split("\\$");
            if (parts.length != 4) return false;

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(actual, expected);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean verifySha256(String rawPassword, String storedPassword) {
        String expected = storedPassword.substring((SHA256_PREFIX + "$").length()).toLowerCase(Locale.ENGLISH);
        String actual = sha256Hex(rawPassword);
        return MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        }
    }

    private static String sha256Hex(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
