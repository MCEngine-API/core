package io.github.mcengine.api.core.util;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * A utility class to dynamically register event listeners by class name.
 * <p>
 * This API allows plugins to register Bukkit {@link Listener} classes at runtime
 * using reflection.
 */
public class MCEngineCoreApiUtilListener {

    /**
     * Registers an event listener by dynamically loading a class that implements {@link Listener}.
     * <p>
     * The class must:
     * <ul>
     *   <li>Be on the classpath</li>
     *   <li>Implement {@link Listener}</li>
     *   <li>Have a public no-argument constructor</li>
     * </ul>
     *
     * @param plugin    the plugin instance
     * @param className the fully qualified name of the class implementing {@link Listener}
     */
    public static void registerListener(Plugin plugin, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (!(instance instanceof Listener)) {
                plugin.getLogger().warning("Class " + className + " does not implement Listener.");
                return;
            }

            PluginManager manager = plugin.getServer().getPluginManager();
            manager.registerEvents((Listener) instance, plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Listener class not found: " + className);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register listener for " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
