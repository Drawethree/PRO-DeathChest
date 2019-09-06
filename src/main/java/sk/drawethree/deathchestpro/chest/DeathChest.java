package sk.drawethree.deathchestpro.chest;

import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.misc.DCHook;
import sk.drawethree.deathchestpro.misc.DCVaultHook;
import sk.drawethree.deathchestpro.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathChest {

    private DeathChestPro plugin;
    private UUID chestUUID;
    private OfflinePlayer player;
    private OfflinePlayer killer;
    private DeathChestHologram hologram;
    private Location location;
    private BlockState replacedBlock;
    private Inventory chestInventory;
    private BukkitTask removeTask;
    private ItemStack listItem;
    private boolean announced;
    private boolean locked;
    private int timeLeft;

    public DeathChest(DeathChestPro plugin, Player p, OfflinePlayer killer, List<ItemStack> items) {
        this.plugin = plugin;
        this.chestUUID = UUID.randomUUID();
        this.player = p;
        this.killer = killer;
        this.locked = p.hasPermission("deathchestpro.lock");
        this.timeLeft = this.plugin.getSettings().getExpireTimeForPlayer(p);

        this.setupChest(false, p.getLocation(), items);
        this.setupHologram();

        this.listItem = this.createListItem();
        this.announced = false;
    }

    public DeathChest(DeathChestPro plugin, UUID chestUuid, OfflinePlayer p, OfflinePlayer killer, Location loc, boolean locked, int timeLeft, List<ItemStack> items) {
        this.plugin = plugin;
        this.chestUUID = chestUuid;
        this.player = p;
        this.killer = killer;
        this.locked = locked;
        this.timeLeft = timeLeft;

        this.setupChest(true, loc, items);
        this.setupHologram();

        this.listItem = this.createListItem();

        this.announce();
        this.runRemoveTask();
        this.runUnlockTask();
    }

    private ItemStack createListItem() {
        final ItemStack returnItem = Items.DEATHCHEST_LIST_ITEM.getItemStack().clone();
        final ItemMeta meta = returnItem.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replaceAll("%player%", player.getName()));
        final List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i).replaceAll("%xloc%", String.valueOf(location.getBlockX())).
                    replaceAll("%yloc%", String.valueOf(location.getBlockY())).
                    replaceAll("%zloc%", String.valueOf(location.getBlockZ())).
                    replaceAll("%world%", location.getWorld().getName()).
                    replaceAll("%locked%", String.valueOf(locked).toUpperCase()).
                    replaceAll("%chesttype%", "§oPlease remove this placeholder from config !"));
        }
        meta.setLore(lore);
        returnItem.setItemMeta(meta);
        return returnItem;
    }

    private void setupHologram() {

        if (!this.plugin.getSettings().isHologramEnabled()) {
            return;
        }

        this.hologram = new DeathChestHologram(this);

        /*if (this.plugin.isDisplayPlayerHead()) {
            hologram.appendItemLine(ItemUtil.getPlayerSkull(player, null, null));
        }*/

    }

    private void setupChest(boolean fromConfig, Location loc, List<ItemStack> items) {

        if (!fromConfig) {
            if (this.plugin.getSettings().isSpawnChestOnHighestBlock() || loc.getY() <= 0) {
                loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
            }

            if (loc.getY() > 255) {
                loc.setY(255);
            }

            while (loc.getBlock().getType() == CompMaterial.CHEST.getMaterial()) {
                loc.setY(loc.getY() + 1);
            }
        }

        this.replacedBlock = loc.getBlock().getState();

        //Build glass cage if lava protection is on
        if (loc.getBlock().getType() == CompMaterial.LAVA.getMaterial() && this.plugin.getSettings().isLavaProtection()) {
            this.buildProtectionCage(loc);
        }

        loc.getBlock().setType(CompMaterial.CHEST.getMaterial());

        this.location = loc.getBlock().getLocation();

        this.chestInventory = Bukkit.createInventory(null, items.size() > 27 ? 54 : 27, this.plugin.getSettings().getDeathChestInvTitle().replaceAll("%player%", player.getName()));

        for (ItemStack i : items) {
            if (i == null) continue;
            this.chestInventory.addItem(i);
        }
    }

    private void buildProtectionCage(Location loc) {
        loc.clone().add(1, 0, 0).getBlock().setType(CompMaterial.GLASS.getMaterial());
        loc.clone().add(-1, 0, 0).getBlock().setType(CompMaterial.GLASS.getMaterial());
        loc.clone().add(0, 0, 1).getBlock().setType(CompMaterial.GLASS.getMaterial());
        loc.clone().add(0, 0, -1).getBlock().setType(CompMaterial.GLASS.getMaterial());
        loc.clone().add(0, 1, 0).getBlock().setType(CompMaterial.GLASS.getMaterial());
        loc.clone().add(0, -1, 0).getBlock().setType(CompMaterial.GLASS.getMaterial());
    }

    public OfflinePlayer getOwner() {
        return player;
    }

    public boolean isLocked() {
        return locked;
    }

    public ItemStack getListItem() {
        return listItem;
    }


    public void runUnlockTask() {

        if (this.plugin.getSettings().getUnlockChestAfter() <= 0) {
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {

                //Check if chest exists
                if (plugin.getDeathChestManager().getDeathChest(chestUUID.toString()) == null) {
                    return;
                }

                locked = false;
                if (hologram != null)
                    hologram.updateHologram(timeLeft);

            }
        }.runTaskLater(this.plugin.getInstance(), this.plugin.getSettings().getUnlockChestAfter() * 20L);
    }

    public void runRemoveTask() {

        //We do not want to run remove task when its never ending deathchest
        if (this.timeLeft == -1) {
            return;
        }

        this.removeTask = new BukkitRunnable() {
            int nextFireworkIn = plugin.getSettings().getFireworkInterval();

            @Override
            public void run() {

                if (timeLeft == 0) {
                    removeDeathChest(true);
                    cancel();
                } else {
                    timeLeft--;
                    if (hologram != null) {
                        hologram.updateHologram(timeLeft);
                    }

                    if (location.getBlock().getType() != Material.CHEST && !plugin.getSettings().isAllowBreakChests()) {
                        location.getBlock().setType(Material.CHEST);
                        location.getBlock().getState().update(true);
                    }

                    if (plugin.getSettings().isDeathchestFireworks()) {
                        nextFireworkIn--;
                        if (nextFireworkIn == 0) {
                            FireworkUtil.spawnRandomFirework(hologram.getLocation());
                            nextFireworkIn = plugin.getSettings().getFireworkInterval();
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin.getInstance(), 20L, 20L);

    }

    public boolean isChestEmpty() {
        return DeathChestManager.isInventoryEmpty(this.chestInventory);
    }

    private void removeChests(boolean closeInventories) {

        if (closeInventories) {
            for (HumanEntity entity : new ArrayList<>(chestInventory.getViewers())) {
                entity.closeInventory();
            }
        }

        if (this.plugin.getSettings().isDropItemsAfterExpire()) {
            for (ItemStack item : chestInventory.getContents()) {
                if (item != null) {
                    location.getWorld().dropItemNaturally(location, item);
                }
            }
            chestInventory.clear();
            location.getWorld().playSound(location, CompSound.ITEM_PICKUP.getSound(), 1F, 1F);
        }

        replacedBlock.update(true);
    }

    public void removeDeathChest(boolean closeInventories) {

        if (removeTask != null) {
            removeTask.cancel();
        }

        if (hologram != null) {
            this.hologram.despawn();
        }

        this.removeChests(closeInventories);

        if (this.player.isOnline()) {
            this.player.getPlayer().sendMessage(Message.DEATHCHEST_DISAPPEARED.getChatMessage());
        }

        this.plugin.getDeathChestManager().removeDeathChest(this);
    }

    public boolean isAnnounced() {
        return announced;
    }

    public void announce() {

        if (this.location.getBlock().getType() != CompMaterial.CHEST.getMaterial()) {
            this.location.getBlock().setType(CompMaterial.CHEST.getMaterial());
        }

        if (!this.getOwner().isOnline()) {
            return;
        }

        if (this.plugin.getSettings().isClickableMessage()) {
            BaseComponent[] msg = TextComponent.fromLegacyText(Message.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.location.getBlockX())).replaceAll("%yloc%", String.valueOf(this.location.getBlockY())).replaceAll("%zloc%", String.valueOf(this.location.getBlockZ())).replaceAll("%world%", this.location.getWorld().getName()));
            for (BaseComponent bc : msg) {
                bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.DEATHCHEST_LOCATED_HOVER.getMessage()).create()));
                bc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dc teleport " + chestUUID.toString()));
            }
            player.getPlayer().spigot().sendMessage(msg);
        } else {
            player.getPlayer().sendMessage(Message.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.location.getBlockX())).replaceAll("%yloc%", String.valueOf(this.location.getBlockY())).replaceAll("%zloc%", String.valueOf(this.location.getBlockZ())).replaceAll("%world%", this.location.getWorld().getName()));
        }

        if (timeLeft != -1) {
            player.getPlayer().sendMessage(Message.DEATHCHEST_WILL_DISAPPEAR.getChatMessage().replaceAll("%time%", timeLeft == -1 ? "∞" : String.valueOf(this.timeLeft)));
        }

        this.announced = true;
    }

    public String getLockedString() {
        return locked ? Message.DEATHCHEST_LOCKED.getMessage() : Message.DEATHCHEST_UNLOCKED.getMessage();
    }

    public boolean teleportPlayer(Player p) {
        if (p.hasPermission("deathchestpro.teleport")) {
            DCVaultHook vaultHook = (DCVaultHook) DCHook.getHookByName("Vault");

            if (vaultHook.getEconomy() != null) {
                if (!vaultHook.getEconomy().has(p, this.plugin.getSettings().getTeleportCost())) {
                    p.sendMessage(Message.DEATHCHEST_TELEPORT_NO_MONEY.getChatMessage());
                    return false;
                }

                vaultHook.getEconomy().withdrawPlayer(p, this.plugin.getSettings().getTeleportCost());
            }

            p.teleport(this.location.clone().add(0, 1, 0));
            p.sendMessage(Message.DEATHCHEST_TELEPORTED.getChatMessage());
            return true;
        } else {
            p.sendMessage(Message.NO_PERMISSION.getChatMessage());
            return false;
        }
    }

    public void fastLoot(Player p) {
        if (p.hasPermission("deathchestpro.fastloot")) {
            for (ItemStack i : chestInventory.getContents()) {
                if (i == null) continue;

                if (p.getInventory().firstEmpty() == -1) {
                    break;
                }

                if (this.plugin.getSettings().isAutoEquipArmor() && (CompMaterial.isHelmet(i.getType()) || CompMaterial.isChestPlate(i.getType()) || CompMaterial.isLeggings(i.getType()) || CompMaterial.isBoots(i.getType()))) {
                    if (!this.autoEquip(p, i)) {
                        p.getInventory().addItem(i);
                    }
                    chestInventory.remove(i);
                    continue;
                }

                chestInventory.remove(i);
                p.getInventory().addItem(i);
            }

            p.playSound(p.getLocation(), CompSound.ITEM_PICKUP.getSound(), 1F, 1F);

            if (isChestEmpty()) {
                removeDeathChest(true);
            } else {
                p.sendMessage(Message.DEATHCHEST_FASTLOOT_COMPLETE.getChatMessage().replaceAll("%amount%", String.valueOf(DeathChestManager.getAmountOfItems(chestInventory))));
            }

        } else {
            p.sendMessage(Message.NO_PERMISSION.getChatMessage());
        }
    }

    private boolean autoEquip(Player p, ItemStack i) {
        if (CompMaterial.isHelmet(i.getType()) && p.getInventory().getHelmet() == null) {
            p.getInventory().setHelmet(i);
            return true;
        } else if (CompMaterial.isChestPlate(i.getType()) && p.getInventory().getChestplate() == null) {
            p.getInventory().setChestplate(i);
            return true;
        } else if (CompMaterial.isLeggings(i.getType()) && p.getInventory().getLeggings() == null) {
            p.getInventory().setLeggings(i);
            return true;
        } else if (CompMaterial.isBoots(i.getType()) && p.getInventory().getBoots() == null) {
            p.getInventory().setBoots(i);
            return true;
        }
        return false;
    }

    public UUID getChestUUID() {
        return chestUUID;
    }

    public Inventory getChestInventory() {
        return chestInventory;
    }

    public Location getLocation() {
        return location;
    }

    public void save() {
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".location", this.location.serialize());
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".player", this.getOwner().getUniqueId().toString());
        if (this.killer != null) {
            this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".killer", this.getKiller().getUniqueId().toString());
        }
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".items", this.chestInventory.getContents());
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".locked", this.locked);
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".timeleft", this.timeLeft);
        this.plugin.getFileManager().getConfig("deathchests.yml").save();
    }

    public Player getPlayer() {
        return player.getPlayer();
    }

    public void removeHologram() {
        if (this.hologram != null) {
            this.hologram.despawn();
        }
    }

    public void removeChest() {
        this.location.getBlock().setType(CompMaterial.AIR.getMaterial());
        this.location.getBlock().getState().update(true);
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public int getItemCount() {
        int i = 0;
        for (ItemStack item : this.chestInventory.getContents()) {
            if (item != null) {
                i++;
            }
        }
        return i;
    }

    public void updateHologram() {
        if (this.hologram != null) {
            this.hologram.updateHologram(this.timeLeft);
        }
    }

    public OfflinePlayer getKiller() {
        return killer;
    }

    public DeathChestHologram getHologram() {
        return this.hologram;
    }

    public void spawnHologram() {
        if (this.hologram != null)
            this.hologram.spawn();
    }

    public void despawnHologram() {
        if (this.hologram != null)
            this.hologram.despawn();
    }

    public DeathChestPro getPlugin() {
        return this.plugin;
    }
}
