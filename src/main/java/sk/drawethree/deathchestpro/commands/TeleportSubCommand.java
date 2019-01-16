package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.managers.DeathChestManager;

public class TeleportSubCommand extends DeathChestSubCommand {

    public TeleportSubCommand() {
        super("teleport");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) {
            return false;
        }
        Player p = (Player) sender;
        DeathChest dc = DeathChestManager.getInstance().getDeathChest(args[0]);
        if (dc != null) {
            dc.teleportPlayer(p);
            return true;
        }
        return false;
    }
}
