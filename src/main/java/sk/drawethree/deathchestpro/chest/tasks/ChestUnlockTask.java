package sk.drawethree.deathchestpro.chest.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import sk.drawethree.deathchestpro.chest.DeathChest;

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
