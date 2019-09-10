package sk.drawethree.deathchestpro;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import sk.drawethree.deathchestpro.utils.Common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
public class DeathChestSettings {

	private final int CONFIG_VERSION = 1;
	private final int DEFAULT_EXPIRE_TIME;

	private final DeathChestPro plugin;

	private LinkedHashMap<String, Integer> expireGroups;
	private List<String> disabledworlds;
	private List<String> disabledRegions;
	private boolean allowBreakChests;
	private boolean deathchestFireworks;
	private boolean spawnChestOnHighestBlock;
	private boolean dropItemsAfterExpire;
	private boolean clickableMessage;
	private boolean lavaProtection;
	private boolean voidSpawning;
	private boolean autoEquipArmor;
	private boolean lavaSpawning;
	private boolean debugMode;
	private boolean hologramEnabled;
	private boolean startTimerAtDeath;
	private boolean storeExperience;
	private SimpleDateFormat deathDateFormat;
	private List<String> hologramLines;
	private String deathChestInvTitle;
	private int fireworkInterval;
	private int unlockChestAfter;
	private boolean allowKillerLooting;
	private double teleportCost;

	public DeathChestSettings(DeathChestPro plugin) {
		this.plugin = plugin;
		deathchestFireworks = true;
		allowBreakChests = false;
		disabledRegions = new ArrayList<>();
		disabledworlds = new ArrayList<>();
		expireGroups = new LinkedHashMap<>();
		DEFAULT_EXPIRE_TIME = 60;
		spawnChestOnHighestBlock = true;
		dropItemsAfterExpire = false;
		clickableMessage = false;
		lavaProtection = false;
		voidSpawning = false;
		autoEquipArmor = true;
		lavaSpawning = true;
		debugMode = true;
		hologramEnabled = true;
		storeExperience = false;
		startTimerAtDeath = false;
		deathDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");
		hologramLines = new ArrayList<>();
		deathChestInvTitle = "&7%player%'s DeathChest";
		fireworkInterval = 5;
		unlockChestAfter = -1;
		allowKillerLooting = false;
		teleportCost = 0;
	}

	public void loadSettings() {

		int configVersion = this.plugin.getFileManager().getConfig("config.yml").get().getInt("config_version");

		if (configVersion > this.CONFIG_VERSION) {
			this.plugin.broadcast(DeathChestPro.BroadcastType.WARN, "Config version %d is invalid ! Loading default config values.", configVersion);
			return;
		}

		if (configVersion == 1) {
			allowBreakChests = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("allow_break_chests");
			disabledworlds = this.plugin.getFileManager().getConfig("config.yml").get().getStringList("disabled_worlds");
			disabledRegions = this.plugin.getFileManager().getConfig("config.yml").get().getStringList("disabled_regions");
			deathchestFireworks = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("deathchest_fireworks.enabled");
			fireworkInterval = this.plugin.getFileManager().getConfig("config.yml").get().getInt("deathchest_fireworks.interval");
			hologramLines = Common.color(this.plugin.getFileManager().getConfig("config.yml").get().getStringList("hologram.lines"));
			spawnChestOnHighestBlock = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("spawn_chest_on_highest_block");
			dropItemsAfterExpire = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("drop_items_after_expire");
			clickableMessage = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("clickable_message");
			deathDateFormat = new SimpleDateFormat(this.plugin.getFileManager().getConfig("config.yml").get().getString("hologram.death_date_format"));
			deathChestInvTitle = ChatColor.translateAlternateColorCodes('&', this.plugin.getFileManager().getConfig("config.yml").get().getString("deathchest_inv_title"));
			lavaProtection = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("lava_protection");
			voidSpawning = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("void_spawning_chest");
			unlockChestAfter = this.plugin.getFileManager().getConfig("config.yml").get().getInt("unlock_chest_after");
			autoEquipArmor = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("auto_equip_armor");
			lavaSpawning = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("lava_spawning");
			debugMode = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("debug_messages");
			hologramEnabled = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("hologram.enabled");
			allowKillerLooting = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("allow_killer_looting");
			startTimerAtDeath = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("start_timer_at_death");
			teleportCost = this.plugin.getFileManager().getConfig("config.yml").get().getDouble("teleport_cost");
			storeExperience = this.plugin.getFileManager().getConfig("config.yml").get().getBoolean("store_experience");
			//saveXP = fileManager.getConfig("config.yml").get().getBoolean("save_xp");
			this.loadExpireGroups();
		}
	}

	private void loadExpireGroups() {
		expireGroups = new LinkedHashMap<>();
		for (String key : this.plugin.getFileManager().getConfig("config.yml").get().getConfigurationSection("expire_groups").getKeys(false)) {

			String permission = this.plugin.getFileManager().getConfig("config.yml").get().getString("expire_groups." + key + ".permission");
			int time = this.plugin.getFileManager().getConfig("config.yml").get().getInt("expire_groups." + key + ".time");

			this.plugin.broadcast(DeathChestPro.BroadcastType.DEBUG, String.format("Loaded group " + key + " with permission " + permission + " and expire time " + time + " seconds."));

			expireGroups.put(permission, time);
		}
	}

	public int getExpireTimeForPlayer(Player player) {
		for (String perm : this.expireGroups.keySet()) {
			if (player.hasPermission(perm)) {
				return this.expireGroups.get(perm);
			}
		}
		return this.DEFAULT_EXPIRE_TIME;
	}
}
	