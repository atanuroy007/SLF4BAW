package com.atanu.logging;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Service class responsible for sending emails using JavaMail API.
 */
public class EmailService {
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;

    /**
     * Initializes the EmailService with SMTP configurations.
     *
     * @param smtpHost     the SMTP server host
     * @param smtpPort     the SMTP server port
     * @param smtpUsername the SMTP username (optional)
     * @param smtpPassword the SMTP password (optional)
     */
    public EmailService(String smtpHost, int smtpPort, String smtpUsername, String smtpPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    /**
     * Sends an email with the specified parameters.
     *
     * @param from    the sender's email address
     * @param to      the recipient's email address
     * @param subject the email subject
     * @param body    the email body
     * @throws MessagingException if sending the email fails
     */
    public void sendEmail(String from, String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", smtpUsername != null && !smtpUsername.isEmpty());
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        Session session;
        if (smtpUsername != null && !smtpUsername.isEmpty()) {
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            };
            session = Session.getInstance(props, auth);
        } else {
            session = Session.getInstance(props);
        }

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Set email content
        message.setText(body);

        Transport.send(message);
    }
}