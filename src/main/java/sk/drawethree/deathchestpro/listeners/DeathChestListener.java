package sk.drawethree.deathchestpro.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.utils.CompMaterial;
import sk.drawethree.deathchestpro.utils.CompSound;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.ArrayList;

public class DeathChestListener implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        for (Block b : e.blockList()) {
            if (b.getState() instanceof Chest) {
                Chest c = (Chest) b.getState();
                if (DeathChestManager.getInstance().getDeathChestByLocation(c.getLocation()) != null) {
                    e.blockList().remove(b);
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(final InventoryCloseEvent e) {

        final Inventory inv = e.getInventory();
        final Player p = (Player) e.getPlayer();

        if (DeathChestManager.getInstance().getOpenedInventories().contains(p)) {
            DeathChestManager.getInstance().getOpenedInventories().remove(p);
        }
        final DeathChest dc = DeathChestManager.getInstance().getDeathChestByInventory(inv);
        if (dc != null) {
            if (dc.isChestEmpty()) {
                dc.removeDeathChest();
            } else {
                p.playSound(p.getLocation(), CompSound.CHEST_CLOSE.getSound(), 1F, 1F);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        if (!DeathChestPro.getInstance().getDisabledworlds().contains(p.getLocation().getWorld().getName()) && (p.hasPermission("deathchestpro.chest")) && (e.getDrops().size() > 0)) {
            DeathChestManager.getInstance().createDeathChest(p, e.getDrops());
            e.setKeepInventory(true);
        }
    }

    @EventHandler
    public void onFireworkDamage(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Firework) {
            final Firework firework = (Firework) e.getDamager();
            if (firework.getMetadata("deathchestfw") != null) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        final ArrayList<DeathChest> chests = DeathChestManager.getInstance().getPlayerDeathChests(p);
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
    public void onDeathChestBreak(final BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Chest) {
            final Player p = e.getPlayer();
            final DeathChest dc = DeathChestManager.getInstance().getDeathChestByLocation(e.getBlock().getLocation());
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
    public void onInvClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getInventory() == null || !e.getInventory().getTitle().contains(Message.DEATHCHEST_LIST_INV_TITLE.getMessage())) {
            return;
        }

        final Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != CompMaterial.AIR.getMaterial()) {
            int page = (int) e.getInventory().getTitle().charAt(e.getInventory().getTitle().length() - 1);
            final DeathChest clickedChest = DeathChestManager.getInstance().getDeathChest(e.getCurrentItem());
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
    public void onTryingOpen(final PlayerInteractEvent e) {
        if ((e.getClickedBlock() == null) || (!(e.getClickedBlock().getState() instanceof Chest))) {
            return;
        }

        final Player p = e.getPlayer();
        final Block b = e.getClickedBlock();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            DeathChest dc = DeathChestManager.getInstance().getDeathChestByLocation(b.getLocation());
            if (dc != null) {
                if (!dc.getPlayer().getName().equals(p.getName()) && ((!p.hasPermission("deathchestpro.see") || dc.isLocked()) && (!p.isOp()))) {
                    e.setCancelled(true);
                    p.sendMessage(Message.DEATHCHEST_CANNOT_OPEN.getChatMessage());
                } else {
                    e.setCancelled(true);
                    p.playSound(p.getLocation(), CompSound.CHEST_OPEN.getSound(), 1F, 1F);
                    p.openInventory(dc.getChestInventory());
                }
            }
        }
    }

    @EventHandler
    public void onHopperMove(final InventoryMoveItemEvent e) {
        if (e.getDestination().getType() == InventoryType.HOPPER && DeathChestManager.getInstance().isInventoryDeathChestInv(e.getSource())) {
            e.setCancelled(true);
        }
    }
}
