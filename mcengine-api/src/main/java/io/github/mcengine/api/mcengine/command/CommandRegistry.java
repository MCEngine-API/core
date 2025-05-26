package io.github.mcengine.api.mcengine.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry to dynamically register subcommands under root commands like /ai.
 */
public class CommandRegistry implements CommandExecutor {

    private static final Map<String, Map<String, SubCommand>> commandMap = new HashMap<>();

    public static void register(String root, String sub, SubCommand command) {
        commandMap.computeIfAbsent(root.toLowerCase(), k -> new HashMap<>())
                  .put(sub.toLowerCase(), command);
    }

    public static void unregister(String root, String sub) {
        Map<String, SubCommand> subCommands = commandMap.get(root.toLowerCase());
        if (subCommands != null) {
            subCommands.remove(sub.toLowerCase());
        }
    }

    public static boolean execute(String root, CommandSender sender, String[] args) {
        Map<String, SubCommand> subCommands = commandMap.get(root.toLowerCase());
        if (subCommands == null || args.length == 0) return false;

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(sub);
        if (subCommand == null) return false;

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        return subCommand.execute(sender, subArgs);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return execute(label.toLowerCase(), sender, args);
    }

    public interface SubCommand {
        boolean execute(CommandSender sender, String[] args);
    }
}
