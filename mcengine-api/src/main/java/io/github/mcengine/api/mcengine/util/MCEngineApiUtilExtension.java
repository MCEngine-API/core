package io.github.mcengine.api.mcengine.util;

import io.github.mcengine.api.mcengine.addon.IMCEngineAddOn;
import io.github.mcengine.api.mcengine.dlc.IMCEngineDLC;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Utility class for loading AddOns or DLCs from JAR files.
 */
public class MCEngineApiUtilExtension {

    private static final Map<String, List<String>> loadedExtensions = new HashMap<>();

    public static void loadExtensions(Plugin plugin, String folderName, String type) {
        Logger logger = plugin.getLogger();
        File folder = new File(plugin.getDataFolder(), folderName);

        if (!folder.exists() && !folder.mkdirs()) {
            logger.warning("[" + type + "] Could not create " + folderName + " directory.");
            return;
        }

        File[] files = folder.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if (files == null || files.length == 0) {
            logger.info("[" + type + "] No " + folderName + " found.");
            return;
        }

        List<String> successfullyLoaded = new ArrayList<>();

        for (File file : files) {
            boolean loaded = false;
            logger.info("[" + type + "] Scanning JAR: " + file.getName());

            try (
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    MCEngineApiUtilExtension.class.getClassLoader()
                );
                JarFile jar = new JarFile(file)
            ) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.endsWith(".class") || name.contains("$")) continue;

                    String className = name.replace("/", ".").replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                        Object instance = clazz.getDeclaredConstructor().newInstance();

                        boolean matched = false;

                        if (type.equalsIgnoreCase("AddOn") && instance instanceof IMCEngineAddOn addOn) {
                            addOn.onLoad(plugin);
                            matched = true;
                        } else if (type.equalsIgnoreCase("DLC") && instance instanceof IMCEngineDLC dlc) {
                            dlc.onLoad(plugin);
                            matched = true;
                        }

                        if (matched) {
                            logger.info("[" + type + "] Loaded: " + className);
                            loaded = true;
                            break;
                        }

                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to load class: " + className);
                        e.printStackTrace();
                    }
                }

                if (!loaded) {
                    logger.warning("[" + type + "] No valid class found in: " + file.getName());
                } else {
                    successfullyLoaded.add(file.getName());
                }

            } catch (Exception e) {
                logger.warning("[" + type + "] Error loading " + type + " JAR: " + file.getName());
                e.printStackTrace();
            }
        }

        loadedExtensions.put(folderName, successfullyLoaded);
    }

    public static List<String> getLoadedExtensionFileNames(Plugin plugin, String folderName) {
        return loadedExtensions.getOrDefault(folderName, Collections.emptyList());
    }
}
