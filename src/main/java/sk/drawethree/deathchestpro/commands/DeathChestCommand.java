package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import sk.drawethree.deathchestpro.commands.subcommands.*;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.Arrays;

public class DeathChestCommand implements CommandExecutor {

    private static DeathChestSubCommand[] availableSubCommands = new DeathChestSubCommand[]{
            new ListSubCommand(),
            new ReloadSubCommand(),
            new TeleportSubCommand(),
            new TestSubCommand()
    };

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equalsIgnoreCase("deathchest")) {
            if (args.length > 0) {
                DeathChestSubCommand subCommand = getSubCommand(args[0]);
                if (subCommand != null) {
                    subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
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
        for (DeathChestSubCommand subCommand : availableSubCommands) {
            if (subCommand.getSubCommandName().equalsIgnoreCase(subCommandName)) {
                return subCommand;
            }
        }
        return null;
    }
}
