package io.github.mcengine.api.mcengine.util;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

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
}
