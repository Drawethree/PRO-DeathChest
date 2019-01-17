package sk.drawethree.deathchestpro.commands.subcommands;

import org.bukkit.command.CommandSender;

public abstract class DeathChestSubCommand {

    private String subCommandName;
    private String description;

    public DeathChestSubCommand(String subCommandName, String description) {
        this.subCommandName = subCommandName;
        this.description = description;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String getSubCommandName() {
        return subCommandName;
    }

    public String getDescription() {
        return description;
    }
}
