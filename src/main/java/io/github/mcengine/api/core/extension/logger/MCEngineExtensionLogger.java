package io.github.mcengine.api.core.extension.logger;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * General-purpose logger for MCEngine extensions, supporting dynamic prefixing
 * for different contexts like AddOns, APIs, or others.
 */
public class MCEngineExtensionLogger {

    private final Logger logger;
    private final String contextLabel;
    private final String name;

    /**
     * Constructs a new extension logger with a specified label and name.
     *
     * @param plugin       The plugin instance to retrieve the logger from.
     * @param contextLabel The context label to prefix (e.g., "API", "AddOn").
     * @param name         The identifier name to include (e.g., AddOn name, API name).
     */
    public MCEngineExtensionLogger(Plugin plugin, String contextLabel, String name) {
        this.logger = plugin.getLogger();
        this.contextLabel = contextLabel;
        this.name = name;
    }

    /**
     * Logs an informational message with contextual prefix.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        logger.info(getPrefix() + message);
    }

    /**
     * Logs a warning message with contextual prefix.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        logger.warning(getPrefix() + message);
    }

    /**
     * Logs a severe error message with contextual prefix.
     *
     * @param message The message to log.
     */
    public void severe(String message) {
        logger.severe(getPrefix() + message);
    }

    /**
     * Returns the raw logger instance.
     *
     * @return the underlying {@link Logger}
     */
    public Logger getLogger() {
        return logger;
    }

    private String getPrefix() {
        return "[" + contextLabel + "] [" + name + "] ";
    }
}
