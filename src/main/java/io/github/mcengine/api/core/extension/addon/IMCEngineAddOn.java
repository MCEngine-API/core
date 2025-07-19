package io.github.mcengine.api.core.extension.addon;

import org.bukkit.plugin.Plugin;

/**
 * Interface for AddOn modules that can be dynamically loaded.
 */
public interface IMCEngineAddOn {

    /**
     * Called when the AddOn is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);

    /**
     * Called when the AddOn is unloaded or disabled by the engine.
     * <p>
     * Use this method to release resources, unregister listeners,
     * or perform any necessary cleanup.
     *
     * @param plugin The plugin instance providing context.
     */
    void onDisload(Plugin plugin);

    /**
     * Sets a unique ID for this AddOn instance.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
