package io.github.mcengine.api.core;

import io.github.mcengine.api.core.util.MCEngineCoreApiUtilCommand;
import io.github.mcengine.api.core.util.MCEngineCoreApiUtilListener;
import io.github.mcengine.api.core.util.MCEngineCoreApiUtilExtension;
import io.github.mcengine.api.core.util.MCEngineCoreApiUtilUpdate;
import io.github.mcengine.api.core.util.MCEngineCoreApiConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Central API class for MCEngine to provide simplified access to
 * dynamic command registration, event listener registration, extension loading, and update checking.
 */
public class MCEngineCoreApi {

    /**
     * Registers a command executor dynamically by class name.
     *
     * @param plugin    the JavaPlugin instance
     * @param cmd       the command name defined in plugin.yml
     * @param className the fully qualified class name of the CommandExecutor
     */
    public static void registerCommand(JavaPlugin plugin, String cmd, String className) {
        MCEngineCoreApiUtilCommand.registerCommand(plugin, cmd, className);
    }

    /**
     * Registers an event listener dynamically by class name.
     *
     * @param plugin    the plugin instance
     * @param className the fully qualified class name of the Listener
     */
    public static void registerListener(Plugin plugin, String className) {
        MCEngineCoreApiUtilListener.registerListener(plugin, className);
    }

    /**
     * Loads external AddOn or DLC extensions with filtering by class interface name.
     * Only classes that implement the specified interface and provide onLoad(Plugin) are invoked.
     *
     * @param plugin     the plugin instance
     * @param className  the interface class name to filter by (e.g., "com.example.MyAddOnInterface")
     * @param folderName the folder name inside the plugin data folder (e.g., "addons", "dlcs")
     * @param type       the label used for logging (e.g., "AddOn", "DLC")
     */
    public static void loadExtensions(Plugin plugin, String className, String folderName, String type) {
        MCEngineCoreApiUtilExtension.loadExtensions(plugin, className, folderName, type);
    }

    /**
     * Returns a list of successfully loaded AddOn or DLC file names from a specific folder.
     *
     * @param plugin     the plugin instance
     * @param folderName the folder name ("addons", "dlcs", etc.)
     * @return list of loaded JAR filenames
     */
    public static List<String> getLoadedExtensionFileNames(Plugin plugin, String folderName) {
        return MCEngineCoreApiUtilExtension.getLoadedExtensionFileNames(plugin, folderName);
    }

    /**
     * Checks for plugin updates from GitHub or GitLab by fetching the latest release tag.
     * Logs update information using the provided logger (for core plugins).
     *
     * @param plugin      the plugin instance
     * @param logger      the logger instance to log messages
     * @param gitPlatform the platform to use: "github" or "gitlab"
     * @param org         the GitHub org or GitLab group/namespace
     * @param repository  the repository name
     * @param token       optional GitHub/GitLab token (can be null or "null")
     */
    public static void checkUpdate(Plugin plugin, Logger logger, String gitPlatform, String org, String repository, String token) {
        MCEngineCoreApiUtilUpdate.checkUpdate(plugin, logger, gitPlatform, org, repository, token);
    }

    /**
     * Checks for plugin updates from GitHub or GitLab by fetching the latest release tag,
     * with a custom prefix for AddOns or DLCs.
     *
     * @param plugin      the plugin instance
     * @param logger      the logger instance to log messages
     * @param prefix      the prefix to prepend to each log message (e.g., "[AddOn] [Name] ")
     * @param gitPlatform the platform to use: "github" or "gitlab"
     * @param org         the GitHub org or GitLab group/namespace
     * @param repository  the repository name
     * @param token       optional GitHub/GitLab token (can be null or "null")
     */
    public static void checkUpdate(Plugin plugin, Logger logger, String prefix, String gitPlatform, String org, String repository, String token) {
        MCEngineCoreApiUtilUpdate.checkUpdate(plugin, logger, prefix, gitPlatform, org, repository, token);
    }

    /**
     * Displays a list of loaded addons or DLCs based on type.
     *
     * @param player The player to send the extension list to.
     * @param plugin The plugin instance used for folder resolution.
     * @param type   The type of extension ("addon" or "dlc").
     * @return true after displaying the list.
     */
    public static boolean handleExtensionList(Player player, Plugin plugin, String type) {
        return MCEngineCoreApiUtilCommand.handleExtensionList(player, plugin, type);
    }

    /**
     * Sets a unique, non-null ID for an extension.
     *
     * @param id The extension ID to set.
     * @throws IllegalArgumentException if ID is null or already exists.
     */
    public static void setId(String id) {
        MCEngineCoreApiUtilExtension.setId(id);
    }

    /**
     * Returns a list of all registered extension IDs.
     *
     * @return list of all extension IDs.
     */
    public static List<String> getAllId() {
        return MCEngineCoreApiUtilExtension.getAllId();
    }

    // ----------------------
    // Config Access Methods
    // ----------------------

    /**
     * Retrieves a String value from the configuration.
     *
     * @param path     the parent path key (e.g., "tools.threadpool")
     * @param variable the specific variable name (e.g., "enable")
     * @param config   the configuration map
     * @return the String value, or {@code null} if not found or not a String
     */
    public static String getConfigString(String path, String variable, Map<String, Object> config) {
        return MCEngineCoreApiConfiguration.getConfigString(path, variable, config);
    }

    /**
     * Retrieves a boolean value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the boolean value, or {@code false} if not found or not a boolean
     */
    public static boolean getConfigBoolean(String path, String variable, Map<String, Object> config) {
        return MCEngineCoreApiConfiguration.getConfigBoolean(path, variable, config);
    }

    /**
     * Retrieves an int value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the int value, or {@code 0} if not found or not a number
     */
    public static int getConfigInt(String path, String variable, Map<String, Object> config) {
        return MCEngineCoreApiConfiguration.getConfigInt(path, variable, config);
    }

    /**
     * Retrieves a long value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the long value, or {@code 0L} if not found or not a number
     */
    public static long getConfigLong(String path, String variable, Map<String, Object> config) {
        return MCEngineCoreApiConfiguration.getConfigLong(path, variable, config);
    }

    /**
     * Retrieves a double value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the double value, or {@code 0.0} if not found or not a number
     */
    public static double getConfigDouble(String path, String variable, Map<String, Object> config) {
        return MCEngineCoreApiConfiguration.getConfigDouble(path, variable, config);
    }

    /**
     * Retrieves a generic Object value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the Object value, or {@code null} if not found
     */
    public static Object getConfigObject(String path, String variable, Map<String, Object> config) {
        return MCEngineCoreApiConfiguration.getConfigObject(path, variable, config);
    }
}
