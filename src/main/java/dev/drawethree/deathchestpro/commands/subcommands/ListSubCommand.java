package dev.drawethree.deathchestpro.commands.subcommands;

import dev.drawethree.deathchestpro.DeathChestPro;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.drawethree.deathchestpro.commands.DeathChestSubCommand;
import dev.drawethree.deathchestpro.enums.DeathChestMessage;

public class ListSubCommand extends DeathChestSubCommand {

    public ListSubCommand() {
        super("list", "Opens your DeathChest GUI");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {
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
                plugin.getDeathChestManager().openDeathchestList(whoChests, p, 1);
                return true;
            } else {
                p.sendMessage(DeathChestMessage.NO_PERMISSION.getChatMessage());
            }
        }
        return false;
    }
}
