package com.SEGroup.Infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerWrapper {
    private static final Logger logger = Logger.getLogger(LoggerWrapper.class.getName());

    public static void info(String message) {
        logger.log(Level.INFO, message);
    }

    public static void warning(String message) {
        logger.log(Level.WARNING, message);
    }

    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    public static void debug(String message) {
        logger.log(Level.FINE, message);
    }
}