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
import sk.drawethree.deathchestpro.enums.DeathChestMenuItems;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;
import sk.drawethree.deathchestpro.utils.FireworkUtil;
import sk.drawethree.deathchestpro.utils.comp.CompMaterial;
import sk.drawethree.deathchestpro.utils.comp.CompSound;

import java.util.ArrayList;
import java.util.Iterator;

public class DeathChestListener implements Listener {

    private DeathChestPro plugin;

    public DeathChestListener(DeathChestPro plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(final EntityExplodeEvent e) {
        final Iterator it = e.blockList().iterator();
        while (it.hasNext()) {
            final Block b = (Block) it.next();
            if (b.getState() instanceof Chest) {
                final Chest c = (Chest) b.getState();
                if (this.plugin.getDeathChestManager().getDeathChestByLocation(c.getLocation()) != null) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(final InventoryCloseEvent e) {

        final Inventory inv = e.getInventory();
        final Player p = (Player) e.getPlayer();

        this.plugin.getDeathChestManager().removeFromOpenedInventories(p);

        final DeathChest dc = this.plugin.getDeathChestManager().getDeathChestByInventory(inv);
        if (dc != null) {
            if (dc.isChestEmpty()) {
                dc.restoreExp(p);
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
        this.plugin.debug(p, "Player " + p.getName() + " died.");
        this.plugin.debug(p, "Keep Inventory Flag:  " + e.getKeepInventory());

        //Check Keep Inventory Flag
        if (e.getKeepInventory()) {
            this.plugin.debug(p, "Chest will not be spawned, player has keep inventory flag set to true.");
            return;
        }

        //Check for PvP
        if (p.getKiller() != null && !this.plugin.getSettings().isSpawnOnPVP()) {
            this.plugin.debug(p, "Chest will not be spawned, player was killed in PvP and its disabled in config.");
            return;
        }

        //Check for maximum chests allowed at one time
        if (!p.isOp() && (this.plugin.getDeathChestManager().getAmountOfPlayerChests(p) >= this.plugin.getSettings().getMaxPlayerChests())) {
            this.plugin.debug(p, "Chest will not be spawned, player " + p.getName() + " has maximum allowed chests.");
            return;
        }

        this.plugin.debug(p, "Player " + p.getName() + " has less than maximum allowed chests.");

        //Check for restricted world, permission and drops size
        if (this.plugin.getSettings().getDisabledworlds().contains(p.getLocation().getWorld().getName()) || (!p.hasPermission("deathchestpro.chest")) || (e.getDrops().size() == 0)) {
            this.plugin.debug(p, "Chest will not be spawned, player " + p.getName() + " is in disabled world, does not have permission 'deathchestpro.chest' or has empty dropped items.");
            return;
        }

        this.plugin.debug(p, "Player " + p.getName() + " has permission to have chest, has some items in inventory and is not in restricted world");

        //Check for damage
        if (((e.getEntity().getLastDamageCause() != null) && (e.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) && (!this.plugin.getSettings().isVoidSpawning())) || (p.getLocation().getBlock().getType() == CompMaterial.LAVA.getMaterial()) && (!this.plugin.getSettings().isLavaSpawning())) {
            return;
        }

        if (this.plugin.getDeathChestManager().createDeathChest(p, p.getKiller(), new ArrayList<>(e.getDrops()))) {
            this.plugin.debug(p, "DeathChest created");

            if (this.plugin.getSettings().isStoreExperience()) {
                e.setDroppedExp(0);
                e.setKeepLevel(false);
            }

            e.getDrops().clear();
            e.setKeepInventory(true);
            p.getInventory().setArmorContents(null);
            p.getInventory().clear();
            p.updateInventory();

        }
    }


    @EventHandler
    public void onFireworkDamage(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Firework) {
            final Firework firework = (Firework) e.getDamager();
            if (firework.hasMetadata(FireworkUtil.FIREWORK_METADATA)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent e) {

        if (this.plugin.getSettings().isStartTimerAtDeath()) {
            return;
        }

        final Player p = e.getPlayer();
        final ArrayList<DeathChest> chests = this.plugin.getDeathChestManager().getPlayerDeathChests(p);
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
            final DeathChest dc = this.plugin.getDeathChestManager().getDeathChestByLocation(e.getBlock().getLocation());
            if (dc != null) {
                if (!this.plugin.getSettings().isAllowBreakChests() || (dc.isLocked() && !dc.getOwner().getUniqueId().equals(p.getUniqueId()))) {
                    e.setCancelled(true);
                    p.sendMessage(DeathChestMessage.DEATHCHEST_CANNOT_BREAK.getChatMessage());
                } else {
                    e.setCancelled(true);
                    dc.removeDeathChest(true);
                }
            }
        }
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getInventory() == null || e.getView() == null || !e.getView().getTitle().contains(DeathChestMessage.DEATHCHEST_LIST_INV_TITLE.getMessage())) {
            return;
        }

        e.setCancelled(true);
        final Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != CompMaterial.AIR.getMaterial()) {
            int page = (int) e.getView().getTitle().charAt(e.getView().getTitle().length() - 1);
            final DeathChest clickedChest = this.plugin.getDeathChestManager().getDeathChest(e.getCurrentItem());
            if (clickedChest != null) {
                if (e.getClick().isLeftClick()) {
                    clickedChest.teleportPlayer(p);
                } else if (e.getClick().isRightClick()) {
                    if (p.hasPermission("deathchestpro.see")) {
                        p.openInventory(clickedChest.getChestInventory());
                    } else {
                        p.sendMessage(DeathChestMessage.NO_PERMISSION.getChatMessage());
                    }
                }
            } else if (e.getCurrentItem().isSimilar(DeathChestMenuItems.NEXT_ITEM.getItemStack())) {
                this.plugin.getDeathChestManager().openDeathchestList(this.plugin.getDeathChestManager().getOpenedInventory(p), p, page + 1);
            } else if (e.getCurrentItem().isSimilar(DeathChestMenuItems.PREV_ITEM.getItemStack())) {
                this.plugin.getDeathChestManager().openDeathchestList(this.plugin.getDeathChestManager().getOpenedInventory(p), p, page - 1);
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
            DeathChest dc = this.plugin.getDeathChestManager().getDeathChestByLocation(b.getLocation());
            if (dc != null) {

                if (dc.isLocked()) {
                    if (p.hasPermission("deathchestpro.see.others") || dc.getOwner().getUniqueId().equals(p.getUniqueId()) || (this.plugin.getSettings().isAllowKillerLooting() && dc.getKiller() != null && dc.getKiller().getUniqueId().equals(p.getUniqueId())) || (dc.getKiller() != null && dc.getKiller().getUniqueId().equals(p.getUniqueId()) && dc.getOwner().getUniqueId().equals(p.getUniqueId()))) {
                        this.plugin.debug(p, "Player " + p.getName() + " can open the " + dc);
                    } else {
                        e.setCancelled(true);
                        p.sendMessage(DeathChestMessage.DEATHCHEST_CANNOT_OPEN.getChatMessage());
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
        if (e.getDestination().getType() == InventoryType.HOPPER && this.plugin.getDeathChestManager().isInventoryDeathChestInv(e.getSource())) {
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
