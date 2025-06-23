package io.github.mcengine.api.mcengine.extension.dlc;

import org.bukkit.plugin.Plugin;

/**
 * Interface for DLC modules that can be dynamically loaded.
 */
public interface IMCEngineDLC {

    /**
     * Called when the DLC is loaded by the engine.
     *
     * @param plugin The plugin instance providing context.
     */
    void onLoad(Plugin plugin);
}
