package io.github.mcengine.api.core.extension.api;

import org.bukkit.plugin.Plugin;

/**
 * Represents a core-related DLC module that can be dynamically loaded into the MCEngine.
 * <p>
 * Implement this interface to create a plugin extension that hooks into the core system
 * provided by the MCEngine. The implementation should register its functionality within the {@link #onLoad(Plugin)} method.
 */
public interface IMCEngineAPI {

    /**
     * Called when the DLC module is loaded by the engine.
     * <p>
     * This method should be used to initialize any resources, register listeners,
     * or perform setup logic necessary for the plugin extension to function correctly.
     *
     * @param plugin The {@link Plugin} instance that is providing the context for this DLC module.
     */
    void onLoad(Plugin plugin);

    /**
     * Called when the DLC module is unloaded or disabled by the engine.
     * <p>
     * Use this method to deregister any resources, cancel tasks,
     * or clean up systems related to this module.
     *
     * @param plugin The {@link Plugin} instance providing the context.
     */
    void onDisload(Plugin plugin);

    /**
     * Sets a unique ID for this API module.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
