package io.github.mcengine.api.mcengine.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String root = label.toLowerCase(); // e.g., "ai", "task", "report"
        Map<String, SubCommand> subCommands = commandMap.get(root);

        if (subCommands == null) {
            sender.sendMessage("§cNo handlers registered for /" + root);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /" + root + " <subcommand>");
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(sub);

        if (subCommand != null) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            return subCommand.execute(sender, subArgs);
        }

        sender.sendMessage("§cUnknown subcommand: " + sub);
        return true;
    }

    public interface SubCommand {
        boolean execute(CommandSender sender, String[] args);
    }
}
