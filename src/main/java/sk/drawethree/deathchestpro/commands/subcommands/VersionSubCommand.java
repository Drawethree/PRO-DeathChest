package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.commands.DeathChestSubCommand;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;

public class VersionSubCommand extends DeathChestSubCommand {

    public VersionSubCommand() {
        super("version", "Show current plugin version");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {
        sender.sendMessage(DeathChestMessage.PREFIX.getMessage() + "You are running PRO DeathChest version " + plugin.getDescription().getVersion());
        return true;
    }
}
