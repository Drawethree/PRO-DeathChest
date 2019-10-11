package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.utils.comp.CompMaterial;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;

public class TestSubCommand extends DeathChestSubCommand {

    public TestSubCommand() {
        super("test", "Test command.");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {

        if (sender instanceof Player) {

            Player p = (Player) sender;

            if(!DeathChestPro.getInstance().getDescription().getVersion().contains("TEST")) {
                p.sendMessage(DeathChestMessage.PREFIX + "§cThis is not a test version. This command is not supported.");
                return false;
            }

            p.getInventory().clear();

            p.getInventory().setHelmet(new ItemStack(CompMaterial.DIAMOND_HELMET.toMaterial(),1));
            p.getInventory().setChestplate(new ItemStack(CompMaterial.DIAMOND_CHESTPLATE.toMaterial(),1));
            p.getInventory().setLeggings(new ItemStack(CompMaterial.DIAMOND_LEGGINGS.toMaterial(),1));
            p.getInventory().setBoots(new ItemStack(CompMaterial.DIAMOND_BOOTS.toMaterial(),1));
            p.getInventory().addItem(new ItemStack(CompMaterial.OAK_PLANKS.toMaterial(), 64));
            p.getInventory().addItem(new ItemStack(CompMaterial.APPLE.toMaterial(), 16));
            p.getInventory().addItem(new ItemStack(CompMaterial.STONE.toMaterial(), 64));
            p.getInventory().addItem(new ItemStack(CompMaterial.DIAMOND_SWORD.toMaterial(), 1));
            p.setLevel(10);

            p.sendMessage(DeathChestMessage.PREFIX + "§aTest items given. Sorry, but in order to see how it works, you need to die :(");

            new BukkitRunnable() {

                @Override
                public void run() {
                    p.damage(p.getHealth());
                }
            }.runTaskLater(DeathChestPro.getInstance(), 40L);
            return true;
        }
        return false;
    }
}
