package io.github.mcengine.api.core.extension.logger;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * General-purpose logger for MCEngine extensions, supporting dynamic labeling
 * for different contexts like AddOns, APIs, or others.
 */
public class MCEngineExtensionLogger {

    /** The underlying Java logger with contextual labeling. */
    private final Logger logger;

    /**
     * Constructs a new extension logger with a specified label and name.
     *
     * @param plugin       The plugin instance to retrieve the logger from.
     * @param contextLabel The context label to identify the type of component.
     * @param name         The specific component name (e.g., AddOn name).
     */
    public MCEngineExtensionLogger(Plugin plugin, String contextLabel, String name) {
        this.logger = Logger.getLogger("["+ plugin.getLogger().getName() + "]" + " [" + contextLabel + "] [" + name + "] ");
    }

    /**
     * Logs an informational message with contextual label.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning message with contextual label.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning(message);
    }

    /**
     * Logs a severe error message with contextual label.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe(message);
    }

    /**
     * Returns the raw logger instance.
     *
     * @return the underlying {@link Logger}
     */
    public Logger getLogger() {
        return logger;
    }
}
