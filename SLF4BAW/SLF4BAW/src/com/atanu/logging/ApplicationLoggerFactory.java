package com.atanu.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for creating and managing Logger instances with specific configurations.
 */
public class ApplicationLoggerFactory {
    private static final Logger factoryLogger = LoggerFactory.getLogger(ApplicationLoggerFactory.class);
    private static final Map<String, LoggerOptions> APP_LOGGER_CACHE = new ConcurrentHashMap<>();

    /**
     * Retrieves an ExtendedLogger for the given application name with default configurations.
     *
     * @param appName the name of the application
     * @return the ExtendedLogger instance
     */
    public static ExtendedLogger getLogger(String appName) {
        MDCConfig.setApplicationName(appName);
        return getLogger(new LoggerOptions.Builder(appName).build());
    }

    /**
     * Retrieves an ExtendedLogger for the given application with specified configurations.
     *
     * @param appName      the name of the application
     * @param logPath      the path where logs should be stored
     * @param maxFileSize  the maximum size of a log file before rolling over
     * @param totalSizeCap the total size cap for all log files
     * @param logPattern   the pattern to use for log formatting
     * @param emailEnabled whether to enable email notifications
     * @param smtpHost     SMTP host for email
     * @param smtpPort     SMTP port for email
     * @param smtpUsername SMTP username for email
     * @param smtpPassword SMTP password for email
     * @param emailFrom    Email 'from' address
     * @param emailTo      Email 'to' address
     * @param emailSubject Email subject
     * @return the ExtendedLogger instance
     */
    public static ExtendedLogger getLogger(String appName,
                                           String logPath,
                                           String maxFileSize,
                                           String totalSizeCap,
                                           String logPattern,
                                           boolean emailEnabled,
                                           String smtpHost,
                                           int smtpPort,
                                           String smtpUsername,
                                           String smtpPassword,
                                           String emailFrom,
                                           String emailTo,
                                           String emailSubject) {
        LoggerOptions.Builder builder = new LoggerOptions.Builder(appName)
                .logPath(logPath)
                .maxFileSize(maxFileSize)
                .totalSizeCap(totalSizeCap)
                .logPattern(logPattern)
                .enableEmail(emailEnabled);
        
        if (emailEnabled) {
            builder
                    .smtpHost(smtpHost)
                    .smtpPort(smtpPort)
                    .smtpUsername(smtpUsername)
                    .smtpPassword(smtpPassword)
                    .emailFrom(emailFrom)
                    .emailTo(emailTo)
                    .emailSubject(emailSubject);
        }
        
        LoggerOptions options = builder.build();
        MDCConfig.setApplicationName(appName);
        return getLogger(options);
    }

    /**
     * Retrieves an ExtendedLogger based on the provided LoggerOptions.
     * Utilizes caching to reuse existing configurations when possible.
     *
     * @param options the LoggerOptions containing configuration details
     * @return the ExtendedLogger instance
     */
    public static ExtendedLogger getLogger(LoggerOptions options) {
        String appName = options.getAppName();
        if (appName == null || appName.trim().isEmpty()) {
            throw new IllegalArgumentException("Application name cannot be null or empty");
        }

        LoggerOptions existingOptions = APP_LOGGER_CACHE.get(appName);
        if (existingOptions != null && !isDifferentConfig(existingOptions, options)) {
            factoryLogger.debug("Using cached logger for app={}", appName);
            Logger existingLogger = LoggerFactory.getLogger(appName);
            return new ExtendedLogger(existingLogger);
        }

        synchronized (ApplicationLoggerFactory.class) {
            // Double-checked locking
            existingOptions = APP_LOGGER_CACHE.get(appName);
            if (existingOptions != null && !isDifferentConfig(existingOptions, options)) {
                factoryLogger.debug("Using cached logger for app={}", appName);
                Logger existingLogger = LoggerFactory.getLogger(appName);
                return new ExtendedLogger(existingLogger);
            }

            factoryLogger.info("Configuring new logger for app={}", appName);
            try {
                LoggerConfiguration.configureBase(options);
                LoggerConfiguration.configureForApp(appName);
                APP_LOGGER_CACHE.put(appName, options);
                factoryLogger.info("Logger configured successfully for app={}", appName);
            } catch (Exception e) {
                factoryLogger.error("Failed to configure logger for app={}", appName, e);
                throw new RuntimeException("Failed to configure logger for app: " + appName, e);
            }

            Logger newLogger = LoggerFactory.getLogger(appName);
            return new ExtendedLogger(newLogger);
        }
    }

