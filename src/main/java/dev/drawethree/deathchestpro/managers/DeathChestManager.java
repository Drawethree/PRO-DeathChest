package dev.drawethree.deathchestpro.managers;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import dev.drawethree.deathchestpro.DeathChestPro;
import dev.drawethree.deathchestpro.chest.DeathChest;
import dev.drawethree.deathchestpro.chest.DeathChestHologram;
import dev.drawethree.deathchestpro.enums.DeathChestCreateResult;
import dev.drawethree.deathchestpro.enums.DeathChestMenuItems;
import dev.drawethree.deathchestpro.enums.DeathChestMessage;
import dev.drawethree.deathchestpro.misc.hook.DCHook;
import dev.drawethree.deathchestpro.utils.ExperienceUtil;
import dev.drawethree.deathchestpro.utils.comp.CompSound;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;

public class DeathChestManager {

	private DeathChestPro plugin;
	private HashMap<UUID, ArrayList<DeathChest>> deathChests;
	@Getter
	private HashMap<UUID, DeathChest> deathChestsByUUID;
	private HashMap<Player, OfflinePlayer> openedInventories;

	public DeathChestManager(DeathChestPro plugin) {
		this.plugin = plugin;
		deathChests = new HashMap<>();
		deathChestsByUUID = new HashMap<>();
		openedInventories = new HashMap<>();
	}

	public DeathChest getDeathChest(ItemStack item) {
		for (ArrayList<DeathChest> list : deathChests.values()) {
			for (DeathChest dc : list) {
				if (dc.getListItem().equals(item)) {
					return dc;
				}
			}
		}
		return null;
	}

	public void loadDeathChests() {
		this.plugin.debug(null, "Loading deathchests from file database...");

		for (String key : this.plugin.getFileManager().getConfig("deathchests.yml").get().getConfigurationSection("chests").getKeys(false)) {

			UUID chestUuid = UUID.fromString(key);
			Location loc;

			try {
				loc = Location.deserialize(this.plugin.getFileManager().getConfig("deathchests.yml").get().getConfigurationSection("chests." + key + ".location").getValues(true));
			} catch (Exception e) {
				this.plugin.broadcast(DeathChestPro.BroadcastType.WARN, "DeathChest with UUID " + chestUuid.toString() + " is in unknown location! Perhaps its world is not loaded?");
				continue;
			}

			OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(this.plugin.getFileManager().getConfig("deathchests.yml").get().getString("chests." + key + ".player"))));
			OfflinePlayer killer = null;

