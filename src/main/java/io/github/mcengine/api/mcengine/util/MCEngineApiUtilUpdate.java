package io.github.mcengine.api.mcengine.util;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for checking updates from GitHub or GitLab repositories.
 */
public class MCEngineApiUtilUpdate {

    private static final List<String> LABEL_PRIORITY = List.of("BETA", "ALPHA", "SNAPSHOT", "RELEASE");

    /**
     * Checks for plugin updates from GitHub or GitLab asynchronously and logs results.
     * Used by core plugins.
     *
     * @param plugin      The plugin instance.
     * @param logger      The logger instance to log messages.
     * @param gitPlatform The git platform ("github" or "gitlab").
     * @param org         The organization or user name.
     * @param repository  The repository name.
     * @param token       The API authentication token (can be null or "null" if not used).
     */
    public static void checkUpdate(Plugin plugin, Logger logger,
                                   String gitPlatform, String org, String repository, String token) {
        checkUpdate(plugin, logger, "", gitPlatform, org, repository, token);
    }

    /**
     * Checks for plugin updates from GitHub or GitLab asynchronously and logs results with a prefixed format.
     * Used by AddOns or DLCs to customize log format.
     *
     * @param plugin      The plugin instance.
     * @param logger      The logger instance to log messages.
     * @param prefix      A prefix string to prepend to all log messages.
     * @param gitPlatform The git platform ("github" or "gitlab").
     * @param org         The organization or user name.
     * @param repository  The repository name.
     * @param token       The API authentication token (can be null or "null" if not used).
     */
    public static void checkUpdate(Plugin plugin, Logger logger, String prefix,
                                   String gitPlatform, String org, String repository, String token) {
        switch (gitPlatform.toLowerCase()) {
            case "github":
                checkUpdateGitHub(plugin, logger, prefix, org, repository, token);
                break;
            case "gitlab":
                checkUpdateGitLab(plugin, logger, prefix, org, repository, token);
                break;
            default:
                logger.warning(prefix + "Unknown platform: " + gitPlatform);
        }
    }

    private static void checkUpdateGitHub(Plugin plugin, Logger logger, String prefix,
                                          String org, String repository, String githubToken) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", org, repository);
        String downloadUrl = String.format("https://github.com/%s/%s/releases", org, repository);
        fetchAndCompareUpdate(plugin, logger, prefix, apiUrl, downloadUrl, githubToken, "application/vnd.github.v3+json", false);
    }

    private static void checkUpdateGitLab(Plugin plugin, Logger logger, String prefix,
                                          String org, String repository, String gitlabToken) {
        String apiUrl = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/releases", org, repository);
        String downloadUrl = String.format("https://gitlab.com/%s/%s/-/releases", org, repository);
        fetchAndCompareUpdate(plugin, logger, prefix, apiUrl, downloadUrl, gitlabToken, "application/json", true);
    }

    private static void fetchAndCompareUpdate(Plugin plugin, Logger logger, String prefix,
                                              String apiUrl, String downloadUrl,
                                              String token, String acceptHeader, boolean jsonArray) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                con.setRequestMethod("GET");
                if (token != null && !token.trim().isEmpty() && !"null".equalsIgnoreCase(token.trim())) {
                    con.setRequestProperty("Authorization", "token " + token);
                }
                con.setRequestProperty("Accept", acceptHeader);
                con.setDoOutput(true);

                JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
                String latestVersion;

                if (jsonArray) {
                    var jsonArrayObj = JsonParser.parseReader(reader).getAsJsonArray();
                    latestVersion = jsonArrayObj.size() > 0 ? jsonArrayObj.get(0).getAsJsonObject().get("tag_name").getAsString() : null;
                } else {
                    var jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    latestVersion = jsonObject.get("tag_name").getAsString();
                }

                if (latestVersion == null) {
                    logger.warning(prefix + "[UpdateCheck] Could not find release tag from API: " + apiUrl);
                    return;
                }

                String version = plugin.getDescription().getVersion();
                boolean changed = isUpdateAvailable(version, latestVersion);

                if (changed) {
                    logger.info(prefix + "§6A new update is available!");
                    logger.info(prefix + "Current version: " + version + " >> Latest: " + latestVersion);
                    logger.info(prefix + "Download: " + downloadUrl);
                } else {
                    logger.info(prefix + "No updates found. You are running the latest version.");
                }

            } catch (Exception ex) {
                logger.warning(prefix + "[UpdateCheck] [" + (apiUrl.contains("github") ? "GitHub" : "GitLab") + "] Could not check updates: " + ex.getMessage());
            }
        });
    }

    /**
     * Compares the current plugin version with the latest version to determine if an update is available.
     * Uses both numerical and label rank (e.g., ALPHA > BETA > SNAPSHOT > RELEASE).
     *
     * @param currentVersion The currently installed version.
     * @param latestVersion  The latest version from the API.
     * @return true if an update is available; false otherwise.
     */
    private static boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        if (currentVersion.equalsIgnoreCase(latestVersion)) return false;

        VersionInfo current = extractVersionParts(currentVersion);
        VersionInfo latest = extractVersionParts(latestVersion);

        int maxLen = Math.max(current.numbers.size(), latest.numbers.size());
        for (int i = 0; i < maxLen; i++) {
            int curr = i < current.numbers.size() ? current.numbers.get(i) : 0;
            int next = i < latest.numbers.size() ? latest.numbers.get(i) : 0;
            if (next > curr) return true;
            if (next < curr) return false;
        }

        return latest.labelRank > current.labelRank;
    }

    /**
     * Represents a parsed version with numbers and label rank.
     */
    private static class VersionInfo {
        List<Integer> numbers = new ArrayList<>();
        int labelRank = -1;
    }

    /**
     * Extracts version components and ranks the stability label.
     *
     * @param version Version string like 1.0.0-1-SNAPSHOT
     * @return Parsed version information
     */
    private static VersionInfo extractVersionParts(String version) {
        VersionInfo info = new VersionInfo();
        String[] parts = version.split("[-\\.]");

        for (String part : parts) {
            try {
                info.numbers.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                int rank = getLabelRank(part);
                if (rank > info.labelRank) {
                    info.labelRank = rank;
                }
            }
        }

        return info;
    }

    /**
     * Returns the priority of the given label.
     * Later label in the list is considered newer.
     *
     * @param label Version label (e.g., RELEASE, SNAPSHOT)
     * @return Rank index or -1 if unknown
     */
    private static int getLabelRank(String label) {
        for (int i = 0; i < LABEL_PRIORITY.size(); i++) {
            if (label.equalsIgnoreCase(LABEL_PRIORITY.get(i))) {
                return i; // Later = newer
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        
    }
}
