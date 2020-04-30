package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.commands.subcommands.*;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DeathChestCommand implements CommandExecutor, TabCompleter {


    private static final TreeMap<String, DeathChestSubCommand> availableSubCommands = new TreeMap<>();
    private DeathChestPro plugin;


    static {
        availableSubCommands.put("list", new ListSubCommand());
        availableSubCommands.put("reload", new ReloadSubCommand());
        availableSubCommands.put("teleport", new TeleportSubCommand());
        availableSubCommands.put("version", new VersionSubCommand());
        availableSubCommands.put("test", new TestSubCommand());
    }

    public DeathChestCommand(DeathChestPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equalsIgnoreCase("deathchest")) {
            if (args.length > 0) {
                DeathChestSubCommand subCommand = getSubCommand(args[0]);
                if (subCommand != null) {
                    subCommand.execute(this.plugin, sender, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    return invalidUsage(sender);
                }
            } else {
                return invalidUsage(sender);
            }
        }
        return false;
    }

    private boolean invalidUsage(CommandSender sender) {
        sender.sendMessage(DeathChestMessage.INVALID_USAGE.getChatMessage());
        return true;
    }

    private DeathChestSubCommand getSubCommand(String subCommandName) {
        return availableSubCommands.get(subCommandName.toLowerCase());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("deathchest")) {
            return null;
        }

        if (args.length == 1) {
            return availableSubCommands.keySet().stream().filter(s -> !s.equalsIgnoreCase("test")).collect(Collectors.toList());
        }
        return null;
    }
}
