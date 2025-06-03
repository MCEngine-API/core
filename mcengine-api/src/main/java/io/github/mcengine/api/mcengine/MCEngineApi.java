package io.github.mcengine.api.mcengine;

import io.github.mcengine.api.mcengine.util.MCEngineApiUtilCommand;
import io.github.mcengine.api.mcengine.util.MCEngineApiUtilListener;
import io.github.mcengine.api.mcengine.util.MCEngineApiUtilExtension;
import io.github.mcengine.api.mcengine.util.MCEngineApiUtilUpdate;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
     * Loads external AddOn or DLC extensions from a folder (e.g., "addons" or "dlcs").
     * The JARs are expected to contain a class with a public onLoad(Plugin) method.
     *
     * @param plugin     the plugin instance
     * @param folderName the folder name inside the plugin data folder (e.g., "addons", "dlcs")
     * @param type       the label used for logging (e.g., "AddOn", "DLC")
     */
    public static void loadExtensions(Plugin plugin, String folderName, String type) {
        MCEngineApiUtilExtension.loadExtensions(plugin, folderName, type);
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
     *
     * @param plugin      the plugin instance
     * @param gitPlatform the platform to use: "github" or "gitlab"
     * @param org         the GitHub org or GitLab group/namespace
     * @param repository  the repository name
     * @param token       optional GitHub/GitLab token (can be null or "null")
     */
    public static void checkUpdate(Plugin plugin, String gitPlatform, String org, String repository, String token) {
        MCEngineApiUtilUpdate.checkUpdate(plugin, gitPlatform, org, repository, token);
    }
}
