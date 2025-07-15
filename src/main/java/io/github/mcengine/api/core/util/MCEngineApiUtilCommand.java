package io.github.mcengine.api.core.util;

import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A utility class to dynamically register command executors by class name.
 * <p>
 * This API allows plugins to register commands with associated
 * {@link CommandExecutor} classes at runtime using reflection.
 */
public class MCEngineApiUtilCommand {

    /**
     * Registers a command by dynamically loading a class that implements {@link CommandExecutor}.
     * <p>
     * The command must already be defined in plugin.yml.
     * The class must:
     * <ul>
     *   <li>Be on the classpath</li>
     *   <li>Implement {@link CommandExecutor}</li>
     *   <li>Have a public no-argument constructor</li>
     * </ul>
     *
     * @param plugin    the JavaPlugin instance
     * @param cmd       the command name defined in plugin.yml
     * @param className the fully qualified class name that implements {@link CommandExecutor}
     */
    public static void registerCommand(JavaPlugin plugin, String cmd, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (!(instance instanceof CommandExecutor)) {
                plugin.getLogger().warning("Class " + className + " does not implement CommandExecutor.");
                return;
            }

            plugin.getCommand(cmd).setExecutor((CommandExecutor) instance);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Command class not found: " + className);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register command for " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
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
        type = type.toLowerCase();
        String folder = type.equals("addon") ? "addons" : "dlcs";
        List<String> extensions = MCEngineApiUtilExtension.getLoadedExtensionFileNames(plugin, folder);

        player.sendMessage("§eLoaded " + type + "s:");
        if (extensions.isEmpty()) {
            player.sendMessage("§7- §cNo " + type + "s found.");
        } else {
            extensions.stream()
                    .sorted()
                    .forEach(name -> player.sendMessage("§7- §a" + name));
        }
        return true;
    }
}
