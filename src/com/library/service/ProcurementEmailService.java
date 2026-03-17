package com.library.service;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.file.Path;
import java.util.Properties;

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
        if (config.smtpHost == null || config.smtpHost.trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP host is required.");
        }
        if (config.username == null || config.username.trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP username is required.");
        }
        if (config.password == null || config.password.trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP password is required.");
        }

        String from = isBlank(config.from) ? config.username.trim() : config.from.trim();

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", config.smtpHost.trim());
            props.put("mail.smtp.port", String.valueOf(config.smtpPort));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.username.trim(), config.password.trim());
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            addRecipientIfPresent(message, Message.RecipientType.TO, companyMail);
            addRecipientIfPresent(message, Message.RecipientType.CC, contactPersonMail);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body == null ? "" : body);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(pdfPath.toFile());
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(pdfPath.getFileName().toString());

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setSubject(subject == null ? "LMS Receipt" : subject);
            message.setContent(multipart);

            Transport.send(message);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to send receipt email: " + e.getMessage(), e);
        }
    }

    private void addRecipientIfPresent(MimeMessage message, Message.RecipientType type, String email) throws Exception {
        if (isBlank(email)) return;
        message.addRecipients(type, InternetAddress.parse(email.trim()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
