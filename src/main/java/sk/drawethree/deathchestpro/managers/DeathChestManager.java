package sk.drawethree.deathchestpro.managers;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.utils.CompSound;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.*;

public class DeathChestManager {

    private static DeathChestManager ourInstance = new DeathChestManager();
    private HashMap<UUID, ArrayList<DeathChest>> deathChests;
    private HashMap<UUID, DeathChest> deathChestsByUUID;
    private HashMap<Player, OfflinePlayer> openedInventories;

    private DeathChestManager() {
        deathChests = new HashMap<>();
        deathChestsByUUID = new HashMap<>();
        openedInventories = new HashMap<>();
    }


    public static DeathChestManager getInstance() {
        return ourInstance;
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
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Loading deathchests from file...");

        for (String key : DeathChestPro.getFileManager().getConfig("deathchests.yml").get().getConfigurationSection("chests").getKeys(false)) {

            UUID chestUuid = UUID.fromString(key);
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(DeathChestPro.getFileManager().getConfig("deathchests.yml").get().getString("chests." + key + ".player")));
            boolean locked = DeathChestPro.getFileManager().getConfig("deathchests.yml").get().getBoolean("chests." + key + ".locked");
            int timeLeft = DeathChestPro.getFileManager().getConfig("deathchests.yml").get().getInt("chests." + key + ".timeleft");
            Location loc = Location.deserialize(DeathChestPro.getFileManager().getConfig("deathchests.yml").get().getConfigurationSection("chests." + key + ".location").getValues(true));
            List<ItemStack> items = (ArrayList<ItemStack>) DeathChestPro.getFileManager().getConfig("deathchests.yml").get().get("chests." + key + ".items");
            createDeathChest(chestUuid, player, locked, loc, timeLeft, items);

        }
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Loaded!");

    }


    public void saveDeathChests() {
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Saving deathchests...");

        for (DeathChest dc : this.deathChestsByUUID.values()) {
            dc.removeHologram();
            dc.removeChest();
            dc.save();
        }

        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Saved!");
    }

    public ArrayList<DeathChest> getPlayerDeathChests(OfflinePlayer p) {
        return deathChests.get(p.getUniqueId());
    }

    public void openDeathchestList(OfflinePlayer whoseChests, Player openTo, int page) {
        int amountOfChests = deathChests.get(whoseChests.getUniqueId()) == null ? 0 : deathChests.get(whoseChests.getUniqueId()).size();
        if (page > 0) {
            if (amountOfChests >= (page * 45) - 45) {
                Inventory inv = Bukkit.createInventory(null, 54, Message.DEATHCHEST_LIST_INV_TITLE.getMessage() + page);
                List<DeathChest> deathChestsList = getPlayerDeathChests(whoseChests);
                int index = 0;

                for (int i = (page * 45) - 45; i < page * 45; i++) {
                    try {
                        inv.setItem(index, deathChestsList.get(i).getListItem());
                        index += 1;
                    } catch (Exception e) {
                        break;
                    }
                }
                inv.setItem(45, Items.PREV_ITEM.getItemStack());
                inv.setItem(53, Items.NEXT_ITEM.getItemStack());
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

    public int getPageNumber(InventoryView inv) {
        return Integer.parseInt(inv.getTitle().replaceAll(Message.DEATHCHEST_LIST_INV_TITLE.getMessage(), "").replaceAll(" ", ""));
    }

    public void removeDeathChest(DeathChest dc) {
        ArrayList<DeathChest> list = deathChests.get(dc.getOfflinePlayer().getUniqueId());
        list.remove(dc);
        if (list.isEmpty()) {
            deathChests.remove(dc.getOfflinePlayer().getUniqueId());
        } else {
            deathChests.put(dc.getOfflinePlayer().getUniqueId(), list);
        }
        deathChestsByUUID.remove(dc.getChestUUID());
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + dc.getChestUUID().toString(), null).save();
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
        for (ArrayList<DeathChest> list : deathChests.values()) {
            for (DeathChest dc : list) {
                if (dc.getLocation().equals(loc)) {
                    return dc;
                }
            }
        }
        return null;
    }

    public boolean isInventoryDeathChestInv(Inventory inv) {
        return getDeathChestByInventory(inv) != null;
    }

    public static boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null) return false;
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

    public boolean createDeathChest(Player p, List<ItemStack> drops) {

        if (!canPlace(p)) {
            return false;
        }

        if (deathChests.get(p.getUniqueId()) == null) {
            deathChests.put(p.getUniqueId(), new ArrayList<>());
        }

        ArrayList<DeathChest> currentChests = deathChests.get(p.getUniqueId());

        DeathChest dc = new DeathChest(p, drops);

        currentChests.add(dc);
        deathChests.put(p.getUniqueId(), currentChests);
        deathChestsByUUID.put(dc.getChestUUID(), dc);
        return true;

    }

    private boolean createDeathChest(UUID chestUuid, OfflinePlayer p, boolean locked, Location loc, int timeLeft, List<ItemStack> items) {

        if (deathChests.get(p.getUniqueId()) == null) {
            deathChests.put(p.getUniqueId(), new ArrayList<>());
        }

        ArrayList<DeathChest> currentChests = deathChests.get(p.getUniqueId());

        DeathChest dc = new DeathChest(chestUuid, p, loc, locked, timeLeft, items);

        currentChests.add(dc);
        deathChests.put(p.getUniqueId(), currentChests);
        deathChestsByUUID.put(dc.getChestUUID(), dc);

        return true;
    }

    private boolean canPlace(Player p) {
        //Residence Check
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Checking Residence...");
        if (DeathChestPro.isUseResidence()) {
            final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(p);
            if (res != null && !res.getPermissions().playerHas(p, Flags.build, true)) {
                DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Player does not have permission to build in residence=" + res.getName());
                return false;
            }
        }
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Residence OK");

        //WorldGuard Check
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Checking WorldGuard...");
        if (DeathChestPro.isUseWorldGuard()) {
            Set<IWrappedRegion> regions = null;

            try {
                regions = WorldGuardWrapper.getInstance().getRegions(p.getLocation());
            } catch (NoClassDefFoundError e) {
                DeathChestPro.broadcast(DeathChestPro.BroadcastType.WARN, "Looks like you are using bad WorldEdit version!");
            }

            if (regions != null) {
                for (IWrappedRegion region : regions) {
                    if (DeathChestPro.getDisabledRegions().contains(region.getId())) {
                        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Player is in restriced region=" + region.getId());
                        return false;
                    }
                }
            }
        }

        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "WorldGuard OK");
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Can place!");

        return true;
    }

    public DeathChest getDeathChest(String id) {
        return deathChestsByUUID.get(UUID.fromString(id));
    }

    public void removeFromOpenedInventories(Player p) {
        openedInventories.remove(p);
    }

    public OfflinePlayer getOpenedInventory(Player p) {
        return openedInventories.get(p);
    }
}
