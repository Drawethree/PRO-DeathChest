package dev.drawethree.deathchestpro.chest.tasks;

import dev.drawethree.deathchestpro.chest.DeathChest;
import dev.drawethree.deathchestpro.utils.FireworkUtil;
import dev.drawethree.deathchestpro.utils.LocationUtil;
import org.bukkit.scheduler.BukkitRunnable;

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
			this.deathChest.removeDeathChest(true, DeathChest.RemovalCause.EXPIRE);
			this.cancel();
		} else {
			this.deathChest.setTimeLeft(this.deathChest.getTimeLeft()-1);

			if(deathChest.isUnloaded()) {
				return;
			}

			if (this.deathChest.getLocation().getBlock().getType() != this.deathChest.getChestType() && !this.deathChest.getPlugin().getSettings().isAllowBreakChests()) {
				this.deathChest.getLocation().getBlock().setType(this.deathChest.getChestType());
				this.deathChest.getLocation().getBlock().getState().update(true);
			}

			if (this.deathChest.getPlugin().getSettings().isDeathchestFireworks() && this.deathChest.getLocation().getChunk().isLoaded()) {
				this.nextFireworkIn--;
				if (this.nextFireworkIn == 0) {
					FireworkUtil.spawnRandomFirework(LocationUtil.getCenter(this.deathChest.getLocation().clone().add(0,1,0)));
					this.nextFireworkIn = this.deathChest.getPlugin().getSettings().getFireworkInterval();
				}
			}
		}
	}
}
