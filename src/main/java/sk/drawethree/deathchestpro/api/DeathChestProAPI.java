package sk.drawethree.deathchestpro.api;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import sk.drawethree.deathchestpro.chest.DeathChest;

import java.util.List;

/**
 * DeathChestPro API Interface
 */
public interface DeathChestProAPI {

	/**
	 * Function to get all player's DeathChests
	 * @param p - Player
	 * @return List of active player DeathChests
	 * @return Empty ArrayList if player does not have DeathChests
	 */
	List<DeathChest> getPlayerDeathChests(OfflinePlayer p);

	/**
	 * Function to get DeathChest at given Location
	 * @param loc - Location
	 * @return DeathChest at provided location
	 * @return null if at location is no DeathChest
	 */
	DeathChest getDeathChestByLocation(Location loc);

}
