package io.github.mcengine.api.mcengine.addon;

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
}
