package dev.drawethree.deathchestpro.chest.tasks;

import dev.drawethree.deathchestpro.chest.DeathChest;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramUpdateTask extends BukkitRunnable {

	private DeathChest deathChest;

	public HologramUpdateTask(DeathChest deathChest) {
		this.deathChest = deathChest;
	}

	@Override
	public void run() {
		this.deathChest.updateHologram();
	}
}
