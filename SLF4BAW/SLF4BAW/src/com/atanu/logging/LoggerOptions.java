package com.atanu.logging;

/**
 * Configuration class that encapsulates various logging options such as application name, log path, file sizes, log patterns, and email settings.
 */
public class LoggerOptions {
    private final String appName;
    private final String logPath;
    private final String maxFileSize;
    private final String totalSizeCap;
    private final String logPattern;

    // Email configurations
    private final boolean emailEnabled;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String emailFrom;
    private final String emailTo;
    private final String emailSubject;

    private LoggerOptions(Builder builder) {
        this.appName = builder.appName;
        this.logPath = builder.logPath;
        this.maxFileSize = builder.maxFileSize;
        this.totalSizeCap = builder.totalSizeCap;
        this.logPattern = builder.logPattern;
        this.emailEnabled = builder.emailEnabled;
        this.smtpHost = builder.smtpHost;
        this.smtpPort = builder.smtpPort;
        this.smtpUsername = builder.smtpUsername;
        this.smtpPassword = builder.smtpPassword;
        this.emailFrom = builder.emailFrom;
        this.emailTo = builder.emailTo;
        this.emailSubject = builder.emailSubject;
    }

    // Getters for existing fields
    public String getAppName() {
        return appName;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public String getTotalSizeCap() {
        return totalSizeCap;
    }

    public String getLogPattern() {
        return logPattern;
    }

    // Getters for new email fields
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    @Override
    public String toString() {
        return "LoggerOptions{" +
                "appName='" + appName + '\'' +
                ", logPath='" + logPath + '\'' +
                ", maxFileSize='" + maxFileSize + '\'' +
                ", totalSizeCap='" + totalSizeCap + '\'' +
                ", logPattern='" + logPattern + '\'' +
                ", emailEnabled=" + emailEnabled +
                ", smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", smtpUsername='" + smtpUsername + '\'' +
                ", smtpPassword='******'" + // Masked for security
                ", emailFrom='" + emailFrom + '\'' +
                ", emailTo='" + emailTo + '\'' +
                ", emailSubject='" + emailSubject + '\'' +
                '}';
    }

    /**
     * Builder class for constructing LoggerOptions instances with optional email configurations.
     */
    public static class Builder {
        private final String appName;
        private String logPath;
        private String maxFileSize = "10MB";
        private String totalSizeCap = "4GB";
        private String logPattern;
        
        // Email configurations
        private boolean emailEnabled = false;
        private String smtpHost;
        private int smtpPort = 25; // Default SMTP port
        private String smtpUsername;
        private String smtpPassword;
        private String emailFrom;
        private String emailTo;
        private String emailSubject = "Application Error Notification";

        /**
         * Initializes the Builder with the required application name.
         *
         * @param appName the name of the application
         */
        public Builder(String appName) {
            if (appName == null || appName.trim().isEmpty()) {
                throw new IllegalArgumentException("Application name cannot be null or empty");
            }
            this.appName = appName;
        }

        /**
         * Sets the log path for the application.
         *
         * @param logPath the log path
         * @return the Builder instance
         */
        public Builder logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        /**
         * Sets the maximum file size before rolling over.
         *
         * @param maxFileSize the maximum file size (e.g., "10MB")
         * @return the Builder instance
         */
        public Builder maxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        /**
         * Sets the total size cap for all log files.
         *
         * @param totalSizeCap the total size cap (e.g., "4GB")
         * @return the Builder instance
         */
        public Builder totalSizeCap(String totalSizeCap) {
            this.totalSizeCap = totalSizeCap;
            return this;
        }

        /**
         * Sets the log pattern for formatting log messages.
         *
         * @param logPattern the log pattern
         * @return the Builder instance
         */
        public Builder logPattern(String logPattern) {
            this.logPattern = logPattern;
            return this;
        }

        /**
         * Enables the email feature.
         *
         * @param enabled true to enable email notifications
         * @return the Builder instance
         */
        public Builder enableEmail(boolean enabled) {
            this.emailEnabled = enabled;
            return this;
        }

        /**
         * Sets the SMTP host for sending emails.
         *
         * @param smtpHost the SMTP server host
         * @return the Builder instance
         */
        public Builder smtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
            return this;
        }

        /**
         * Sets the SMTP port for sending emails.
         *
         * @param smtpPort the SMTP server port
         * @return the Builder instance
         */
        public Builder smtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
            return this;
        }

        /**
         * Sets the SMTP username for authentication.
         *
         * @param smtpUsername the SMTP username
         * @return the Builder instance
         */
        public Builder smtpUsername(String smtpUsername) {
            this.smtpUsername = smtpUsername;
            return this;
        }

        /**
         * Sets the SMTP password for authentication.
         *
         * @param smtpPassword the SMTP password
         * @return the Builder instance
         */
        public Builder smtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
            return this;
        }

        /**
         * Sets the "from" email address for notifications.
         *
         * @param emailFrom the sender's email address
         * @return the Builder instance
         */
        public Builder emailFrom(String emailFrom) {
            this.emailFrom = emailFrom;
            return this;
        }

        /**
         * Sets the "to" email address for notifications.
         *
         * @param emailTo the recipient's email address
         * @return the Builder instance
         */
        public Builder emailTo(String emailTo) {
            this.emailTo = emailTo;
            return this;
        }

        /**
         * Sets the email subject for notifications.
         *
         * @param emailSubject the email subject
         * @return the Builder instance
         */
        public Builder emailSubject(String emailSubject) {
            this.emailSubject = emailSubject;
            return this;
        }

        /**
         * Builds and returns a LoggerOptions instance with the configured settings.
         *
         * @return the constructed LoggerOptions
         */
        public LoggerOptions build() {
            if (emailEnabled) {
                if (smtpHost == null || smtpHost.trim().isEmpty()) {
                    throw new IllegalArgumentException("SMTP host must be provided when email is enabled");
                }
                if (emailFrom == null || emailFrom.trim().isEmpty()) {
                    throw new IllegalArgumentException("Email 'from' address must be provided when email is enabled");
                }
                if (emailTo == null || emailTo.trim().isEmpty()) {
                    throw new IllegalArgumentException("Email 'to' address must be provided when email is enabled");
                }
            }
            return new LoggerOptions(this);
        }
    }
}
