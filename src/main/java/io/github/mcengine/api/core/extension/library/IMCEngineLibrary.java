package io.github.mcengine.api.core.extension.library;

import org.bukkit.plugin.Plugin;

/**
 * Represents a library-related DLC module that can be dynamically loaded into the MCEngine.
 * <p>
 * This interface should be implemented by modules that provide backend or shared logic to the MCEngine,
 * typically not involving direct player interaction but supporting systems like storage, logic utilities, etc.
 */
public interface IMCEngineLibrary {

    /**
     * Called when the DLC library is loaded by the engine.
     * <p>
     * Use this method to perform initialization, resource registration, or dependency linking
     * required for the library to function correctly.
     *
     * @param plugin The {@link Plugin} instance providing context for this DLC module.
     */
    void onLoad(Plugin plugin);
}
