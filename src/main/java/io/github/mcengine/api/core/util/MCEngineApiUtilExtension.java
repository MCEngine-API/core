package io.github.mcengine.api.core.util;

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

    /**
     * Keeps track of loaded AddOn/DLC filenames per folder name.
     * The key is the folder name and the value is a list of successfully loaded JAR filenames.
     */
    private static final Map<String, List<String>> loadedExtensions = new HashMap<>();

    /**
     * Set of all registered extension IDs.
     * Ensures all extension IDs are unique and not reused.
     */
    private static final Set<String> extensionIds = new HashSet<>();

    /**
     * Sets the current extension ID. Must be unique and not null.
     *
     * @param id The extension ID to register.
     * @throws IllegalArgumentException if the ID is null or already used.
     */
    public static void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Extension ID must not be null.");
        }
        if (extensionIds.contains(id)) {
            throw new IllegalArgumentException("Extension ID already exists: " + id);
        }
        extensionIds.add(id);
    }

    /**
     * Returns a list of all registered extension IDs.
     *
     * @return List of all extension IDs.
     */
    public static List<String> getAllId() {
        return new ArrayList<>(extensionIds);
    }

    /**
     * Loads extensions (AddOns or DLCs) from the specified folder (recursively),
     * only loading classes that implement a specific interface and contain both
     * "onLoad(Plugin)" and "setId(String)" methods.
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

        File rootFolder = new File(plugin.getDataFolder(), "extensions/" + folderName);
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

                        Method setIdMethod;
                        try {
                            setIdMethod = clazz.getMethod("setId", String.class);
                        } catch (NoSuchMethodException e) {
                            logger.fine("[" + type + "] No setId(String) found in: " + targetClassName);
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();

                        // Generate and set unique ID
                        String extensionId = UUID.randomUUID().toString();
                        setId(extensionId);
                        setIdMethod.invoke(extensionInstance, extensionId);

                        onLoadMethod.invoke(extensionInstance, plugin);

                        logger.info("[" + type + "] Loaded: " + targetClassName + " with ID: " + extensionId);
                        loaded = true;
                        break;
                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to load class: " + targetClassName);
                        e.printStackTrace();
                    }
                }

                if (!loaded) {
                    logger.warning("[" + type + "] No valid onLoad(Plugin) + setId(String) class found in: " + file.getName());
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
     * Recursively collects all .jar files under the given folder and its subfolders.
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

    /**
     * Invokes the "onDisload" method on all previously loaded extensions from a given folder.
     *
     * @param plugin     The Bukkit plugin instance.
     * @param folderName The folder name from which extensions were previously loaded.
     * @param type       The extension type label (e.g., "AddOn", "DLC").
     */
    public static void onDisload(Plugin plugin, String folderName, String type) {
        Logger logger = plugin.getLogger();
        List<String> jars = loadedExtensions.get(folderName);
        if (jars == null || jars.isEmpty()) {
            logger.info("[" + type + "] No previously loaded extensions to disload.");
            return;
        }

        File rootFolder = new File(plugin.getDataFolder(), "extensions/" + folderName);
        for (String fileName : jars) {
            File jarFile = new File(rootFolder, fileName);
            if (!jarFile.exists()) continue;

            try (
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jarFile.toURI().toURL()},
                    plugin.getClass().getClassLoader()
                );
                JarFile jar = new JarFile(jarFile)
            ) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.endsWith(".class") || name.contains("$")) continue;
                    String targetClassName = name.replace("/", ".").replace(".class", "");

                    try {
                        Class<?> clazz = classLoader.loadClass(targetClassName);
                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                        Method onDisloadMethod;
                        try {
                            onDisloadMethod = clazz.getMethod("onDisload", Plugin.class);
                        } catch (NoSuchMethodException e) {
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();
                        onDisloadMethod.invoke(extensionInstance, plugin);
                        logger.info("[" + type + "] Disloaded: " + targetClassName);
                        break;
                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to disload class: " + targetClassName);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                logger.warning("[" + type + "] Error disloading " + type + " JAR: " + jarFile.getName());
                e.printStackTrace();
            }
        }

        loadedExtensions.remove(folderName);
    }
}
