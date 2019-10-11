package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;

public class ReloadSubCommand extends DeathChestSubCommand {

    public ReloadSubCommand() {
        super("reload", "Reloads the plugin");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {
        if (sender.hasPermission("deathchestpro.reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(DeathChestMessage.PREFIX.getMessage() + "Plugin reloaded !");
            return true;
        } else {
            sender.sendMessage(DeathChestMessage.NO_PERMISSION.getChatMessage());
        }
        return false;
    }
}
