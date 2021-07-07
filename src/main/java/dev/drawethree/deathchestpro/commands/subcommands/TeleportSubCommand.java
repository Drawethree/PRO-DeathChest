package dev.drawethree.deathchestpro.commands.subcommands;

import dev.drawethree.deathchestpro.DeathChestPro;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.drawethree.deathchestpro.chest.DeathChest;
import dev.drawethree.deathchestpro.commands.DeathChestSubCommand;

public class TeleportSubCommand extends DeathChestSubCommand {

    public TeleportSubCommand() {
        super("teleport", "Teleport to DeathChest");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) {
            return false;
        }
        Player p = (Player) sender;
        DeathChest dc = plugin.getDeathChestManager().getDeathChest(args[0]);

        if (dc != null) {
            dc.teleportPlayer(p);
            return true;
        }

        return false;
    }
}
