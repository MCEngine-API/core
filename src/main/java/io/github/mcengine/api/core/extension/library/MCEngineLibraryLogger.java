package io.github.mcengine.api.core.extension.api;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Logger utility for MCEngine libraries, providing prefixed logging for a specific library component.
 */
public class MCEngineLibraryLogger {

    /** The underlying logger instance obtained from the plugin. */
    private final Logger logger;

    /** The name used to prefix log messages for identification. */
    private final String libraryName;

    /**
     * Constructs a new library logger for the specified plugin and library name.
     *
     * @param plugin      The plugin instance used to retrieve the logger.
     * @param libraryName The name of the library component to include in log message prefixes.
     */
    public MCEngineLibraryLogger(Plugin plugin, String libraryName) {
        this.logger = plugin.getLogger();
        this.libraryName = libraryName;
    }

    /**
     * Logs an informational message with the library prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info("[Library] [" + libraryName + "] " + message);
    }

    /**
     * Logs a warning message with the library prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning("[Library] [" + libraryName + "] " + message);
    }

    /**
     * Logs a severe error message with the library prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe("[Library] [" + libraryName + "] " + message);
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
