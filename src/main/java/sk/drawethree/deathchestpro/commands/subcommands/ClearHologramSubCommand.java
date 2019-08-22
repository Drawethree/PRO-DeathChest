package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.chest.DeathChestHologram;
import sk.drawethree.deathchestpro.utils.Message;

public class ClearHologramSubCommand extends DeathChestSubCommand {

    public ClearHologramSubCommand() {
        super("clearhologram", "Removes bugged hologram manually.");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("deathchestpro.clearhologram")) {
            p.sendMessage(Message.NO_PERMISSION.getChatMessage());
            return false;
        }

        for (Entity e : p.getNearbyEntities(3, 3, 3)) {
            //We do not want to remove existing holograms, just old ones.
            if (DeathChestHologram.existLine(e)) {
                continue;
            }
            if (e instanceof ArmorStand && e.hasMetadata(DeathChestHologram.ENTITY_METADATA)) {
                e.remove();
            }
        }
        p.sendMessage(Message.PREFIX + "§aCommand successfully processed.");

        return true;
    }
}
