# Advanced Java Logging Framework for IBM BAW

Welcome to the Advanced Java Logging Framework for IBM BAW. This project is a robust, flexible, and high-performance logging solution built on top of SLF4J and Logback, specifically tailored for IBM Business Automation Workflow (IBM BAW) environments. It addresses modern enterprise logging challenges with features like lazy initialization, auto-creation of loggers, environment-driven configuration, dynamic file logging, and proactive email notifications.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Core Design Principles](#core-design-principles)
- [Requirements](#requirements)
- [Installation & Setup](#installation--setup)
- [Usage](#usage)
- [Configuration](#configuration)
- [Real-World Examples](#real-world-examples)
- [About the Used Libraries](#about-the-used-libraries)
- [License](#license)

## Overview

This logging framework was developed to meet the demanding requirements of IBM BAW environments. It tackles critical logging challenges by implementing:

* Lazy Initialization: Logger instances are created only when needed, reducing startup overhead.
* Auto-Creation of Loggers: Loggers are automatically generated for each application, eliminating manual configuration.
* Environment-Driven Configuration: All settings (log paths, patterns, email configurations) are controlled via environment variables instead of XML files.
* Thread Safety & Performance: Advanced concurrency mechanisms, cached logger instances, and minimal synchronization ensure efficient operation.
* Context Management with MDC: Each log message is enriched with contextual data (such as the IBM BAW application name) to simplify troubleshooting.
* Proactive Alerting: Critical events trigger email notifications automatically.
* Runtime Monitoring: Real-time metrics provide insights into log volume and overall system health.

## Features

* Centralized Configuration: Configure loggers using ApplicationLoggerFactory and LoggerOptions.
* Lazy Initialization: Loggers are created on demand, ensuring efficient resource utilization.
* Auto-Creation of Loggers: Automatically generates and configures loggers for each IBM BAW application.
* Environment-Driven Settings: All configurations are driven by environment variables, streamlining deployment.
* Dynamic File Logging: Uses a dynamic rolling file appender (DynamicAppender) for per-application and per-level logging.
* Email Notifications: Automatically send email alerts for critical log events using EmailAppender and EmailService.
* Context Management with MDC: Enrich log messages with application-specific context using MDCConfig.
* Runtime Monitoring: Track and retrieve logging metrics (event count, log bytes) in JSON format using LoggerMonitor.

## Core Design Principles

### Lazy Initialization
Logger instances are created on demand, reducing startup overhead and conserving memory.

### Auto-Creation of Loggers
Loggers are automatically generated and configured for each application, reducing manual configuration tasks.

### Environment-Driven Configuration
Configurations are sourced from environment variables, making the framework highly adaptable across different deployment environments.

### Thread Safety
Uses ConcurrentHashMap, synchronized methods, and thread-local storage to ensure safe operation in multi-threaded environments.

### Performance Optimization
Cached logger instances and minimal synchronization points keep performance overhead low.

### Robust Error Handling
Input validation and clear exception messages help catch misconfigurations early.

### Context Management with MDC
Utilizes MDC to attach contextual data to log messages, simplifying debugging and analysis.

## Requirements

* Java 8 or above
* IBM Business Automation Workflow (IBM BAW) environment
* SLF4J API
* Logback (classic and core)
* JavaMail API (for email notifications)
* Gson (for JSON reporting in metrics)


## Usage

To use the framework in your IBM BAW application, simply obtain an instance of the logger through the factory:

```java
import com.atanu.logging.ApplicationLoggerFactory;
import com.atanu.logging.ExtendedLogger;

public class MyBAWApp {
    private static final ExtendedLogger logger = ApplicationLoggerFactory.getLogger("MyBAWApp");

    public static void main(String[] args) {
        logger.info("IBM BAW Application started.");
        // Use logger.errorWithMail() for critical errors that need email notifications
    }
}
```

## Configuration

All configurations are driven by environment variables rather than XML files. For example, `LOG_PATH` and `LOG_PATTERN` determine where and how logs are stored and formatted. Email configurations (SMTP settings, sender, recipient addresses) are also controlled via environment variables. Additionally, you can customize logging settings programmatically using the `LoggerOptions.Builder` class if needed.

## Real-World Examples

### Example 1: Basic Logger Initialization

```java
import com.atanu.logging.ApplicationLoggerFactory;
import com.atanu.logging.ExtendedLogger;

public class MyBAWApp {
    private static final ExtendedLogger logger = ApplicationLoggerFactory.getLogger("MyBAWApp");

    public static void main(String[] args) {
        logger.info("IBM BAW Application started.");
    }
}
```

### Example 2: Custom Logger with Email Alerts

```java
import com.atanu.logging.ApplicationLoggerFactory;
import com.atanu.logging.ExtendedLogger;
import com.atanu.logging.LoggerOptions;

public class MyBAWApp {
    private static final ExtendedLogger logger = ApplicationLoggerFactory.getLogger(
        new LoggerOptions.Builder("MyBAWApp")
            .logPath(System.getenv().getOrDefault("LOG_PATH", "/var/log/mybawapp"))
            .maxFileSize("20MB")
            .totalSizeCap("10GB")
            .logPattern(System.getenv().getOrDefault("LOG_PATTERN", "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n"))
            .enableEmail(true)
            .smtpHost(System.getenv("SMTP_HOST"))
            .smtpPort(Integer.parseInt(System.getenv().getOrDefault("SMTP_PORT", "587")))
            .smtpUsername(System.getenv("SMTP_USERNAME"))
            .smtpPassword(System.getenv("SMTP_PASSWORD"))
            .emailFrom(System.getenv("EMAIL_FROM"))
            .emailTo(System.getenv("EMAIL_TO"))
            .emailSubject("MyBAWApp Error Notification")
            .build()
    );

    public static void main(String[] args) {
        logger.info("IBM BAW Application started with custom settings.");
        logger.errorWithMail("A critical error occurred in MyBAWApp!");
    }
}
```

### Example 3: Accessing Runtime Metrics

```java
import com.atanu.logging.LoggerMonitor;

public class MetricsDemo {
    public static void main(String[] args) {
        // Simulate logging events...
        String allMetrics = LoggerMonitor.getAllLoggerMetricsAsJson();
        System.out.println("Logger Metrics: " + allMetrics);
        
        String totalLogCount = LoggerMonitor.getTotalLogEventCountAsJson();
        System.out.println("Total Log Event Count: " + totalLogCount);
    }
}
```

### Example 4: Contextual Logging with MDC

```java
import com.atanu.logging.MDCConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextDemo {
    private static final Logger logger = LoggerFactory.getLogger(ContextDemo.class);

    public static void main(String[] args) {
        MDCConfig.setApplicationName("MyBAWApp");
        logger.info("This log entry includes the IBM BAW application name in its context.");
        MDCConfig.clear();
    }
}
```

## About the Used Libraries

### SLF4J (Simple Logging Facade for Java)
SLF4J provides a uniform logging API that decouples your code from the underlying logging implementation. It makes it easier to swap out logging frameworks without changing your code.

### Logback
Logback is the native implementation for SLF4J. Known for its high performance and flexibility, Logback supports dynamic configurations (driven by environment variables in this framework), advanced filtering, custom appenders (like DynamicAppender and EmailAppender), and robust MDC support. Its design is ideal for high-demand environments such as IBM BAW.

## License

This project is licensed under the MIT License.

**Repository:** SLF4BAW

Happy logging and best of luck with your IBM BAW projects!
