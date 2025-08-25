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
 * <p>
 * Features:
 * <ul>
 *   <li>Recursive discovery of {@code .jar} files under {@code plugins/YourPlugin/extensions/&lt;folderName&gt;}.</li>
 *   <li>Eager preloading of <em>all</em> classes in each JAR to trigger static initializers
 *       (so you don't need "check()" warm-ups).</li>
 *   <li>Filtering for concrete classes that implement a given interface and expose
 *       {@code onLoad(Plugin)} and {@code setId(String)} methods.</li>
 *   <li>Instantiation and invocation of <em>every</em> matching class (not just the first).</li>
 *   <li>Balanced {@code onDisload(Plugin)} support that iterates all previously loaded classes in each JAR.</li>
 * </ul>
 */
public class MCEngineCoreApiUtilExtension {

    /**
     * Map of folder name → list of successfully loaded JAR filenames.
     * <p>
     * Used to remember which JARs were loaded so the matching {@link #onDisload(Plugin, String, String)}
     * can iterate only those JARs later.
     */
    private static final Map<String, List<String>> loadedExtensions = new HashMap<>();

    /**
     * Set of all registered extension IDs to ensure uniqueness across loads.
     */
    private static final Set<String> extensionIds = new HashSet<>();

    /**
     * Cache of JAR filename → URLClassLoader.
     * <p>
     * We intentionally keep these class loaders alive to ensure dependent class/resource
     * access keeps working after load (some Bukkit environments can misbehave if the URLClassLoader
     * is closed immediately).
     */
    private static final Map<String, URLClassLoader> classLoaderCache = new HashMap<>();

    /**
     * Cache of JAR filename → list of preloaded class names.
     * <p>
     * This is populated during {@link #loadExtensions(Plugin, String, String, String)} so that
     * {@link #onDisload(Plugin, String, String)} can iterate known classes efficiently.
     */
    private static final Map<String, List<String>> preloadedClassNames = new HashMap<>();

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
     * Loads extensions (AddOns or DLCs) from the specified folder (recursively).
     * <p>
     * Steps performed per JAR:
     * <ol>
     *     <li>Collect all class names in the JAR (skips inner/synthetic and {@code module-info}).</li>
     *     <li><strong>Preload all classes</strong> via {@code Class.forName(..., true, loader)} to
     *         trigger static initializers (ensures helpers like {@code ChatBotConfigLoader} are initialized).</li>
     *     <li>Find all concrete classes that implement {@code className} and expose both
     *         {@code onLoad(Plugin)} and {@code setId(String)}; instantiate, assign a unique ID, and invoke onLoad.</li>
     * </ol>
     *
     * @param plugin     The Bukkit plugin instance.
     * @param className  Fully qualified name of the interface to filter against (non-null).
     * @param folderName The folder name (relative to {@code extensions/}) to scan.
     * @param type       Human-readable label of the extension type (e.g., "AddOn", "DLC").
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

        // Resolve the required interface once using the plugin's class loader.
        final Class<?> requiredInterface;
        try {
            requiredInterface = Class.forName(className, false, plugin.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.warning("[" + type + "] Interface not found: " + className);
            return;
        }

        for (File file : jarFiles) {
            logger.info("[" + type + "] Scanning JAR: " + file.getName());

            boolean anyLoadedInJar = false;
            URLClassLoader classLoader = classLoaderCache.get(file.getName());
            try {
                if (classLoader == null) {
                    classLoader = new URLClassLoader(
                        new URL[]{file.toURI().toURL()},
                        plugin.getClass().getClassLoader()
                    );
                    classLoaderCache.put(file.getName(), classLoader);
                }

                // 1) Gather class names
                List<String> classNames = new ArrayList<>();
                try (JarFile jar = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (!name.endsWith(".class")) continue;
                        if (name.contains("$")) continue;                       // skip inner/anonymous
                        if (name.equals("module-info.class")) continue;         // skip module-info

                        String fqcn = name.replace('/', '.').substring(0, name.length() - 6);
                        classNames.add(fqcn);
                    }
                }

                // 2) Eagerly preload all classes to run static initializers
                //    (fixes the "only loads surface class" problem).
                for (String fqcn : classNames) {
                    try {
                        Class.forName(fqcn, true, classLoader); // initialize = true
                        logger.finest("[" + type + "] Preloaded: " + fqcn);
                    } catch (Throwable preloadErr) {
                        logger.fine("[" + type + "] Preload skipped/failed for " + fqcn + ": " + preloadErr.getMessage());
                    }
                }
                preloadedClassNames.put(file.getName(), classNames);

                // 3) Instantiate and invoke onLoad for all matching classes
                for (String fqcn : classNames) {
                    try {
                        Class<?> clazz = Class.forName(fqcn, false, classLoader);

                        // Only concrete classes
                        int mods = clazz.getModifiers();
                        if (clazz.isInterface() || Modifier.isAbstract(mods)) {
                            logger.finer("[" + type + "] Skipped (abstract/interface): " + fqcn);
                            continue;
                        }

                        if (!requiredInterface.isAssignableFrom(clazz)) {
                            logger.finer("[" + type + "] Skipped (doesn't implement " + className + "): " + fqcn);
                            continue;
                        }

                        Method onLoadMethod;
                        try {
                            onLoadMethod = clazz.getMethod("onLoad", Plugin.class);
                        } catch (NoSuchMethodException e) {
                            logger.finer("[" + type + "] No onLoad(Plugin): " + fqcn);
                            continue;
                        }

                        Method setIdMethod;
                        try {
                            setIdMethod = clazz.getMethod("setId", String.class);
                        } catch (NoSuchMethodException e) {
                            logger.finer("[" + type + "] No setId(String): " + fqcn);
                            continue;
                        }

                        Object extensionInstance = clazz.getDeclaredConstructor().newInstance();

                        // Generate and set unique ID (also register globally)
                        String extensionId = UUID.randomUUID().toString();
                        setId(extensionId);
                        setIdMethod.invoke(extensionInstance, extensionId);

                        onLoadMethod.invoke(extensionInstance, plugin);

                        logger.info("[" + type + "] Loaded: " + fqcn + " with ID: " + extensionId);
                        anyLoadedInJar = true;

                    } catch (Throwable e) {
                        logger.warning("[" + type + "] Failed to load/initialize class: " + fqcn);
                        e.printStackTrace();
                    }
                }

                if (!anyLoadedInJar) {
                    logger.warning("[" + type + "] No valid onLoad(Plugin) + setId(String) implementors found in: " + file.getName());
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
     * Recursively collects all {@code .jar} files under the given folder and its subfolders.
     *
     * @param folder   The folder to search in.
     * @param jarFiles Output list to append found {@code .jar} files to.
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
     * @param plugin     The plugin instance (unused; kept for compatibility).
     * @param folderName The folder name ("addons" or "dlcs").
     * @return List of loaded {@code .jar} filenames from the given folder.
     */
    public static List<String> getLoadedExtensionFileNames(Plugin plugin, String folderName) {
        return loadedExtensions.getOrDefault(folderName, Collections.emptyList());
    }

    /**
     * Invokes {@code onDisload(Plugin)} on all previously loaded extensions from a given folder.
     * <p>
     * We iterate the preloaded class list per JAR, reflectively find {@code onDisload(Plugin)} if present,
     * and invoke it. Every eligible class is processed (no early break).
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

        for (String fileName : jars) {
            URLClassLoader classLoader = classLoaderCache.get(fileName);
            List<String> classNames = preloadedClassNames.get(fileName);

            if (classLoader == null || classNames == null || classNames.isEmpty()) {
                logger.fine("[" + type + "] No cached class loader or classes for: " + fileName);
                continue;
            }

            for (String fqcn : classNames) {
                try {
                    Class<?> clazz = Class.forName(fqcn, false, classLoader);
                    int mods = clazz.getModifiers();
                    if (clazz.isInterface() || Modifier.isAbstract(mods)) continue;

                    Method onDisloadMethod;
                    try {
                        onDisloadMethod = clazz.getMethod("onDisload", Plugin.class);
                    } catch (NoSuchMethodException e) {
                        continue; // not all classes have this method
                    }

                    Object extensionInstance = clazz.getDeclaredConstructor().newInstance();
                    onDisloadMethod.invoke(extensionInstance, plugin);
                    logger.info("[" + type + "] Disloaded: " + fqcn);

                } catch (Throwable e) {
                    logger.warning("[" + type + "] Failed to disload class: " + fqcn);
                    e.printStackTrace();
                }
            }
        }

        loadedExtensions.remove(folderName);
        // Leave class loaders cached; if you truly want to unload, you could close them here
        // (Java 9+ has URLClassLoader#close), but many plugin environments prefer them kept.
    }
}
