package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.CommandSender;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.utils.Message;

public class ReloadSubCommand extends DeathChestSubCommand {

    public ReloadSubCommand() {
        super("reload");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("deathchestpro.reload")) {
            DeathChestPro.getInstance().reloadPlugin();
            sender.sendMessage(Message.PREFIX.getMessage() + "Plugin reloaded !");
            return true;
        } else {
            sender.sendMessage(Message.NO_PERMISSION.getChatMessage());
        }
        return false;
    }
}
