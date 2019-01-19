package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TestSubCommand extends DeathChestSubCommand {

    public TestSubCommand() {
        super("test", "Fills your inventory with items.");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender.isOp() && (sender instanceof Player)) {
            Player p = (Player) sender;
            while (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(new ItemStack(Material.STONE));
            }
            p.sendMessage("§aItems received.");
            return true;
        }
        return false;
    }
}
