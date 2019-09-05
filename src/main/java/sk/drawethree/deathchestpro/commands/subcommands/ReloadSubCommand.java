package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.utils.Message;

public class ReloadSubCommand extends DeathChestSubCommand {

    public ReloadSubCommand() {
        super("reload", "Reloads the plugin");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {
        if (sender.hasPermission("deathchestpro.reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(Message.PREFIX.getMessage() + "Plugin reloaded !");
            return true;
        } else {
            sender.sendMessage(Message.NO_PERMISSION.getChatMessage());
        }
        return false;
    }
}
