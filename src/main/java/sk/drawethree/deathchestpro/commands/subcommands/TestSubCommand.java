package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.DeathChestPro;

public class TestSubCommand extends DeathChestSubCommand {

    public TestSubCommand() {
        super("test", "Test command.");
    }

    @Override
    public boolean execute(DeathChestPro plugin, CommandSender sender, String[] args) {

        if (sender.isOp() && (sender instanceof Player)) {
            Player p = (Player) sender;
            return true;
        }

        return false;
    }
}
