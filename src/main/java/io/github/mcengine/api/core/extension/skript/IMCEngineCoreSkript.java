package io.github.mcengine.api.core.extension.skript;

import org.bukkit.plugin.Plugin;

/**
 * Represents a skript-related DLC module that can be dynamically loaded into the MCEngine.
 * <p>
 * This interface should be implemented by modules that provide backend or shared logic to the MCEngine,
 * typically not involving direct player interaction but supporting systems like storage, logic utilities, etc.
 */
public interface IMCEngineCoreSkript {

    /**
     * Called when the DLC skript is loaded by the engine.
     * <p>
     * Use this method to perform initialization, resource registration, or dependency linking
     * required for the skript to function correctly.
     *
     * @param plugin The {@link Plugin} instance providing context for this DLC module.
     */
    void onLoad(Plugin plugin);

    /**
     * Called when the DLC skript is unloaded or disabled by the engine.
     * <p>
     * This method should be used to clean up event handlers, memory, or tasks
     * created during {@link #onLoad(Plugin)}.
     *
     * @param plugin The {@link Plugin} instance providing context for this DLC module.
     */
    void onDisload(Plugin plugin);

    /**
     * Sets a unique ID for this skript module.
     *
     * @param id The unique ID assigned by the engine.
     */
    void setId(String id);
}
