package sk.drawethree.deathchestpro.chest.tasks;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.utils.FireworkUtil;

public class ChestRemoveTask extends BukkitRunnable {

	private DeathChest deathChest;
	private int nextFireworkIn;

	public ChestRemoveTask(DeathChest deathChest) {
		this.deathChest = deathChest;
		this.nextFireworkIn = this.deathChest.getPlugin().getSettings().getFireworkInterval();
	}

	@Override
	public void run() {
		if (this.deathChest.getTimeLeft() == 0) {
			this.deathChest.removeDeathChest(true);
			this.cancel();
		} else {
			this.deathChest.setTimeLeft(this.deathChest.getTimeLeft()-1);

			if (this.deathChest.getLocation().getBlock().getType() != Material.CHEST && !this.deathChest.getPlugin().getSettings().isAllowBreakChests()) {
				this.deathChest.getLocation().getBlock().setType(Material.CHEST);
				this.deathChest.getLocation().getBlock().getState().update(true);
			}

			if (this.deathChest.getPlugin().getSettings().isDeathchestFireworks() && this.deathChest.getLocation().getChunk().isLoaded()) {
				this.nextFireworkIn--;
				if (this.nextFireworkIn == 0) {
					FireworkUtil.spawnRandomFirework(this.deathChest.getLocation());
					this.nextFireworkIn = this.deathChest.getPlugin().getSettings().getFireworkInterval();
				}
			}
		}
	}
}
