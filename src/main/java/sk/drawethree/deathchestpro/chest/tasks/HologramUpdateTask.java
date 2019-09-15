package sk.drawethree.deathchestpro.chest.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import sk.drawethree.deathchestpro.chest.DeathChest;

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
