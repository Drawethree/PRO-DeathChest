package sk.drawethree.deathchestpro.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.utils.CompMaterial;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.ArrayList;

public class DeathChestListener implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        for (Block b : e.blockList()) {
            if (b.getState() instanceof Chest) {
                Chest c = (Chest) b.getState();
                if (DeathChestManager.getInstance().getDeathChestByChest(c) != null) {
                    e.blockList().remove(b);
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {

        Inventory inv = e.getInventory();
        Player p = (Player) e.getPlayer();

        if (DeathChestManager.getInstance().getOpenedInventories().contains(p)) {
            DeathChestManager.getInstance().getOpenedInventories().remove(p);
        }

        Chest c = null;
        if (inv.getHolder() instanceof Chest) {
            c = (Chest) inv.getHolder();
        } else if (inv.getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) inv.getHolder();
            c = (Chest) doubleChest.getRightSide();
        }

        if (c != null) {
            DeathChest dc = DeathChestManager.getInstance().getDeathChestByChest(c);
            if (dc != null) {
                if (dc.areChestsEmpty()) {
                    dc.removeDeathChest();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!DeathChestPro.getInstance().getDisabledworlds().contains(p.getLocation().getWorld().getName()) && (p.hasPermission("deathchestpro.chest")) && (e.getDrops().size() > 0)) {
            DeathChestManager.getInstance().createDeathChest(p, e.getDrops());
            e.setKeepInventory(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        ArrayList<DeathChest> chests = DeathChestManager.getInstance().getPlayerDeathChests(p);
        if (chests != null) {
            p.getInventory().setArmorContents(null);
            p.getInventory().clear();
            for (DeathChest dc : chests) {
                if (!dc.isAnnounced()) {
                    dc.announce(p);
                    dc.runRemoveTask();
                }
            }
        }
    }

    @EventHandler
    public void onDeathChestBreak(BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Chest) {
            final Player p = e.getPlayer();
            final DeathChest dc = DeathChestManager.getInstance().getDeathChestByChest((Chest) e.getBlock().getState());
            if (dc != null) {
                if (!DeathChestPro.getInstance().isAllowBreakChests() || (dc.isLocked() && !dc.getPlayer().equals(p))) {
                    e.setCancelled(true);
                    p.sendMessage(Message.DEATHCHEST_CANNOT_BREAK.getChatMessage());
                } else {
                    e.setCancelled(true);
                    dc.removeDeathChest();
                }
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getInventory() == null || !e.getInventory().getTitle().contains(Message.DEATHCHEST_LIST_INV_TITLE.getMessage())) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != CompMaterial.AIR.getMaterial()) {
            int page = (int) e.getInventory().getTitle().charAt(e.getInventory().getTitle().length() - 1);
            DeathChest clickedChest = DeathChestManager.getInstance().getDeathChest(e.getCurrentItem());
            if (clickedChest != null) {
                clickedChest.teleportPlayer(p);
                e.setCancelled(true);
            } else if (e.getCurrentItem().isSimilar(Items.NEXT_ITEM.getItemStack())) {
                DeathChestManager.getInstance().openDeathchestList(p, page + 1);
                e.setCancelled(true);
            } else if (e.getCurrentItem().isSimilar(Items.PREV_ITEM.getItemStack())) {
                DeathChestManager.getInstance().openDeathchestList(p, page - 1);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTryingOpen(PlayerInteractEvent e) {
        if ((e.getClickedBlock() == null) || (!(e.getClickedBlock().getState() instanceof Chest))) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            DeathChest dc = DeathChestManager.getInstance().getDeathChestByChest((Chest) b.getState());
            if (dc != null && !dc.getPlayer().getName().equals(p.getName()) && ((!p.hasPermission("deathchestpro.see") || dc.isLocked()) && (!p.isOp()))) {
                e.setCancelled(true);
                p.sendMessage(Message.DEATHCHEST_CANNOT_OPEN.getChatMessage());
            }
        }
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent e) {
        if (e.getDestination().getType() == InventoryType.HOPPER && DeathChestManager.getInstance().isInventoryDeathChestInv(e.getSource())) {
            e.setCancelled(true);
        }
    }
}
