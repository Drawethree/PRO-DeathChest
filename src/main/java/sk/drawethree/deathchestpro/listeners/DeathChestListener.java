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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
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
import java.util.Iterator;

public class DeathChestListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(final EntityExplodeEvent e) {
        final Iterator it = e.blockList().iterator();
        while (it.hasNext()) {
            final Block b = (Block) it.next();
            if (b.getState() instanceof Chest) {
                final Chest c = (Chest) b.getState();
                if (DeathChestManager.getInstance().getDeathChestByLocation(c.getLocation()) != null) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(final InventoryCloseEvent e) {

        final Inventory inv = e.getInventory();
        final Player p = (Player) e.getPlayer();

        DeathChestManager.getInstance().removeFromOpenedInventories(p);

        final DeathChest dc = DeathChestManager.getInstance().getDeathChestByInventory(inv);
        if (dc != null) {
            if (dc.isChestEmpty()) {
                dc.removeDeathChest(false);
            } else {
                dc.updateHologram();
                p.playSound(p.getLocation(), CompSound.CHEST_CLOSE.getSound(), 0.5F, 1F);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Player " + p.getName() + " died.");
        if (!DeathChestPro.getDisabledworlds().contains(p.getLocation().getWorld().getName()) && (p.hasPermission("deathchestpro.chest")) && (e.getDrops().size() > 0)) {

            DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Player has permission to have chest, has some items in inventory and is not in restricted world");

            if (((e.getEntity().getLastDamageCause() != null) && (e.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) && (!DeathChestPro.isVoidSpawning())) || (p.getLocation().getBlock().getType() == CompMaterial.LAVA.getMaterial()) && (!DeathChestPro.isLavaSpawning())) {
                return;
            }

            if (DeathChestManager.getInstance().createDeathChest(p, p.getKiller(), new ArrayList<>(e.getDrops()))) {
                DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Chest created");

                e.getDrops().clear();
                e.setKeepInventory(true);
                p.getInventory().setArmorContents(null);
                p.getInventory().clear();
                p.updateInventory();

            }
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

        if (DeathChestPro.isStartTimerAtDeath()) {
            return;
        }

        final Player p = e.getPlayer();
        final ArrayList<DeathChest> chests = DeathChestManager.getInstance().getPlayerDeathChests(p);
        if (chests != null) {
            for (DeathChest dc : chests) {
                if (!dc.isAnnounced()) {
                    dc.announce();
                    dc.runRemoveTask();
                    dc.runUnlockTask();
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
                if (!DeathChestPro.isAllowBreakChests() || (dc.isLocked() && !dc.getOwner().getUniqueId().equals(p.getUniqueId()))) {
                    e.setCancelled(true);
                    p.sendMessage(Message.DEATHCHEST_CANNOT_BREAK.getChatMessage());
                } else {
                    e.setCancelled(true);
                    dc.removeDeathChest(true);
                }
            }
        }
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getInventory() == null || e.getView() == null || !e.getView().getTitle().contains(Message.DEATHCHEST_LIST_INV_TITLE.getMessage())) {
            return;
        }

        e.setCancelled(true);
        final Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != CompMaterial.AIR.getMaterial()) {
            int page = (int) e.getView().getTitle().charAt(e.getView().getTitle().length() - 1);
            final DeathChest clickedChest = DeathChestManager.getInstance().getDeathChest(e.getCurrentItem());
            if (clickedChest != null) {
                clickedChest.teleportPlayer(p);
            } else if (e.getCurrentItem().isSimilar(Items.NEXT_ITEM.getItemStack())) {
                DeathChestManager.getInstance().openDeathchestList(DeathChestManager.getInstance().getOpenedInventory(p), p, page + 1);
            } else if (e.getCurrentItem().isSimilar(Items.PREV_ITEM.getItemStack())) {
                DeathChestManager.getInstance().openDeathchestList(DeathChestManager.getInstance().getOpenedInventory(p), p, page - 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTryingOpen(final PlayerInteractEvent e) {

        if ((e.getClickedBlock() == null) || (!(e.getClickedBlock().getState() instanceof Chest))) {
            return;
        }

        final Player p = e.getPlayer();
        final Block b = e.getClickedBlock();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            DeathChest dc = DeathChestManager.getInstance().getDeathChestByLocation(b.getLocation());
            if (dc != null) {

                if (dc.isLocked()) {
                    if ((DeathChestPro.isAllowKillerLooting() && dc.getKiller() != null && dc.getKiller().getUniqueId().equals(p.getUniqueId())) || dc.getOwner().getUniqueId().equals(p.getUniqueId()) || p.hasPermission("deathchestpro.see") || dc.getKiller() != null && dc.getKiller().getUniqueId().equals(p.getUniqueId()) && dc.getOwner().getUniqueId().equals(p.getUniqueId())) {
                        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Player can open the chest.");
                    } else {
                        e.setCancelled(true);
                        p.sendMessage(Message.DEATHCHEST_CANNOT_OPEN.getChatMessage());
                        return;
                    }
                }

                e.setCancelled(true);

                if (p.isSneaking()) {
                    dc.fastLoot(p);
                    return;
                }

                p.playSound(p.getLocation(), CompSound.CHEST_OPEN.getSound(), 0.5F, 1F);
                p.openInventory(dc.getChestInventory());
            }
        }
    }

    @EventHandler
    public void onHopperMove(final InventoryMoveItemEvent e) {
        if (e.getDestination().getType() == InventoryType.HOPPER && DeathChestManager.getInstance().isInventoryDeathChestInv(e.getSource())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void manipulateHologram(PlayerArmorStandManipulateEvent e) {
        if (!e.getRightClicked().isVisible()) {
            e.setCancelled(true);
        }
    }
}
