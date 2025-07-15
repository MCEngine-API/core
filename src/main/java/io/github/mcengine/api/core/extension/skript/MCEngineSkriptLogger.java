package io.github.mcengine.api.core.extension.api;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Logger utility for Skript integrations, providing prefixed logging for Skript-related modules.
 */
public class MCEngineSkriptLogger {

    /** The underlying logger instance obtained from the plugin. */
    private final Logger logger;

    /** The name used to prefix log messages for identification. */
    private final String skriptName;

    /**
     * Constructs a new Skript logger for the specified plugin and Skript module name.
     *
     * @param plugin      The plugin instance used to retrieve the logger.
     * @param skriptName  The name to include in log message prefixes.
     */
    public MCEngineSkriptLogger(Plugin plugin, String skriptName) {
        this.logger = plugin.getLogger();
        this.skriptName = skriptName;
    }

    /**
     * Logs an informational message with the Skript prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info("[Skript] [" + skriptName + "] " + message);
    }

    /**
     * Logs a warning message with the Skript prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning("[Skript] [" + skriptName + "] " + message);
    }

    /**
     * Logs a severe error message with the Skript prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe("[Skript] [" + skriptName + "] " + message);
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
