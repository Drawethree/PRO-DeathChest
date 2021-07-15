package dev.drawethree.deathchestpro.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeathChestPreCreateEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final List<ItemStack> contents;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Event that's called when player dies and deathchest is about to spawn.
	 * You can modify here the items, that will be included in the deathchest
	 * @param who
	 * @param contents
	 */
	public DeathChestPreCreateEvent(Player who, List<ItemStack> contents) {
		super(who);
		this.contents = contents;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
