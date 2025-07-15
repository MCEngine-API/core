package io.github.mcengine.api.core.extension.api;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Logger utility for the MCEngine API, providing prefixed logging with a specific identifier.
 */
public class MCEngineAPILogger {

    /** The underlying logger instance obtained from the plugin. */
    private final Logger logger;

    /** The name used to prefix log messages for identification. */
    private final String loggerName;

    /**
     * Constructs a new API logger for the specified plugin and name.
     *
     * @param plugin     The plugin instance used to retrieve the logger.
     * @param loggerName The name to include in log message prefixes.
     */
    public MCEngineAPILogger(Plugin plugin, String loggerName) {
        this.logger = plugin.getLogger();
        this.loggerName = loggerName;
    }

    /**
     * Logs an informational message with the API prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info("[API] [" + loggerName + "] " + message);
    }

    /**
     * Logs a warning message with the API prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning("[API] [" + loggerName + "] " + message);
    }

    /**
     * Logs a severe error message with the API prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe("[API] [" + loggerName + "] " + message);
    }

    /**
     * Returns the underlying raw logger without any prefix formatting.
     * Useful when interfacing with APIs that expect a standard {@link Logger}.
     *
     * @return the raw {@link Logger} instance
     */
    public Logger getLogger() {
        return logger;
    }
}
