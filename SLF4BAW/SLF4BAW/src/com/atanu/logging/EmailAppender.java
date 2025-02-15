package com.atanu.logging;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Custom EmailAppender that sends emails based on log events with specific markers.
 */
public class EmailAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger logger = LoggerFactory.getLogger(EmailAppender.class);

    private String smtpHost;
    private int smtpPort = 25; // Default SMTP port
    private String smtpUsername;
    private String smtpPassword;
    private String emailFrom;
    private String emailTo;
    private String emailSubject = "Application Error Notification";

    private PatternLayoutEncoder encoder;

    /**
     * Starts the EmailAppender by validating configurations.
     */
    @Override
    public void start() {
        if (smtpHost == null || smtpHost.isEmpty()) {
            logger.error("EmailAppender: SMTP host is not set. Email feature will be disabled.");
            return;
        }
        if (emailFrom == null || emailFrom.isEmpty()) {
            logger.error("EmailAppender: Email 'from' address is not set. Email feature will be disabled.");
            return;
        }
        if (emailTo == null || emailTo.isEmpty()) {
            logger.error("EmailAppender: Email 'to' address is not set. Email feature will be disabled.");
            return;
        }
        if (encoder == null) {
            logger.error("EmailAppender: Encoder is not set. Email feature will be disabled.");
            return;
        }
        encoder.start();
        super.start();
        logger.info("EmailAppender: Started successfully.");
    }

    /**
     * Appends the log event by sending an email if the event contains the EMAIL marker.
     *
     * @param event the logging event
     */
    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }

        Marker emailMarker = LogMarkers.EMAIL;
        if (event.getMarker() != null && event.getMarker().contains(emailMarker)) {
            String formattedMessage = encoder.getLayout().doLayout(event);
            sendEmail(event.getLevel().toString(), formattedMessage);
        }
    }

    /**
     * Sends an email with the log event details.
     *
     * @param level   the log level
     * @param message the formatted log message
     */
    private void sendEmail(String level, String message) {
        // Implement email sending logic using JavaMail API
        EmailService emailService = new EmailService(
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword
        );

        try {
            emailService.sendEmail(emailFrom, emailTo, emailSubject + " - " + level, message);
            logger.debug("EmailAppender: Sent email for log level {}", level);
        } catch (Exception e) {
            logger.error("EmailAppender: Failed to send email for log level {}", level, e);
        }
    }

    /**
     * Sets the SMTP host.
     *
     * @param smtpHost the SMTP server host
     */
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    /**
     * Sets the SMTP port.
     *
     * @param smtpPort the SMTP server port
     */
    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    /**
     * Sets the SMTP username.
     *
     * @param smtpUsername the SMTP username
     */
    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    /**
     * Sets the SMTP password.
     *
     * @param smtpPassword the SMTP password
     */
    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    /**
     * Sets the email 'from' address.
     *
     * @param emailFrom the sender's email address
     */
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    /**
     * Sets the email 'to' address.
     *
     * @param emailTo the recipient's email address
     */
    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    /**
     * Sets the email subject.
     *
     * @param emailSubject the email subject
     */
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    /**
     * Sets the PatternLayoutEncoder for formatting email content.
     *
     * @param encoder the PatternLayoutEncoder instance
     */
    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }
}