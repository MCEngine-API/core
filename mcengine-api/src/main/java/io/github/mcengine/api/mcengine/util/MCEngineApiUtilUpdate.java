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

    /**
     * Checks for plugin updates from GitHub or GitLab asynchronously and logs results.
     *
     * @param plugin      The plugin instance.
     * @param logger      A dynamic logger instance (can be plugin.getLogger() or a custom one).
     * @param gitPlatform The git platform ("github" or "gitlab").
     * @param org         The organization or user name.
     * @param repository  The repository name.
     * @param token       The API authentication token (can be null or "null" if not used).
     */
    public static void checkUpdate(Plugin plugin, Logger logger,
                                   String gitPlatform, String org, String repository, String token) {
        switch (gitPlatform.toLowerCase()) {
            case "github":
                checkUpdateGitHub(plugin, logger, org, repository, token);
                break;
            case "gitlab":
                checkUpdateGitLab(plugin, logger, org, repository, token);
                break;
            default:
                logger.warning("Unknown platform: " + gitPlatform);
        }
    }

    /**
     * Initiates update check for GitHub repository asynchronously.
     *
     * @param plugin      The plugin instance.
     * @param logger      Logger to output update information.
     * @param org         GitHub organization or username.
     * @param repository  GitHub repository name.
     * @param githubToken GitHub API token (optional).
     */
    private static void checkUpdateGitHub(Plugin plugin, Logger logger, String org, String repository, String githubToken) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", org, repository);
        String downloadUrl = String.format("https://github.com/%s/%s/releases", org, repository);
        fetchAndCompareUpdate(plugin, logger, apiUrl, downloadUrl, githubToken, "application/vnd.github.v3+json", false);
    }

    /**
     * Initiates update check for GitLab repository asynchronously.
     *
     * @param plugin      The plugin instance.
     * @param logger      Logger to output update information.
     * @param org         GitLab group or username.
     * @param repository  GitLab repository name.
     * @param gitlabToken GitLab API token (optional).
     */
    private static void checkUpdateGitLab(Plugin plugin, Logger logger, String org, String repository, String gitlabToken) {
        String apiUrl = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/releases", org, repository);
        String downloadUrl = String.format("https://gitlab.com/%s/%s/-/releases", org, repository);
        fetchAndCompareUpdate(plugin, logger, apiUrl, downloadUrl, gitlabToken, "application/json", true);
    }

    /**
     * Fetches the latest release from the API asynchronously and compares it with the current plugin version.
     * Logs update information using the provided logger.
     *
     * @param plugin       The plugin instance.
     * @param logger       Logger to log update details.
     * @param apiUrl       The API endpoint URL.
     * @param downloadUrl  The URL to the release download page.
     * @param token        API token (optional).
     * @param acceptHeader The Accept header for the request.
     * @param jsonArray    Whether the API response is a JSON array (true for GitLab).
     */
    private static void fetchAndCompareUpdate(Plugin plugin, Logger logger,
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
                    logger.warning("[UpdateCheck] Could not find release tag from API: " + apiUrl);
                    return;
                }

                String version = plugin.getDescription().getVersion();
                boolean changed = isUpdateAvailable(version, latestVersion);

                if (changed) {
                    logger.info("§6A new update is available!");
                    logger.info("Current version: " + version + " >> Latest: " + latestVersion);
                    logger.info("Download: " + downloadUrl);
                } else {
                    logger.info("No updates found. You are running the latest version.");
                }

            } catch (Exception ex) {
                logger.warning("[UpdateCheck] [" + (apiUrl.contains("github") ? "GitHub" : "GitLab") + "] Could not check updates: " + ex.getMessage());
            }
        });
    }

    /**
     * Compares the current plugin version with the latest version to determine if an update is available.
     * Supports complex formats like:
     *   - 1.0.0-SNAPSHOT
     *   - 1.0.0-1-SNAPSHOT
     *   - 1.0.0-2-2-1-SNAPSHOT
     *   - 1.0.0-RELEASE
     *   - 1.0.1-2-2-1-1-SNAPSHOT
     *
     * Comparison rules:
     * - RELEASE is always considered the most stable and highest version
     * - Numeric version parts are compared in order
     * - SNAPSHOT is considered lower than RELEASE
     *
     * @param currentVersion The currently installed version.
     * @param latestVersion  The latest version from the API.
     * @return true if an update is available; false otherwise.
     */
    private static boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        if (currentVersion.equalsIgnoreCase(latestVersion)) return false;

        // Normalize version strings (remove -RELEASE or -SNAPSHOT for comparison)
        String normCurrent = currentVersion.replace("-RELEASE", "");
        String normLatest = latestVersion.replace("-RELEASE", "");

        List<Integer> currentParts = parseVersion(normCurrent);
        List<Integer> latestParts = parseVersion(normLatest);

        // Compare version numbers part-by-part
        int maxLen = Math.max(currentParts.size(), latestParts.size());
        for (int i = 0; i < maxLen; i++) {
            int curr = i < currentParts.size() ? currentParts.get(i) : 0;
            int latest = i < latestParts.size() ? latestParts.get(i) : 0;
            if (latest > curr) return true;
            if (latest < curr) return false;
        }

        // RELEASE is newer than any SNAPSHOT with same version parts
        boolean currentIsSnapshot = currentVersion.contains("SNAPSHOT");
        boolean latestIsSnapshot = latestVersion.contains("SNAPSHOT");

        return currentIsSnapshot && !latestIsSnapshot;
    }

    /**
     * Parses a version string like "1.0.1-2-2-1" into a list of integers.
     *
     * @param version The version string (without -RELEASE or -SNAPSHOT).
     * @return List of version components as integers.
     */
    private static List<Integer> parseVersion(String version) {
        List<Integer> parts = new ArrayList<>();
        for (String part : version.replace("-SNAPSHOT", "").split("[-\\.]")) {
            try {
                parts.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                parts.add(0); // fallback for malformed versions
            }
        }
        return parts;
    }
}
