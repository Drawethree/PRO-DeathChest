package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.utils.Message;

public class ListSubCommand extends DeathChestSubCommand {

    public ListSubCommand() {
        super("list");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.hasPermission("deathchestpro.list")) {
                DeathChestManager.getInstance().openDeathchestList(p, 1);
                return true;
            } else {
                p.sendMessage(Message.NO_PERMISSION.getChatMessage());
            }
        }
        return false;
    }
}
