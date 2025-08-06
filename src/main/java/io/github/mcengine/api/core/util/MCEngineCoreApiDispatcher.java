package io.github.mcengine.api.core.util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

/**
 * Internal command dispatcher for managing namespaces and subcommands with tab completion.
 */
public class MCEngineCoreApiDispatcher {

    /**
     * Map of namespaces to their command handlers.
     */
    private final Map<String, NamespaceHandler> namespaces = new HashMap<>();

    /**
     * Map of namespace -> subcommand -> tab completer.
     */
    private final Map<String, Map<String, TabCompleter>> tabCompleters = new HashMap<>();

    /**
     * Registers a new namespace if it does not already exist.
     *
     * @param namespace the unique namespace key (e.g., "plugin1")
     */
    public void registerNamespace(String namespace) {
        namespaces.putIfAbsent(namespace.toLowerCase(), new NamespaceHandler(namespace));
    }

    /**
     * Registers a subcommand handler under a given namespace.
     *
     * @param namespace the namespace
     * @param name      the subcommand name
     * @param executor  the executor handling this subcommand
     */
    public void registerSubCommand(String namespace, String name, CommandExecutor executor) {
        NamespaceHandler handler = namespaces.get(namespace.toLowerCase());
        if (handler != null) {
            handler.registerSubCommand(name.toLowerCase(), executor);
        }
    }

    /**
     * Registers a tab completer for a specific subcommand under a namespace.
     *
     * @param namespace    the namespace
     * @param subcommand   the subcommand
     * @param tabCompleter the tab completer to be invoked
     */
    public void registerSubTabCompleter(String namespace, String subcommand, TabCompleter tabCompleter) {
        tabCompleters
            .computeIfAbsent(namespace.toLowerCase(), n -> new HashMap<>())
            .put(subcommand.toLowerCase(), tabCompleter);
    }

    /**
     * Sets a fallback executor for a namespace.
     *
     * @param namespace the namespace
     * @param executor  the fallback command executor
     */
    public void bindNamespaceToCommand(String namespace, CommandExecutor executor) {
        NamespaceHandler handler = namespaces.get(namespace.toLowerCase());
        if (handler != null) {
            handler.setFallbackExecutor(executor);
        }
    }

    /**
     * Retrieves the CommandExecutor associated with a namespace.
     *
     * @param namespace the namespace
     * @return the namespace handler as CommandExecutor
     */
    public CommandExecutor getDispatcher(String namespace) {
        return namespaces.get(namespace.toLowerCase());
    }

    /**
     * Internal class that handles command execution and tab completion for a namespace.
     */
    private class NamespaceHandler implements CommandExecutor, TabCompleter {

        /**
         * The namespace key this handler is responsible for.
         */
        private final String namespace;

        /**
         * Map of subcommands and their executors.
         */
        private final Map<String, CommandExecutor> subcommands = new HashMap<>();

        /**
         * Fallback executor if no subcommand matches.
         */
        private CommandExecutor fallbackExecutor;

        /**
         * Constructs a handler for the given namespace.
         *
         * @param namespace the namespace
         */
        public NamespaceHandler(String namespace) {
            this.namespace = namespace.toLowerCase();
        }

        /**
         * Registers a subcommand executor.
         *
         * @param name     the subcommand name
         * @param executor the executor
         */
        public void registerSubCommand(String name, CommandExecutor executor) {
            subcommands.put(name.toLowerCase(), executor);
        }

        /**
         * Sets the fallback executor.
         *
         * @param executor fallback executor
         */
        public void setFallbackExecutor(CommandExecutor executor) {
            this.fallbackExecutor = executor;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /" + label + " <subcommand>");
                return true;
            }

            String sub = args[0].toLowerCase();
            CommandExecutor exec = subcommands.get(sub);

            if (exec != null) {
                return exec.onCommand(sender, command, label, args);
            }

            if (fallbackExecutor != null) {
                return fallbackExecutor.onCommand(sender, command, label, args);
            }

            sender.sendMessage("Unknown subcommand: " + sub);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                // Suggest subcommands
                String prefix = args[0].toLowerCase();
                List<String> suggestions = new ArrayList<>();
                for (String sub : subcommands.keySet()) {
                    if (sub.startsWith(prefix)) {
                        suggestions.add(sub);
                    }
                }
                return suggestions;
            }

            if (args.length >= 2) {
                String sub = args[0].toLowerCase();
                Map<String, TabCompleter> subMap = tabCompleters.get(namespace);
                if (subMap != null) {
                    TabCompleter completer = subMap.get(sub);
                    if (completer != null) {
                        return completer.onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
                    }
                }
            }

            return Collections.emptyList();
        }
    }
}
