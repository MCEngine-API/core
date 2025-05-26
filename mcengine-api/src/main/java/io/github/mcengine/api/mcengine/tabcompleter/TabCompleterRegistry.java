package io.github.mcengine.api.mcengine.tabcompleter;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TabCompleterRegistry allows dynamic registration of tab completers for subcommands.
 * Works alongside CommandRegistry.
 */
public class TabCompleterRegistry {

    private static final Map<String, Map<String, TabCompleter>> tabCompleters = new HashMap<>();

    /**
     * Registers a tab completer for a given root and subcommand.
     *
     * @param root      The root command (e.g. "ai").
     * @param sub       The subcommand under the root (e.g. "chat").
     * @param completer The TabCompleter to use.
     */
    public static void registerTabCompleter(String root, String sub, TabCompleter completer) {
        tabCompleters.computeIfAbsent(root.toLowerCase(), k -> new HashMap<>())
                     .put(sub.toLowerCase(), completer);
    }

    /**
     * Retrieves tab completions for the given root command and arguments.
     *
     * @param root   The root command (e.g. "ai").
     * @param sender The sender requesting completion.
     * @param args   The command arguments.
     * @return A list of completion suggestions.
     */
    public static List<String> getCompletions(String root, CommandSender sender, String[] args) {
        Map<String, TabCompleter> subCompleters = tabCompleters.get(root.toLowerCase());
        if (subCompleters == null || args.length == 0) return null;

        String sub = args[0].toLowerCase();
        TabCompleter completer = subCompleters.get(sub);
        if (completer == null) return null;

        // Strip the subcommand and pass remaining args
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        return completer.onTabComplete(sender, null, sub, subArgs);
    }
}
