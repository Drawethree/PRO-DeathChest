package dev.drawethree.deathchestpro.commands.subcommands;

import dev.drawethree.deathchestpro.DeathChestPro;
import org.bukkit.command.CommandSender;
import dev.drawethree.deathchestpro.commands.DeathChestSubCommand;
import dev.drawethree.deathchestpro.enums.DeathChestMessage;

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
