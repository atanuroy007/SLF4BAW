package com.atanu.logging.test;

import org.slf4j.Logger;

import com.atanu.logging.ApplicationLoggerFactory;
import com.atanu.logging.ExtendedLogger;

public class LoggerTest {
    public static void main(String[] args) {
        // Initialize logger without email notifications
    	ExtendedLogger fileLogger = ApplicationLoggerFactory.getLogger(
    		    "ALF",                 // appName
    		    "C:/tmp/NEW_LOGS",       // logPath
    		    null,                     // maxFileSize (uses default)
    		    null,                     // totalSizeCap (uses default)
    		    null,                     // logPattern (uses default)
    		    false,                    // emailEnabled
    		    null,                     // smtpHost
    		    0,                        // smtpPort (default port if not used)
    		    null,                     // smtpUsername
    		    null,                     // smtpPassword
    		    null,                     // emailFrom
    		    null,                     // emailTo
    		    null                      // emailSubject
    		);

        
        // Logging to file only
        fileLogger.info("Application started without email notifications.");
        fileLogger.debug("Debugging application without email.");
        fileLogger.error("An error occurred without email notification.");
        
        
        
        // Initialize logger with email notifications
        ExtendedLogger emailLogger = ApplicationLoggerFactory.getLogger("MyAppWithEmail",
                "C:/tmp/NEW_LOGS",
                "20MB",
                "10GB",
                "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n",
                true, // Email enabled
                "localhost", // SMTP host
                587, // SMTP port
                "test@local.com", // SMTP username
                "test", // SMTP password
                "help@local.com", // Email from
                "test@local.com", // Email to
                "Critical Error in MyAppWithEmail"); // Email subject

        // Logging to file only
        emailLogger.info("Application started with email notifications.");
        emailLogger.debug("Debugging application with email.");
        emailLogger.error("An error occurred with email notification.");

        // Logging to both file and email
        emailLogger.infoWithMail("Informational message that triggers an email.");
        emailLogger.debugWithMail("Debug message that triggers an email.");
        emailLogger.errorWithMail("Error message that triggers an email.");
        
    }
}
