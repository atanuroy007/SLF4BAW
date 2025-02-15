package com.atanu.logging;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Monitors and tracks logging metrics such as total log events and bytes per application.
 */
public class LoggerMonitor {
    private static final Map<String, LoggerMetrics> LOGGER_METRICS = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    /**
     * Inner class representing metrics for a specific logger.
     */
    public static class LoggerMetrics {
        private final String appName;
        private final long creationTimestamp;
        private final AtomicLong totalLogEvents = new AtomicLong(0);
        private final AtomicLong totalLogBytes = new AtomicLong(0);

        /**
         * Initializes LoggerMetrics for the specified application.
         *
         * @param appName the name of the application
         */
        public LoggerMetrics(String appName) {
            this.appName = appName;
            this.creationTimestamp = System.currentTimeMillis();
        }

        /**
         * Increments the log event count and adds to the total log bytes.
         *
         * @param logSize the size of the log event in bytes
         */
        public void incrementLogEvent(int logSize) {
            totalLogEvents.incrementAndGet();
            totalLogBytes.addAndGet(logSize);
        }

        public String getAppName() {
            return appName;
        }

        public long getCreationTimestamp() {
            return creationTimestamp;
        }

        public long getTotalLogEvents() {
            return totalLogEvents.get();
        }

        public long getTotalLogBytes() {
            return totalLogBytes.get();
        }
    }

    /**
     * Registers a logger for the specified application if not already registered.
     *
     * @param appName the name of the application
     */
    public static void registerLogger(String appName) {
        LOGGER_METRICS.putIfAbsent(appName, new LoggerMetrics(appName));
    }

    /**
     * Retrieves all logger metrics in JSON format.
     *
     * @return JSON string representing all logger metrics
     */
    public static String getAllLoggerMetricsAsJson() {
        Map<String, Map<String, Object>> metricsMap = LOGGER_METRICS.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            LoggerMetrics metrics = entry.getValue();
                            Map<String, Object> metricDetails = new HashMap<>();
                            metricDetails.put("creationTimestamp", metrics.getCreationTimestamp());
                            metricDetails.put("totalLogEvents", metrics.getTotalLogEvents());
                            metricDetails.put("totalLogBytes", metrics.getTotalLogBytes());
                            return metricDetails;
                        }
                ));
        return gson.toJson(metricsMap);
    }

    /**
     * Retrieves logger metrics for a specific application in JSON format.
     *
     * @param appName the name of the application
     * @return JSON string representing the logger metrics, or null if not found
     */
    public static String getLoggerMetricsAsJson(String appName) {
        LoggerMetrics metrics = LOGGER_METRICS.get(appName);
        if (metrics == null) return null;

        Map<String, Object> metricDetails = new HashMap<>();
        metricDetails.put("appName", metrics.getAppName());
        metricDetails.put("creationTimestamp", metrics.getCreationTimestamp());
        metricDetails.put("totalLogEvents", metrics.getTotalLogEvents());
        metricDetails.put("totalLogBytes", metrics.getTotalLogBytes());

        return gson.toJson(metricDetails);
    }

    /**
     * Retrieves the total number of loggers being tracked in JSON format.
     *
     * @return JSON string representing the total logger count
     */
    public static String getTotalLoggerCountAsJson() {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("totalLoggerCount", LOGGER_METRICS.size());
        return gson.toJson(countMap);
    }

    /**
     * Retrieves the total number of log events across all loggers in JSON format.
     *
     * @return JSON string representing the total log event count
     */
    public static String getTotalLogEventCountAsJson() {
        long totalEvents = LOGGER_METRICS.values().stream()
                .mapToLong(LoggerMetrics::getTotalLogEvents)
                .sum();
        Map<String, Long> eventMap = new HashMap<>();
        eventMap.put("totalLogEventCount", totalEvents);
        return gson.toJson(eventMap);
    }

    /**
     * Retrieves the total number of log bytes across all loggers in JSON format.
     *
     * @return JSON string representing the total log bytes
     */
    public static String getTotalLogBytesAsJson() {
        long totalBytes = LOGGER_METRICS.values().stream()
                .mapToLong(LoggerMetrics::getTotalLogBytes)
                .sum();
        Map<String, Long> bytesMap = new HashMap<>();
        bytesMap.put("totalLogBytes", totalBytes);
        return gson.toJson(bytesMap);
    }

    /**
     * Tracks a log event by incrementing the corresponding metrics.
     *
     * @param appName the name of the application
     * @param logSize the size of the log event in bytes
     */
    public static void trackLogEvent(String appName, int logSize) {
        LOGGER_METRICS.computeIfAbsent(appName, LoggerMetrics::new).incrementLogEvent(logSize);
    }
}