			try {
				killer = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(this.plugin.getFileManager().getConfig("deathchests.yml").get().getString("chests." + key + ".killer"))));
			} catch (Exception e) {
				//Without killer.
			}

			boolean locked = this.plugin.getFileManager().getConfig("deathchests.yml").get().getBoolean("chests." + key + ".locked");
			int timeLeft = this.plugin.getFileManager().getConfig("deathchests.yml").get().getInt("chests." + key + ".timeleft");
			long diedAt = this.plugin.getFileManager().getConfig("deathchests.yml").get().getLong("chests." + key + ".died");

			Material chestBlock;
			try {
				chestBlock = Material.valueOf(this.plugin.getFileManager().getConfig("deathchests.yml").get().getString("chests." + key + ".chest_block"));
			} catch (Exception e) {
				chestBlock = Material.CHEST;
			}

			if (diedAt == 0) {
				diedAt = new Date().getTime();
			}

			int playerExp = this.plugin.getFileManager().getConfig("deathchests.yml").get().getInt("chests." + key + ".exp");
			ArrayList<ItemStack> items = (ArrayList<ItemStack>) this.plugin.getFileManager().getConfig("deathchests.yml").get().get("chests." + key + ".items");
			createDeathChest(chestUuid, player, killer, locked, loc, timeLeft, diedAt, items, playerExp, chestBlock);
			this.plugin.debug(null, "Loaded DeathChest at location " + loc.toString() + "!");
		}
		this.plugin.debug(null, "Loaded!");

	}


	public void saveDeathChests() {
		this.plugin.debug(null, "Saving deathchests...");

		for (DeathChest dc : this.deathChestsByUUID.values()) {
			dc.removeChest();
			dc.removeHologram();
			dc.save();
		}

		this.plugin.debug(null, "Saved!");
	}

	public ArrayList<DeathChest> getPlayerDeathChests(OfflinePlayer p) {
		return deathChests.getOrDefault(p.getUniqueId(), new ArrayList<>());
	}

	public void openDeathchestList(OfflinePlayer whoseChests, Player openTo, int page) {
		int amountOfChests = deathChests.get(whoseChests.getUniqueId()) == null ? 0 : deathChests.get(whoseChests.getUniqueId()).size();
		if (page > 0) {
			if (amountOfChests >= (page * 45) - 45) {
				Inventory inv = Bukkit.createInventory(null, 54, DeathChestMessage.DEATHCHEST_LIST_INV_TITLE.getMessage() + page);
				List<DeathChest> deathChestsList = this.getPlayerDeathChests(whoseChests);
				int index = 0;

				for (int i = (page * 45) - 45; i < page * 45; i++) {
					try {
						inv.setItem(index, deathChestsList.get(i).getListItem());
						index += 1;
					} catch (Exception e) {
						break;
					}
				}
				inv.setItem(45, DeathChestMenuItems.PREV_ITEM.getItemStack());
				inv.setItem(53, DeathChestMenuItems.NEXT_ITEM.getItemStack());
				openTo.playSound(openTo.getLocation(), CompSound.ORB_PICKUP.getSound(), 1, 1);
				openTo.openInventory(inv);
				openedInventories.put(openTo, whoseChests);
			}
		}
	}

	private void refreshDeathChestInventory(Player p) {
		if (openedInventories.containsKey(p)) {
			int page = getPageNumber(p.getOpenInventory());
			p.closeInventory();
			openDeathchestList(openedInventories.get(p), p, page);
		}
	}

	private int getPageNumber(InventoryView inv) {
		return Integer.parseInt(inv.getTitle().replaceAll(DeathChestMessage.DEATHCHEST_LIST_INV_TITLE.getMessage(), "").replaceAll(" ", ""));
	}

	public void removeDeathChest(DeathChest dc) {

		if (dc == null) {
			return;
		}

		ArrayList<DeathChest> list = deathChests.get(dc.getOwner().getUniqueId());

		if (list == null) {
			list = new ArrayList<>();
		}

		list.remove(dc);

		if (list.isEmpty()) {
			deathChests.remove(dc.getOwner().getUniqueId());
		} else {
			deathChests.put(dc.getOwner().getUniqueId(), list);
		}
		deathChestsByUUID.remove(dc.getChestUUID());
		this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + dc.getChestUUID().toString(), null).save();
		refreshDeathChestInventory(dc.getPlayer());
	}

	public DeathChest getDeathChestByInventory(Inventory inv) {
		for (ArrayList<DeathChest> list : deathChests.values()) {
			for (DeathChest dc : list) {
				if (dc.getChestInventory().equals(inv)) {
					return dc;
				}
			}
		}
		return null;
	}

	public DeathChest getDeathChestByLocation(Location loc) {
		for (DeathChest dc : deathChestsByUUID.values()) {
			if (dc.getLocation().equals(loc)) {
				return dc;
			}
		}
		return null;
	}

	public boolean isInventoryDeathChestInv(Inventory inv) {
		return getDeathChestByInventory(inv) != null;
	}

	public static boolean isInventoryEmpty(Inventory inv) {
		for (ItemStack item : inv.getContents()) {
			if (item != null) {
				return false;
			}
		}
		return true;
	}

	public static int getAmountOfItems(Inventory inv) {
		int amount = 0;
		for (ItemStack item : inv.getContents()) {
			if (item != null) {
				amount += 1;
			}
		}
		return amount;
	}

	public DeathChestCreateResult createDeathChest(Player p, Player killer, List<ItemStack> drops) {

		Location safeLoc = p.getLocation();

		int totalIterations = 0;

		while (!isSafeMaterial(safeLoc.getBlock().getType())) {

			if (totalIterations > 255) {
				//Avoid infinite looping in END / NETHER Environment
				return DeathChestCreateResult.FAILED_NO_SAFE_LOCATION_FOUND;
			}

			safeLoc = safeLoc.clone().add(1, 0, 0);
			totalIterations++;
		}


		if (!canPlace(safeLoc, p)) {
			return DeathChestCreateResult.FAILED_CANT_PLACE_AT_LOCATION;
		}

		deathChests.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>());

		ArrayList<DeathChest> currentChests = deathChests.get(p.getUniqueId());

		int exp = 0;

		if (this.plugin.getSettings().isStoreExperience()) {
			exp = ExperienceUtil.getExp(p);
		}

		DeathChest dc = new DeathChest(this.plugin, p, safeLoc, killer, drops, exp);

		currentChests.add(dc);
		deathChests.put(p.getUniqueId(), currentChests);
		deathChestsByUUID.put(dc.getChestUUID(), dc);

		if (this.plugin.getSettings().isStartTimerAtDeath()) {
			dc.announce();
			dc.runUnlockTask();
			dc.runRemoveTask();
		}

		return DeathChestCreateResult.SUCCESS;

	}


	private boolean isSafeMaterial(Material type) {
		for (String s : this.plugin.getSettings().getDisabledMaterials()) {
			if (type.name().toLowerCase().contains(s.toLowerCase())) {
				return false;
			}
		}
		return true;
	}

	private void createDeathChest(UUID chestUuid, OfflinePlayer p, OfflinePlayer killer, boolean locked, Location loc, int timeLeft, long diedAt, List<ItemStack> items, int playerExp, Material chestBlock) {

		deathChests.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>());

		ArrayList<DeathChest> currentChests = deathChests.get(p.getUniqueId());

		DeathChest dc = new DeathChest(this.plugin, chestUuid, p, killer, loc, locked, timeLeft, diedAt, items, playerExp, chestBlock);
		currentChests.add(dc);

		deathChests.put(p.getUniqueId(), currentChests);
		deathChestsByUUID.put(dc.getChestUUID(), dc);

    }

	private boolean canPlace(Location loc, Player player) {
		//Residence Check
		this.plugin.debug(player, "Checking Residence...");
		if (DCHook.getHook("Residence")) {
			final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
			if (res != null && !res.getPermissions().playerHas(player, Flags.build, true)) {
				this.plugin.debug(player, "Player does not have permission to build in residence=" + res.getName());
				return false;
			}
		}

		this.plugin.debug(player, "Residence OK");

		//WorldGuard Check
		this.plugin.debug(player, "Checking WorldGuard...");
		if (DCHook.getHook("WorldGuard")) {
			Set<IWrappedRegion> regions = null;

			try {
				regions = WorldGuardWrapper.getInstance().getRegions(loc);
			} catch (NoClassDefFoundError e) {
				this.plugin.broadcast(DeathChestPro.BroadcastType.WARN, "Looks like you are using bad WorldEdit version!");
			}

			if (regions != null) {
				for (IWrappedRegion region : regions) {
					if (this.plugin.getSettings().getDisabledRegions().contains(region.getId())) {
						this.plugin.debug(player, "Player is in restriced region=" + region.getId());
						return false;
					}
				}
			}
		}

		this.plugin.debug(player, "WorldGuard OK");
		this.plugin.debug(player, "Can place!");

		return true;
	}

	public DeathChest getDeathChest(String id) {
		UUID uuid;

		try {
			uuid = UUID.fromString(id);
		} catch (Exception e) {
			//Invalid uuid
			return null;
		}

		return this.deathChestsByUUID.get(uuid);
	}

	public void removeFromOpenedInventories(Player p) {
		openedInventories.remove(p);
	}

	public OfflinePlayer getOpenedInventory(Player p) {
		return openedInventories.get(p);
	}

	public void removeExistingHolograms() {
		for (World w : Bukkit.getWorlds()) {
			for (Entity e : w.getEntities()) {
				if (!(e instanceof ArmorStand)) {
					continue;
				}
				if (e.hasMetadata(DeathChestHologram.ENTITY_METADATA)) {
					this.plugin.debug(null, "Removing hologram entity.");
					e.remove();
				}
			}
		}
	}

	public int getAmountOfPlayerChests(Player p) {
		if (!deathChests.containsKey(p.getUniqueId())) {
			return 0;
		}
		return deathChests.get(p.getUniqueId()).size();
	}
}
