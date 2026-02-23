package com.library.service;

import java.nio.file.Path;

public class ProcurementEmailService {

    public static class MailConfig {
        public final String smtpHost;
        public final int smtpPort;
        public final String username;
        public final String password;
        public final String from;

        public MailConfig(String smtpHost, int smtpPort, String username, String password, String from) {
            this.smtpHost = smtpHost;
            this.smtpPort = smtpPort;
            this.username = username;
            this.password = password;
            this.from = from;
        }
    }

    public boolean sendReceiptPdfToSeller(
        String companyMail,
        String contactPersonMail,
        String subject,
        String body,
        Path pdfPath,
        MailConfig config
    ) {
        try {
            Class.forName("javax.mail.Session");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "JavaMail not configured. Add javax.mail/jakarta.mail dependency in lib before using email sending.",
                e
            );
        }

        if (config == null) {
            throw new IllegalArgumentException("Mail config is required.");
        }
        if (pdfPath == null) {
            throw new IllegalArgumentException("PDF path is required.");
        }

        // JavaMail integration point:
        // 1) Build Session with SMTP host/port/auth from config
        // 2) Create MimeMessage with from, companyMail and contactPersonMail as recipients
        // 3) Attach body text and PDF attachment (pdfPath)
        // 4) Transport.send(message)
        return false;
    }
}
