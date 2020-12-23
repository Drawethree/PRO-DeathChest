package sk.drawethree.deathchestpro.misc;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.utils.Time;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
public class DCExpansion extends PlaceholderExpansion {

	private DeathChestPro plugin;

	public DCExpansion(DeathChestPro plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean persist(){
		return true;
	}

	@Override
	public boolean canRegister(){
		return true;
	}

	@Override
	public String getAuthor(){
		return "Drawethree";
	}

	@Override
	public String getIdentifier(){
		return "deathchestpro";
	}

	@Override
	public String getVersion(){
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier){

		if(player == null){
			return "";
		}

		Optional<DeathChest> firstDeathChest = this.plugin.getDeathChestManager().getPlayerDeathChests(player).stream().findFirst();

		if (!firstDeathChest.isPresent()) {
			return "";
		}


		if(identifier.equalsIgnoreCase("expire")) {
			return new Time(firstDeathChest.get().getTimeLeft(), TimeUnit.SECONDS).toString();
		}

		if(identifier.equalsIgnoreCase("location")) {

			int x = firstDeathChest.get().getLocation().getBlockX();
			int y = firstDeathChest.get().getLocation().getBlockY();
			int z = firstDeathChest.get().getLocation().getBlockZ();
			String world = firstDeathChest.get().getLocation().getWorld().getName();

			return plugin.getConfig().getString("placeholders_format.location")
					.replace("%x%",String.valueOf(x))
					.replace("%y%",String.valueOf(y))
					.replace("%z%",String.valueOf(z))
					.replace("%world%", world);
		}

		if (identifier.equalsIgnoreCase("unlock_after")) {
			long unlockTime = firstDeathChest.get().getUnlockTime();

			if (System.currentTimeMillis() >= unlockTime) {
				return "";
			} else {
				return new Time(unlockTime-System.currentTimeMillis(),TimeUnit.MILLISECONDS).toString();

			}
		}

		return null;
	}
}