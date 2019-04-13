package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.utils.Message;

public class ListSubCommand extends DeathChestSubCommand {

    public ListSubCommand() {
        super("list", "Opens your DeathChest GUI");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {

            Player p = (Player) sender;
            OfflinePlayer whoChests = p;

            if (args.length == 1) {
                whoChests = Bukkit.getOfflinePlayer(args[0]);
            } else if (args.length > 1) {
                return false;
            }

            boolean hasPerm;

            if (!whoChests.getUniqueId().equals(p.getUniqueId())) {
                hasPerm = p.hasPermission("deathchestpro.list.others");
            } else {
                hasPerm = p.hasPermission("deathchestpro.list");
            }

            if (hasPerm) {
                DeathChestManager.getInstance().openDeathchestList(whoChests, p, 1);
                return true;
            } else {
                p.sendMessage(Message.NO_PERMISSION.getChatMessage());
            }
        }
        return false;
    }
}
