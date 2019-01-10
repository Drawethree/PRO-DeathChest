package sk.drawethree.deathchestpro.managers;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
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
    private List<Player> openedInventories;

    private DeathChestManager() {
        deathChests = new HashMap<>();
        openedInventories = new ArrayList<>();
    }


    public static DeathChestManager getInstance() {
        return ourInstance;
    }

    public List<Player> getOpenedInventories() {
        return openedInventories;
    }

    public HashMap<UUID, ArrayList<DeathChest>> getDeathChests() {
        return deathChests;
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

    public ArrayList<DeathChest> getPlayerDeathChests(Player p) {
        return deathChests.get(p.getUniqueId());
    }

    public void openDeathchestList(Player p, int page) {
        int amountOfChests = deathChests.get(p.getUniqueId()) == null ? 0 : deathChests.get(p.getUniqueId()).size();
        if (page > 0) {
            if (amountOfChests >= (page * 45) - 45) {
                Inventory inv = Bukkit.createInventory(null, 54, Message.DEATHCHEST_LIST_INV_TITLE.getMessage() + page);
                List<DeathChest> deathChestsList = getPlayerDeathChests(p);
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
                p.playSound(p.getLocation(), CompSound.ORB_PICKUP.getSound(), 1, 1);
                p.openInventory(inv);
                openedInventories.add(p);
            }
        }
    }

    private void refreshDeathChestInventory(Player p) {
        if (openedInventories.contains(p)) {
            int page = getPageNumber(p.getOpenInventory().getTopInventory());
            p.closeInventory();
            openDeathchestList(p, page);
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
        refreshDeathChestInventory(dc.getPlayer());
    }

    public DeathChest getDeathChestByInventory(Inventory inv) {
        for (ArrayList<DeathChest> list : deathChests.values()) {
            for (DeathChest dc : list) {
                for (Chest c : dc.getChests()) {
                    if (c.getBlockInventory().equals(inv)) {
                        return dc;
                    }
                }
            }
        }
        return null;
    }

    public DeathChest getDeathChestByChest(Chest c) {
        for (ArrayList<DeathChest> list : deathChests.values()) {
            for (DeathChest dc : list) {
                if (dc.getChests().contains(c)) {
                    return dc;
                }
            }
        }
        return null;
    }

    public boolean isInventoryDeathChestInv(Inventory inv) {
        for (ArrayList<DeathChest> list : deathChests.values()) {
            for (DeathChest dc : list) {
                for (Chest c : dc.getChests()) {
                    if (c.getInventory().equals(inv)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null) return false;
        }
        return true;
    }

    public void createDeathChest(Player p, List<ItemStack> drops) {
        if (!canPlace(p)) {
            return;
        }

        /*if(DeathChestPro.getInstance().isUseDeathFeathers()) {

        }*/
        if (deathChests.get(p.getUniqueId()) == null) {
            deathChests.put(p.getUniqueId(), new ArrayList<>());
        }
        ArrayList<DeathChest> currentChests = deathChests.get(p.getUniqueId());
        currentChests.add(new DeathChest(p, drops));
        deathChests.put(p.getUniqueId(), currentChests);
    }

    private boolean canPlace(Player p) {
        //Residence Check
        if (DeathChestPro.getInstance().isUseResidence()) {
            final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(p);
            if (res != null && !res.getPermissions().playerHas(p, Flags.build, true)) {
                return false;
            }
        }

        return true;
    }
}
