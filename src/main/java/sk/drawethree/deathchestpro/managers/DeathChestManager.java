package sk.drawethree.deathchestpro.managers;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.utils.CompSound;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
                openedInventories.put(openTo,whoseChests);
            }
        }
    }

    private void refreshDeathChestInventory(Player p) {
        if (openedInventories.containsKey(p)) {
            int page = getPageNumber(p.getOpenInventory().getTopInventory());
            p.closeInventory();
            openDeathchestList(openedInventories.get(p),p, page);
        }
    }

    public int getPageNumber(Inventory inv) {
        return Integer.parseInt(inv.getTitle().replaceAll(Message.DEATHCHEST_LIST_INV_TITLE.getMessage(), "").replaceAll(" ", ""));
    }

    public void removeDeathChest(DeathChest dc) {
        ArrayList<DeathChest> list = deathChests.get(dc.getPlayer().getUniqueId());
        list.remove(dc);
        if (list.isEmpty()) {
            deathChests.remove(dc.getPlayer().getUniqueId());
        } else {
            deathChests.put(dc.getPlayer().getUniqueId(), list);
        }
        deathChestsByUUID.remove(dc.getChestUUID());
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

    private boolean canPlace(Player p) {
        //Residence Check
        if (DeathChestPro.isUseResidence()) {
            final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(p);
            if (res != null && !res.getPermissions().playerHas(p, Flags.build, true)) {
                return false;
            }
        }
        //WorldGuard Check
        if (DeathChestPro.isUseWorldGuard()) {
            try {
                for (ProtectedRegion region : WorldGuardPlugin.inst().getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation())) {
                    if (DeathChestPro.getDisabledRegions().contains(region.getId())) {
                        return false;
                    }
                }
            } catch (Throwable t) {
                DeathChestPro.warn("You are using not supported version of WorldGuard ! Please use any below version 7.0.0.");
                return true;
            }
        }
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
