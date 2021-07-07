package dev.drawethree.deathchestpro.chest.tasks;

import dev.drawethree.deathchestpro.chest.DeathChest;
import org.bukkit.scheduler.BukkitRunnable;

public class ChestUnlockTask extends BukkitRunnable {

	private DeathChest deathChest;

	public ChestUnlockTask(DeathChest deathChest) {
		this.deathChest = deathChest;
	}

	@Override
	public void run() {
		this.deathChest.unlock();
	}
}
