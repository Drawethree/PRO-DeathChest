package sk.drawethree.deathchestpro.api;


import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;

import java.util.List;

public class DeathChestProAPIImpl implements DeathChestProAPI {


	private final DeathChestPro plugin;

	public DeathChestProAPIImpl(DeathChestPro plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<DeathChest> getPlayerDeathChests(OfflinePlayer p) {
		return this.plugin.getDeathChestManager().getPlayerDeathChests(p);
	}

	@Override
	public DeathChest getDeathChestByLocation(Location loc) {
		return this.plugin.getDeathChestManager().getDeathChestByLocation(loc);
	}
}
