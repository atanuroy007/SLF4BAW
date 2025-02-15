package com.atanu.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Logback appender that dynamically manages RollingFileAppenders based on application name and log level.
 */
public class DynamicAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DynamicAppender.class);

    private final Map<String, Map<Level, RollingFileAppender<ILoggingEvent>>> appenders = new ConcurrentHashMap<>();

    private Encoder<ILoggingEvent> encoder;
    private String logPath;
    private String maxFileSize = "10MB";
    private String totalSizeCap = "4GB";

    /**
     * Starts the DynamicAppender by initializing the log path.
     */
    @Override
    public void start() {
        if (logPath == null || logPath.isEmpty()) {
            logPath = System.getProperty("LOG_PATH", System.getProperty("user.install.root") + "/logs/CustomLogger");
        }

        logger.info("DynamicAppender: Starting with logPath = {}", logPath);
        super.start();
    }

    /**
     * Appends a logging event by delegating to the appropriate RollingFileAppender based on application name and log level.
     *
     * @param event the logging event
     */
    @Override
    protected void append(ILoggingEvent event) {
        String appName = event.getLoggerName();

        if (appName == null || appName.isEmpty()) {
            logger.warn("DynamicAppender: No application name found; skipping log event.");
            return;
        }

        LoggerMonitor.registerLogger(appName);
        LoggerMonitor.trackLogEvent(appName, event.getFormattedMessage().getBytes().length);

        logger.debug("DynamicAppender: append -> appName={}, level={}", appName, event.getLevel());

        Level level = event.getLevel();
        RollingFileAppender<ILoggingEvent> appender = getAppenderForLevel(appName, level);

        if (appender != null) {
            appender.doAppend(event);
        } else {
            logger.warn("DynamicAppender: No appender found for appName={} and level={}", appName, level);
        }
    }

    /**
     * Retrieves or creates a RollingFileAppender for the specified application and log level.
     *
     * @param appName the name of the application
     * @param level   the log level
     * @return the RollingFileAppender instance
     */
    private RollingFileAppender<ILoggingEvent> getAppenderForLevel(String appName, Level level) {
        Map<Level, RollingFileAppender<ILoggingEvent>> levelAppenders =
                appenders.computeIfAbsent(appName, k -> new ConcurrentHashMap<>());

        RollingFileAppender<ILoggingEvent> fileAppender = levelAppenders.get(level);
        if (fileAppender == null) {
            fileAppender = createAppender(appName, level);
            if (fileAppender != null) {
                levelAppenders.put(level, fileAppender);
            }
        }
        return fileAppender;
    }

    /**
     * Creates a new RollingFileAppender for the specified application and log level.
     *
     * @param appName the name of the application
     * @param level   the log level
     * @return the created RollingFileAppender, or null if creation fails
     */
    private RollingFileAppender<ILoggingEvent> createAppender(String appName, Level level) {
        try {
            String appLogPath = Paths.get(logPath, appName).toString();
            File appFolder = new File(appLogPath);

            if (!appFolder.exists() && !appFolder.mkdirs()) {
                logger.error("DynamicAppender: Failed to create directory {}", appLogPath);
                return null;
            }

            LoggerContext context = (LoggerContext) getContext();
            RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
            fileAppender.setContext(context);

            String logFileName = Paths.get(appLogPath, level.toString().toLowerCase() + ".log").toString();
            fileAppender.setFile(logFileName);

            logger.debug("DynamicAppender: Creating RollingFileAppender for {}", logFileName);

            SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
            rollingPolicy.setContext(context);
            rollingPolicy.setParent(fileAppender);
            rollingPolicy.setFileNamePattern(Paths.get(appLogPath, level.toString().toLowerCase() + ".%d{yyyy-MM-dd}.%i.log").toString());
            rollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
            rollingPolicy.setTotalSizeCap(FileSize.valueOf(totalSizeCap));
            rollingPolicy.setMaxHistory(90);
            rollingPolicy.start();

            if (encoder == null) {
                logger.error("DynamicAppender: Encoder is not initialized!");
                return null;
            }

            if (!encoder.isStarted()) {
                encoder.start();
            }

            fileAppender.setEncoder(encoder);
            fileAppender.setRollingPolicy(rollingPolicy);
            fileAppender.start();

            logger.info("DynamicAppender: Successfully created appender for appName={}, level={}", appName, level);
            return fileAppender;

        } catch (Exception e) {
            logger.error("DynamicAppender: Failed to create appender for appName={}, level={}", appName, level, e);
            return null;
        }
    }

    /**
     * Sets the encoder for the DynamicAppender.
     *
     * @param encoder the Encoder instance
     */
    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    /**
     * Sets the log path where log files will be stored.
     *
     * @param logPath the log path
     */
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    /**
     * Sets the maximum file size before rolling over.
     *
     * @param maxFileSize the maximum file size (e.g., "10MB")
     */
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Sets the total size cap for all log files.
     *
     * @param totalSizeCap the total size cap (e.g., "4GB")
     */
    public void setTotalSizeCap(String totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    /**
     * Removes and stops all appenders associated with the specified application.
     *
     * @param appName the name of the application
     */
    public void removeAppendersForApp(String appName) {
        Map<Level, RollingFileAppender<ILoggingEvent>> levelAppenders = appenders.remove(appName);
        if (levelAppenders != null) {
            levelAppenders.values().forEach(appender -> {
                appender.stop();
                ((LoggerContext) getContext()).getLogger(appName).detachAppender(appender);
                logger.info("DynamicAppender: Removed appender for appName={}, level={}", appName, appender.getName());
            });
        }
    }
}
