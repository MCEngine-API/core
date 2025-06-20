package io.github.mcengine.api.mcengine.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Utility class for loading AddOns or DLCs from JAR files.
 * Supports recursive directory search and class loading.
 */
public class MCEngineApiUtilExtension {

    // Keeps track of loaded AddOn/DLC filenames per folder name
    private static final Map<String, List<String>> loadedExtensions = new HashMap<>();

    /**
     * Loads extensions (AddOns or DLCs) from the specified folder (recursively),
     * only loading classes that implement a specific interface and contain an "onLoad(Plugin)" method.
     *
     * @param plugin     The Bukkit plugin instance.
     * @param className  The fully qualified name of the interface class to filter against. Must not be null.
     * @param folderName The folder name (relative to the plugin data folder).
     * @param type       The extension type label (e.g., "AddOn", "DLC").
     */
    public static void loadExtensions(Plugin plugin, String className, String folderName, String type) {
        Logger logger = plugin.getLogger();

        if (className == null) {
            throw new IllegalArgumentException("className must not be null.");
        }

        File rootFolder = new File(plugin.getDataFolder(), folderName);
        if (!rootFolder.exists() && !rootFolder.mkdirs()) {
            logger.warning("[" + type + "] Could not create " + folderName + " directory.");
            return;
        }

        List<File> jarFiles = new ArrayList<>();
        collectJarFilesRecursive(rootFolder, jarFiles);

        if (jarFiles.isEmpty()) {
            logger.info("[" + type + "] No " + folderName + " found.");
            return;
        }

        List<String> successfullyLoaded = new ArrayList<>();

        for (File file : jarFiles) {
            boolean loaded = false;
            logger.info("[" + type + "] Scanning JAR: " + file.getName());

            try (
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    plugin.getClass().getClassLoader()
                );
                JarFile jar = new JarFile(file)
            ) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.endsWith(".class") || name.contains("$")) {
                        logger.fine("[" + type + "] Skipped: " + name + " (not a class or inner class)");
                        continue;
                    }

                    String targetClassName = name.replace("/", ".").replace(".class", "");
                    logger.fine("[" + type + "] Inspecting: " + targetClassName);

                    try {
                        Class<?> clazz = classLoader.loadClass(targetClassName);

                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                            logger.fine("[" + type + "] Skipped: " + targetClassName + " (interface or abstract)");
                            continue;
                        }

                        Class<?> requiredInterface;
                        try {
                            requiredInterface = Class.forName(className, false, plugin.getClass().getClassLoader());
                        } catch (ClassNotFoundException e) {
                            logger.warning("[" + type + "] Interface not found: " + className);
                            break;
                        }

                        if (!requiredInterface.isAssignableFrom(clazz)) {
                            logger.fine("[" + type + "] Skipped: " + targetClassName + " does not implement " + className);
                            continue;
                        }

                        Method onLoadMethod;
                        try {
                            onLoadMethod = clazz.getMethod("onLoad", Plugin.class);
                        } catch (NoSuchMethodException e) {
                            logger.fine("[" + type + "] No onLoad(Plugin) found in: " + targetClassName);
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();
                        onLoadMethod.invoke(extensionInstance, plugin);

                        logger.info("[" + type + "] Loaded: " + targetClassName);
                        loaded = true;
                        break;
                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to load class: " + targetClassName);
                        e.printStackTrace();
                    }
                }

                if (!loaded) {
                    logger.warning("[" + type + "] No valid onLoad(Plugin) class found in: " + file.getName());
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

    /**
     * Recursively collects all .jar files under the given folder and subfolders.
     *
     * @param folder   The folder to search in.
     * @param jarFiles The list to append found .jar files to.
     */
    private static void collectJarFilesRecursive(File folder, List<File> jarFiles) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectJarFilesRecursive(file, jarFiles);
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                jarFiles.add(file);
            }
        }
    }

    /**
     * Returns a list of successfully loaded extension JAR filenames from the specified folder.
     *
     * @param plugin     The plugin instance.
     * @param folderName The folder name ("addons" or "dlcs").
     * @return List of loaded .jar filenames from the given folder.
     */
    public static List<String> getLoadedExtensionFileNames(Plugin plugin, String folderName) {
        return loadedExtensions.getOrDefault(folderName, Collections.emptyList());
    }
}
