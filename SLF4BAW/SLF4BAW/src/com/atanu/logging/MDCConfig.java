package com.atanu.logging;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the Mapped Diagnostic Context (MDC) for setting and clearing the application name in the logging context.
 */
class MDCConfig {
    private static final String APP_NAME_KEY = "applicationName";
    private static final Logger logger = LoggerFactory.getLogger(MDCConfig.class);

    /**
     * Sets the application name in the MDC.
     *
     * @param appName the name of the application
     */
    static void setApplicationName(String appName) {
        MDC.put(APP_NAME_KEY, appName);
        logger.debug("MDCConfig: Set applicationName to {}", appName);
    }

    /**
     * Clears the application name from the MDC.
     */
    static void clear() {
        MDC.remove(APP_NAME_KEY);
        logger.debug("MDCConfig: Cleared applicationName from MDC");
    }
}
