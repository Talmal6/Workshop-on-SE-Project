package com.SEGroup.Infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper around the Java Logger class for logging messages at different log levels.
 * This class provides methods to log messages at INFO, WARNING, ERROR, and DEBUG levels.
 */
public class LoggerWrapper {

    // Logger instance for logging messages
    private static final Logger logger = Logger.getLogger(LoggerWrapper.class.getName());

    /**
     * Logs an informational message.
     *
     * @param message The message to log at the INFO level.
     */
    public static void info(String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log at the WARNING level.
     */
    public static void warning(String message) {
        logger.log(Level.WARNING, message);
    }

    /**
     * Logs an error message along with the associated throwable (exception).
     *
     * @param message The error message to log at the SEVERE level.
     * @param throwable The throwable (exception) associated with the error.
     */
    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs a debug message.
     *
     * @param message The message to log at the FINE level.
     */
    public static void debug(String message) {
        logger.log(Level.FINE, message);
    }
}
