package dev.drawethree.deathchestpro.commands;

import dev.drawethree.deathchestpro.DeathChestPro;
import lombok.Getter;
import org.bukkit.command.CommandSender;


@Getter
public abstract class DeathChestSubCommand {

    private String subCommandName;
    private String description;

    public DeathChestSubCommand(String subCommandName, String description) {
        this.subCommandName = subCommandName;
        this.description = description;
    }

    public abstract boolean execute(DeathChestPro plugin, CommandSender sender, String[] args);

}
