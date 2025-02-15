package com.atanu.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.Optional;

/**
 * Handles the configuration of the logging context, including encoder and appender initialization.
 */
class LoggerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(LoggerConfiguration.class);
    private static final String DEFAULT_LOG_PATH = System.getProperty("user.install.root") + "/logs/CustomLogger";
    private static final String DEFAULT_PATTERN = "[%d{M/d/yy HH:mm:ss:SSS z}] " +
            ManagementFactory.getRuntimeMXBean().getName() +
            " %thread %-5level %logger{36} - %msg%n";

    /**
     * Configures the base logging context with default settings.
     */
    static void configureBase() {
        logger.info("Configuring base logger with default settings.");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        String logPath = System.getProperty("LOG_PATH", DEFAULT_LOG_PATH);
        PatternLayoutEncoder encoder = createEncoder(context, DEFAULT_PATTERN);
        DynamicAppender dynamicAppender = createDynamicAppender(
                context, encoder, logPath, "10MB", "4GB");
        context.getLogger("ROOT").addAppender(dynamicAppender);
        logger.info("Base logger configured with logPath={}", logPath);
    }

    /**
     * Configures the base logging context based on provided LoggerOptions, including optional email appender.
     *
     * @param options the LoggerOptions containing configuration details
     */
    static void configureBase(LoggerOptions options) {
        logger.info("Configuring base logger with options: {}", options);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        String logPath = Optional.ofNullable(options.getLogPath())
                .orElse(System.getProperty("LOG_PATH", DEFAULT_LOG_PATH));
        String patternToUse = Optional.ofNullable(options.getLogPattern())
                .filter(p -> !p.isEmpty())
                .orElse(DEFAULT_PATTERN);
        String maxFileSize = Optional.ofNullable(options.getMaxFileSize())
                .orElse("10MB");
        String totalSizeCap = Optional.ofNullable(options.getTotalSizeCap())
                .orElse("4GB");

        PatternLayoutEncoder encoder = createEncoder(context, patternToUse);
        DynamicAppender dynamicAppender = createDynamicAppender(context, encoder, logPath, maxFileSize, totalSizeCap);
        context.getLogger("ROOT").addAppender(dynamicAppender);

        // Initialize EmailAppender if email is enabled
        if (options.isEmailEnabled()) {
            EmailAppender emailAppender = createEmailAppender(context, options);
            if (emailAppender != null) {
                context.getLogger("ROOT").addAppender(emailAppender);
                logger.info("EmailAppender configured and added for application={}", options.getAppName());
            } else {
                logger.warn("EmailAppender was not configured due to missing configurations.");
            }
        }

        logger.info("Base logger configured with logPath={}, maxFileSize={}, totalSizeCap={}",
                logPath, maxFileSize, totalSizeCap);
    }

    /**
     * Configures additional settings specific to an application.
     *
     * @param appName the name of the application
     */
    static void configureForApp(String appName) {
        MDCConfig.setApplicationName(appName);
        logger.debug("Configured MDC for application: {}", appName);
    }

    /**
     * Creates a PatternLayoutEncoder with the specified pattern.
     *
     * @param context the LoggerContext
     * @param pattern the log pattern
     * @return the configured PatternLayoutEncoder
     */
    private static PatternLayoutEncoder createEncoder(LoggerContext context, String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(pattern);
        encoder.start();
        logger.debug("PatternLayoutEncoder created with pattern: {}", pattern);
        return encoder;
    }

    /**
     * Creates and starts a DynamicAppender with the specified configurations.
     *
     * @param context      the LoggerContext
     * @param encoder      the Encoder for formatting log messages
     * @param logPath      the path where logs should be stored
     * @param maxFileSize  the maximum size of a log file before rolling over
     * @param totalSizeCap the total size cap for all log files
     * @return the started DynamicAppender
     */
    private static DynamicAppender createDynamicAppender(LoggerContext context,
                                                         PatternLayoutEncoder encoder,
                                                         String logPath,
                                                         String maxFileSize,
                                                         String totalSizeCap) {
        DynamicAppender dynamicAppender = new DynamicAppender();
        dynamicAppender.setContext(context);
        dynamicAppender.setEncoder(encoder);
        dynamicAppender.setLogPath(logPath);
        dynamicAppender.setMaxFileSize(maxFileSize);
        dynamicAppender.setTotalSizeCap(totalSizeCap);
        dynamicAppender.start();
        logger.debug("DynamicAppender created and started with logPath={}, maxFileSize={}, totalSizeCap={}",
                logPath, maxFileSize, totalSizeCap);
        return dynamicAppender;
    }

    /**
     * Creates and configures an EmailAppender based on LoggerOptions.
     *
     * @param context the LoggerContext
     * @param options the LoggerOptions containing email configurations
     * @return the configured EmailAppender, or null if configuration is incomplete
     */
    private static EmailAppender createEmailAppender(LoggerContext context, LoggerOptions options) {
        try {
            PatternLayoutEncoder emailEncoder = new PatternLayoutEncoder();
            emailEncoder.setContext(context);
            // Define a simple email pattern or allow customization
            emailEncoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n");
            emailEncoder.start();

            EmailAppender emailAppender = new EmailAppender();
            emailAppender.setContext(context);
            emailAppender.setEncoder(emailEncoder);
            emailAppender.setSmtpHost(options.getSmtpHost());
            emailAppender.setSmtpPort(options.getSmtpPort());
            emailAppender.setSmtpUsername(options.getSmtpUsername());
            emailAppender.setSmtpPassword(options.getSmtpPassword());
            emailAppender.setEmailFrom(options.getEmailFrom());
            emailAppender.setEmailTo(options.getEmailTo());
            emailAppender.setEmailSubject(options.getEmailSubject());
            emailAppender.start();

            logger.debug("EmailAppender created with SMTP host={}, port={}", options.getSmtpHost(), options.getSmtpPort());
            return emailAppender;
        } catch (Exception e) {
            logger.error("Failed to create EmailAppender due to missing or invalid configurations.", e);
            return null;
        }
    }
}