package io.github.mcengine.api.core.extension.dlc;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Logger utility for DLCs, providing prefixed logging for a specific DLC name.
 */
public class MCEngineDLCLogger {

    private final Logger logger;
    private final String dlcName;

    /**
     * Constructs a new DLC logger for the specified plugin and DLC name.
     *
     * @param plugin  The plugin instance to retrieve the logger from.
     * @param dlcName The name of the DLC to include in log messages.
     */
    public MCEngineDLCLogger(Plugin plugin, String dlcName) {
        this.logger = plugin.getLogger();
        this.dlcName = dlcName;
    }

    /**
     * Logs an informational message with the DLC prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info("[DLC] [" + dlcName + "] " + message);
    }

    /**
     * Logs a warning message with the DLC prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning("[DLC] [" + dlcName + "] " + message);
    }

    /**
     * Logs a severe error message with the DLC prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe("[DLC] [" + dlcName + "] " + message);
    }

    /**
     * Returns the raw logger (without prefix formatting).
     * Useful for APIs that accept java.util.logging.Logger directly.
     *
     * @return the underlying Logger
     */
    public Logger getLogger() {
        return logger;
    }
}
