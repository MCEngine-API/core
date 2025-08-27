package io.github.mcengine.api.core.extension.logger;

import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General-purpose logger for MCEngine extensions, supporting dynamic labeling
 * for different contexts like AddOns, APIs, or others.
 *
 * Example output:
 *   [YourPlugin] [AddOn] [example] is Enabled
 */
public class MCEngineExtensionLogger {

    /** The Bukkit-managed plugin logger (already prints [PluginName]). */
    private final Logger base;

    /** The additional context prefix we prepend to each message. */
    private final String prefix;

    /**
     * Constructs a new extension logger with a specified label and name.
     *
     * @param plugin       The plugin instance to retrieve the logger from.
     * @param contextLabel The context label to identify the type of component (e.g., "AddOn").
     * @param name         The specific component name (e.g., AddOn name).
     */
    public MCEngineExtensionLogger(Plugin plugin, String contextLabel, String name) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(contextLabel, "contextLabel");
        Objects.requireNonNull(name, "name");

        // Use the plugin's logger so Bukkit will add the leading "[PluginName]" automatically.
        this.base = plugin.getLogger();

        // Build our extra prefix that comes AFTER "[PluginName]".
        this.prefix = " [" + contextLabel + "] [" + name + "] ";
    }

    /** Logs an informational message with contextual label. */
    public void info(String message) {
        base.log(Level.INFO, prefix + message);
    }

    /** Logs a warning message with contextual label. */
    public void warning(String message) {
        base.log(Level.WARNING, prefix + message);
    }

    /** Logs a severe error message with contextual label. */
    public void severe(String message) {
        base.log(Level.SEVERE, prefix + message);
    }
}
