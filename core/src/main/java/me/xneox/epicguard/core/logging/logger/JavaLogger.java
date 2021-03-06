package me.xneox.epicguard.core.logging.logger;

import me.xneox.epicguard.core.logging.GuardLogger;

import java.util.logging.Logger;

/**
 * GuardLogger implementation for platforms using the {@link java.util.logging.Logger}
 */
public class JavaLogger implements GuardLogger {
    private final Logger logger;

    public JavaLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message) {
        this.logger.info(message);
    }

    @Override
    public void warning(String message) {
        this.logger.warning(message);
    }
}
