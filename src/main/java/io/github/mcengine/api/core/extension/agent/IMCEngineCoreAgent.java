package io.github.mcengine.api.core.extension.agent;

import org.bukkit.plugin.Plugin;

/**
 * Represents a Core-based Agent module that can be dynamically loaded into the MCEngine.
 * <p>
 * Implement this interface to integrate core-related agents into the system.
 */
public interface IMCEngineCoreAgent {

    /**
     * Called when the Core {module} is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);

    /**
     * Called when the Core {module} is unloaded or disabled by the engine.
     * <p>
     * Use this method to release resources, unregister listeners, or perform any necessary cleanup.
     *
     * @param plugin The plugin instance providing context.
     */
    void onDisload(Plugin plugin);

    /**
     * Sets a unique ID for this Core {module} instance.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
