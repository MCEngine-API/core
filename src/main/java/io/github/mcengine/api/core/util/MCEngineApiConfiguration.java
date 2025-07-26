package io.github.mcengine.api.core.util;

import java.util.Map;

/**
 * Utility class for accessing configuration values within the MCEngine plugin framework.
 * 
 * <p>This class provides static methods to retrieve values from a nested configuration structure
 * using a parent path and a variable name. The supported return types include:
 * {@code String}, {@code boolean}, {@code int}, {@code long}, {@code double}, and {@code Object}.
 *
 * <p>The configuration should be a hierarchical map (e.g., parsed from a YAML or JSON file).
 */
public class MCEngineApiConfiguration {

    /**
     * Retrieves a string value from the configuration.
     *
     * @param path     the parent path key (e.g., "tools.threadpool")
     * @param variable the specific variable name (e.g., "enable")
     * @param config   the configuration map
     * @return the string value if found, otherwise {@code null}
     */
    public static String getConfigString(String path, String variable, Map<String, Object> config) {
        Object value = getValue(path, variable, config);
        return value instanceof String ? (String) value : null;
    }

    /**
     * Retrieves a boolean value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the boolean value, or {@code false} if not found or not a boolean
     */
    public static boolean getConfigBoolean(String path, String variable, Map<String, Object> config) {
        Object value = getValue(path, variable, config);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    /**
     * Retrieves an integer value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the integer value, or {@code 0} if not found or not an integer
     */
    public static int getConfigInt(String path, String variable, Map<String, Object> config) {
        Object value = getValue(path, variable, config);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /**
     * Retrieves a long value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the long value, or {@code 0L} if not found or not a number
     */
    public static long getConfigLong(String path, String variable, Map<String, Object> config) {
        Object value = getValue(path, variable, config);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /**
     * Retrieves a double value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the double value, or {@code 0.0} if not found or not a number
     */
    public static double getConfigDouble(String path, String variable, Map<String, Object> config) {
        Object value = getValue(path, variable, config);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    /**
     * Retrieves a generic object value from the configuration.
     *
     * @param path     the parent path key
     * @param variable the variable name
     * @param config   the configuration map
     * @return the object value, or {@code null} if not found
     */
    public static Object getConfigObject(String path, String variable, Map<String, Object> config) {
        return getValue(path, variable, config);
    }

    /**
     * Internal method to get a nested value from the configuration.
     *
     * @param path     the parent key, supporting dot notation
     * @param variable the variable to fetch
     * @param config   the full configuration map
     * @return the object if found, otherwise {@code null}
     */
    @SuppressWarnings("unchecked")
    private static Object getValue(String path, String variable, Map<String, Object> config) {
        if (config == null || path == null || variable == null) {
            return null;
        }

        String[] keys = path.split("\\.");
        Map<String, Object> current = config;

        for (String key : keys) {
            Object child = current.get(key);
            if (!(child instanceof Map)) {
                return null;
            }
            current = (Map<String, Object>) child;
        }

        return current.get(variable);
    }
}
