package io.github.mcengine.api.core;

import io.github.mcengine.api.core.util.MCEngineApiUtilCommand;
import io.github.mcengine.api.core.util.MCEngineApiUtilListener;
import io.github.mcengine.api.core.util.MCEngineApiUtilExtension;
import io.github.mcengine.api.core.util.MCEngineApiUtilUpdate;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;

/**
 * Central API class for MCEngine to provide simplified access to
 * dynamic command registration, event listener registration, extension loading, and update checking.
 */
public class MCEngineApi {

    /**
     * Registers a command executor dynamically by class name.
     *
     * @param plugin    the JavaPlugin instance
     * @param cmd       the command name defined in plugin.yml
     * @param className the fully qualified class name of the CommandExecutor
     */
    public static void registerCommand(JavaPlugin plugin, String cmd, String className) {
        MCEngineApiUtilCommand.registerCommand(plugin, cmd, className);
    }

    /**
     * Registers an event listener dynamically by class name.
     *
     * @param plugin    the plugin instance
     * @param className the fully qualified class name of the Listener
     */
    public static void registerListener(Plugin plugin, String className) {
        MCEngineApiUtilListener.registerListener(plugin, className);
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
        MCEngineApiUtilExtension.loadExtensions(plugin, className, folderName, type);
    }

    /**
     * Returns a list of successfully loaded AddOn or DLC file names from a specific folder.
     *
     * @param plugin     the plugin instance
     * @param folderName the folder name ("addons", "dlcs", etc.)
     * @return list of loaded JAR filenames
     */
    public static List<String> getLoadedExtensionFileNames(Plugin plugin, String folderName) {
        return MCEngineApiUtilExtension.getLoadedExtensionFileNames(plugin, folderName);
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
        MCEngineApiUtilUpdate.checkUpdate(plugin, logger, gitPlatform, org, repository, token);
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
        MCEngineApiUtilUpdate.checkUpdate(plugin, logger, prefix, gitPlatform, org, repository, token);
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
        return MCEngineApiUtilCommand.handleExtensionList(player, plugin, type);
    }
}
