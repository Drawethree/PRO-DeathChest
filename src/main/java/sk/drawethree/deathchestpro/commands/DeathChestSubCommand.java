package sk.drawethree.deathchestpro.commands;

import org.bukkit.command.CommandSender;

public abstract class DeathChestSubCommand {

    private String subCommandName;

    public DeathChestSubCommand(String subCommandName) {
        this.subCommandName = subCommandName;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String getSubCommandName() {
        return subCommandName;
    }
}