    /**
     * Reloads the Logger configuration for the specified application.
     *
     * @param appName      the name of the application
     * @param logPath      the new log path
     * @param maxFileSize  the new maximum file size
     * @param totalSizeCap the new total size cap
     * @param logPattern   the new log pattern
     * @param emailEnabled whether to enable email notifications
     * @param smtpHost     SMTP host for email
     * @param smtpPort     SMTP port for email
     * @param smtpUsername SMTP username for email
     * @param smtpPassword SMTP password for email
     * @param emailFrom    Email 'from' address
     * @param emailTo      Email 'to' address
     * @param emailSubject Email subject
     */
    public static void reloadLogger(String appName,
                                    String logPath,
                                    String maxFileSize,
                                    String totalSizeCap,
                                    String logPattern,
                                    boolean emailEnabled,
                                    String smtpHost,
                                    int smtpPort,
                                    String smtpUsername,
                                    String smtpPassword,
                                    String emailFrom,
                                    String emailTo,
                                    String emailSubject) {
        factoryLogger.info("Reloading logger for application: {}", appName);
        factoryLogger.info("New configuration - LogPath: {}, MaxFileSize: {}, TotalSizeCap: {}, LogPattern: {}, EmailEnabled: {}",
                logPath, maxFileSize, totalSizeCap, logPattern, emailEnabled);

        try {
            // Remove old logger configuration for the application
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.getLogger(appName).detachAndStopAllAppenders(); // Detach and stop all previous appenders

            // Create new options for the logger
            LoggerOptions.Builder builder = new LoggerOptions.Builder(appName)
                    .logPath(logPath)
                    .maxFileSize(maxFileSize)
                    .totalSizeCap(totalSizeCap)
                    .logPattern(logPattern)
                    .enableEmail(emailEnabled);

            if (emailEnabled) {
                builder
                        .smtpHost(smtpHost)
                        .smtpPort(smtpPort)
                        .smtpUsername(smtpUsername)
                        .smtpPassword(smtpPassword)
                        .emailFrom(emailFrom)
                        .emailTo(emailTo)
                        .emailSubject(emailSubject);
            }

            LoggerOptions newOptions = builder.build();

            // Get the logger with new configuration
            getLogger(newOptions);

            factoryLogger.info("Logger for application {} successfully reloaded.", appName);
        } catch (Exception e) {
            factoryLogger.error("Error reloading logger for application {}", appName, e);
            throw new RuntimeException("Error reloading logger for application: " + appName, e);
        }
    }

    /**
     * Checks if the new LoggerOptions differ from the existing ones.
     *
     * @param oldOpts the existing LoggerOptions
     * @param newOpts the new LoggerOptions
     * @return true if configurations differ, false otherwise
     */
    private static boolean isDifferentConfig(LoggerOptions oldOpts, LoggerOptions newOpts) {
        if (oldOpts == null) {
            return true;
        }
        return !(safeEq(oldOpts.getLogPath(), newOpts.getLogPath())
                && safeEq(oldOpts.getMaxFileSize(), newOpts.getMaxFileSize())
                && safeEq(oldOpts.getTotalSizeCap(), newOpts.getTotalSizeCap())
                && safeEq(oldOpts.getLogPattern(), newOpts.getLogPattern())
                && oldOpts.isEmailEnabled() == newOpts.isEmailEnabled()
                && safeEq(oldOpts.getSmtpHost(), newOpts.getSmtpHost())
                && oldOpts.getSmtpPort() == newOpts.getSmtpPort()
                && safeEq(oldOpts.getSmtpUsername(), newOpts.getSmtpUsername())
                && safeEq(oldOpts.getSmtpPassword(), newOpts.getSmtpPassword())
                && safeEq(oldOpts.getEmailFrom(), newOpts.getEmailFrom())
                && safeEq(oldOpts.getEmailTo(), newOpts.getEmailTo())
                && safeEq(oldOpts.getEmailSubject(), newOpts.getEmailSubject()));
    }

    /**
     * Safely compares two strings, treating nulls as empty strings.
     *
     * @param s1 first string
     * @param s2 second string
     * @return true if equal, false otherwise
     */
    private static boolean safeEq(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        return s1.equals(s2);
    }
}