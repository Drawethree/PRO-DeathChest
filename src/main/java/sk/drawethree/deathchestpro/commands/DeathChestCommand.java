package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.commands.subcommands.DeathChestSubCommand;
import sk.drawethree.deathchestpro.commands.subcommands.ListSubCommand;
import sk.drawethree.deathchestpro.commands.subcommands.ReloadSubCommand;
import sk.drawethree.deathchestpro.commands.subcommands.TeleportSubCommand;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.Arrays;
import java.util.TreeMap;

public class DeathChestCommand implements CommandExecutor {


    private static final TreeMap<String, DeathChestSubCommand> availableSubCommands = new TreeMap<>();
    private DeathChestPro plugin;


    static {
        availableSubCommands.put("list", new ListSubCommand());
        availableSubCommands.put("reload", new ReloadSubCommand());
        availableSubCommands.put("teleport", new TeleportSubCommand());
        //availableSubCommands.put("test", new TestSubCommand());
    }

    public DeathChestCommand(DeathChestPro plugin) {
        this.plugin =  plugin;
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
        sender.sendMessage(Message.INVALID_USAGE.getChatMessage());
        return true;
    }

    private DeathChestSubCommand getSubCommand(String subCommandName) {
        return availableSubCommands.get(subCommandName.toLowerCase());
    }
}
