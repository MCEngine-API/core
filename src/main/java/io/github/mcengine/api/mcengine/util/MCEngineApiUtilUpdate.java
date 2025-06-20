package io.github.mcengine.api.mcengine.util;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Utility class for checking plugin updates from GitHub or GitLab repositories.
 */
public class MCEngineApiUtilUpdate {

    // Priority list in descending order of stability: RELEASE > SNAPSHOT > ALPHA > BETA
    private static final List<String> LABEL_PRIORITY = List.of("BETA", "ALPHA", "SNAPSHOT", "RELEASE");

    /**
     * Checks for updates for core plugins.
     *
     * @param plugin      Plugin instance.
     * @param logger      Logger to output messages.
     * @param gitPlatform Git platform ("github" or "gitlab").
     * @param org         GitHub/GitLab organization or username.
     * @param repository  Repository name.
     * @param token       GitHub/GitLab API token (can be null).
     */
    public static void checkUpdate(Plugin plugin, Logger logger,
                                   String gitPlatform, String org, String repository, String token) {
        checkUpdate(plugin, logger, "", gitPlatform, org, repository, token);
    }

    /**
     * Checks for updates for plugins with custom log prefix (used by AddOns or DLCs).
     *
     * @param plugin      Plugin instance.
     * @param logger      Logger to output messages.
     * @param prefix      Prefix to prepend to all logs.
     * @param gitPlatform Git platform ("github" or "gitlab").
     * @param org         GitHub/GitLab organization or username.
     * @param repository  Repository name.
     * @param token       GitHub/GitLab API token (can be null).
     */
    public static void checkUpdate(Plugin plugin, Logger logger, String prefix,
                                   String gitPlatform, String org, String repository, String token) {
        switch (gitPlatform.toLowerCase()) {
            case "github" -> checkUpdateGitHub(plugin, logger, prefix, org, repository, token);
            case "gitlab" -> checkUpdateGitLab(plugin, logger, prefix, org, repository, token);
            default -> logger.warning(prefix + "Unknown platform: " + gitPlatform);
        }
    }

    /**
     * Checks for the latest release from GitHub and compares with current plugin version.
     *
     * @param plugin      Plugin instance.
     * @param logger      Logger for output.
     * @param prefix      Prefix to prepend to logs.
     * @param org         GitHub organization or username.
     * @param repository  GitHub repository name.
     * @param githubToken GitHub API token (can be null).
     */
    private static void checkUpdateGitHub(Plugin plugin, Logger logger, String prefix,
                                          String org, String repository, String githubToken) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", org, repository);
        String downloadUrl = String.format("https://github.com/%s/%s/releases", org, repository);
        fetchAndCompareUpdate(plugin, logger, prefix, apiUrl, downloadUrl, githubToken, "application/vnd.github.v3+json", false);
    }

    /**
     * Checks for the latest release from GitLab and compares with current plugin version.
     *
     * @param plugin      Plugin instance.
     * @param logger      Logger for output.
     * @param prefix      Prefix to prepend to logs.
     * @param org         GitLab group or username.
     * @param repository  GitLab repository name.
     * @param gitlabToken GitLab API token (can be null).
     */
    private static void checkUpdateGitLab(Plugin plugin, Logger logger, String prefix,
                                          String org, String repository, String gitlabToken) {
        String apiUrl = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/releases", org, repository);
        String downloadUrl = String.format("https://gitlab.com/%s/%s/-/releases", org, repository);
        fetchAndCompareUpdate(plugin, logger, prefix, apiUrl, downloadUrl, gitlabToken, "application/json", true);
    }

    /**
     * Fetches the latest version from the given API and compares it with the current plugin version.
     *
     * @param plugin       Plugin instance.
     * @param logger       Logger for output.
     * @param prefix       Prefix to prepend to log messages.
     * @param apiUrl       API endpoint for the latest release.
     * @param downloadUrl  Download page URL for display.
     * @param token        Optional API token.
     * @param acceptHeader Accept header value for HTTP request.
     * @param jsonArray    Whether the response is a JSON array (GitLab) or object (GitHub).
     */
    private static void fetchAndCompareUpdate(Plugin plugin, Logger logger, String prefix,
                                              String apiUrl, String downloadUrl,
                                              String token, String acceptHeader, boolean jsonArray) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                con.setRequestMethod("GET");
                if (token != null && !token.trim().isEmpty() && !"null".equalsIgnoreCase(token.trim())) {
                    con.setRequestProperty("Authorization", "Bearer " + token);
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

                String currentVersion = plugin.getDescription().getVersion();
                boolean updateAvailable = isUpdateAvailable(currentVersion, latestVersion);

                if (updateAvailable) {
                    logger.info(prefix + "new update is available!");
                    logger.info(prefix + "Current version: " + currentVersion + " >> Latest: " + latestVersion);
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
     * Compares current and latest version to determine if an update is needed.
     *
     * @param currentVersion Current version string.
     * @param latestVersion  Latest version string.
     * @return True if an update is available.
     */
    @SuppressWarnings("unused")
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
     * Holds parsed version number components and label rank.
     */
    private static class VersionInfo {
        List<Integer> numbers = new ArrayList<>();
        int labelRank = -1;
    }

    /**
     * Parses version into numeric parts and label priority.
     *
     * @param version Version string (e.g., 1.2.3-ALPHA).
     * @return Parsed version info.
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
     * Gets label rank for stability label. Higher value = more stable.
     *
     * @param label Label to evaluate (e.g., ALPHA, RELEASE).
     * @return Rank index, or -1 if not found.
     */
    private static int getLabelRank(String label) {
        for (int i = 0; i < LABEL_PRIORITY.size(); i++) {
            if (label.equalsIgnoreCase(LABEL_PRIORITY.get(i))) {
                return LABEL_PRIORITY.size() - 1 - i; // Higher index = more stable
            }
        }
        return -1;
    }
}
