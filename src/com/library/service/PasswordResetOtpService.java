package com.library.service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class PasswordResetOtpService {
    private static final Random RANDOM = new Random();

    public String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendOtp(String toEmail, String userId, String otp) throws Exception {
        String host = env("LMS_SMTP_HOST");
        String port = envOrDefault("LMS_SMTP_PORT", "587");
        String username = env("LMS_SMTP_USER");
        String password = env("LMS_SMTP_PASS");
        String from = envOrDefault("LMS_SMTP_FROM", username);

        if (isBlank(host) || isBlank(username) || isBlank(password) || isBlank(from)) {
            throw new IllegalStateException(
                "SMTP is not configured. Set LMS_SMTP_HOST, LMS_SMTP_PORT, LMS_SMTP_USER, LMS_SMTP_PASS, LMS_SMTP_FROM."
            );
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("LMS Password Reset OTP");
        message.setText(
            "Hello,\n\n" +
            "OTP for password reset of user " + userId + " is: " + otp + "\n" +
            "This OTP is valid for 10 minutes.\n\n" +
            "If you did not request this, please ignore this email."
        );

        Transport.send(message);
    }

    private String env(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value.trim();
    }

    private String envOrDefault(String key, String fallback) {
        String value = env(key);
        return value.isEmpty() ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
