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
import sk.drawethree.deathchestpro.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathChest {

    private UUID chestUUID;
    private OfflinePlayer player;
    private DeathChestHologram hologram;
    private Location location;
    private BlockState replacedBlock;
    private Inventory chestInventory;
    private BukkitTask removeTask;
    private ItemStack listItem;
    private boolean announced;
    private boolean locked;
    private int timeLeft;

    public DeathChest(Player p, List<ItemStack> items) {
        this.chestUUID = UUID.randomUUID();
        this.player = p;
        this.locked = p.hasPermission("deathchestpro.lock");
        this.timeLeft = DeathChestPro.getRemoveChestAfter();
        this.setupChest(p.getLocation(), items);
        this.setupHologram();
        this.listItem = createListItem();
        this.announced = false;
    }

    public DeathChest(UUID chestUuid, OfflinePlayer p, Location loc, boolean locked, int timeLeft, List<ItemStack> items) {
        this.chestUUID = chestUuid;
        this.player = p;
        this.locked = locked;
        this.timeLeft = timeLeft;
        this.setupChest(loc, items);
        this.setupHologram();
        this.listItem = createListItem();
        this.announce();
        this.runRemoveTask();
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
        this.hologram = new DeathChestHologram(this, this.location.clone());

        /*if (DeathChestPro.isDisplayPlayerHead()) {
            hologram.appendItemLine(ItemUtil.getPlayerSkull(player, null, null));
        }*/

    }

    private void setupChest(Location loc, List<ItemStack> items) {

        if (DeathChestPro.isSpawnChestOnHighestBlock() || loc.getY() <= 0) {
            loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
        }

        if (loc.getY() > 255) {
            loc.setY(255);
        }

        if (loc.getBlock().getType() == CompMaterial.CHEST.getMaterial()) {
            loc.setY(loc.getY() + 1);
        }

        this.replacedBlock = loc.getBlock().getState();

        if (this.replacedBlock.getType() == CompMaterial.CHEST.getMaterial()) {
            this.replacedBlock.setType(Material.AIR);
        }

        //Build glass cage if lava protection is on
        if (loc.getBlock().getType() == CompMaterial.LAVA.getMaterial() && DeathChestPro.isLavaProtection()) {
            this.buildProtectionCage(loc);
        }

        loc.getBlock().setType(CompMaterial.CHEST.getMaterial());
        this.location = loc.getBlock().getLocation();

        this.chestInventory = Bukkit.createInventory(null, items.size() > 27 ? 54 : 27, DeathChestPro.getDeathChestInvTitle().replaceAll("%player%", player.getName()));
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

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    public boolean isLocked() {
        return locked;
    }

    public ItemStack getListItem() {
        return listItem;
    }

    public void runRemoveTask() {

        if (this.timeLeft == -1) {
            return;
        }

        this.removeTask = new BukkitRunnable() {
            int nextFireworkIn = DeathChestPro.getFireworkInterval();
            int unlockChestAfter = DeathChestPro.getUnlockChestAfter();

            @Override
            public void run() {

                if (unlockChestAfter == 0) {
                    locked = false;
                }

                if (timeLeft == 0) {
                    removeDeathChest();
                    cancel();
                } else {
                    timeLeft--;

                    hologram.updateHologram(timeLeft);

                    if (DeathChestPro.isDeathchestFireworks()) {
                        nextFireworkIn--;
                        if (nextFireworkIn == 0) {
                            FireworkUtil.spawnRandomFirework(hologram.getLocation());
                            nextFireworkIn = DeathChestPro.getFireworkInterval();
                        }
                    }

                    if (unlockChestAfter > 0) {
                        unlockChestAfter--;
                    }
                }
            }
        }.runTaskTimer(DeathChestPro.getInstance(), 20L, 20L);

    }

    public boolean isChestEmpty() {
        return DeathChestManager.isInventoryEmpty(this.chestInventory);
    }

    private void removeChests() {

        for (HumanEntity entity : new ArrayList<>(chestInventory.getViewers())) {
            entity.closeInventory();
        }

        if (DeathChestPro.isDropItemsAfterExpire()) {
            for (ItemStack item : chestInventory.getContents()) {
                if (item != null) {
                    location.getWorld().dropItemNaturally(location, item);
                }
            }
            location.getWorld().playSound(location, CompSound.ITEM_PICKUP.getSound(), 1F, 1F);
        }
        replacedBlock.update(true);
    }

    public void removeDeathChest() {

        if (removeTask != null) {
            removeTask.cancel();
        }

        this.hologram.delete();

        this.removeChests();

        if (this.player.isOnline()) {
            this.player.getPlayer().sendMessage(Message.DEATHCHEST_DISAPPEARED.getChatMessage());
        }

        DeathChestManager.getInstance().removeDeathChest(this);
    }

    public boolean isAnnounced() {
        return announced;
    }

    public void announce() {

        if (this.location.getBlock().getType() != CompMaterial.CHEST.getMaterial()) {
            this.location.getBlock().setType(CompMaterial.CHEST.getMaterial());
        }

        if (!this.getOfflinePlayer().isOnline()) {
            return;
        }

        if (DeathChestPro.isClickableMessage()) {
            BaseComponent[] msg = TextComponent.fromLegacyText(Message.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.location.getBlockX())).replaceAll("%yloc%", String.valueOf(this.location.getBlockY())).replaceAll("%zloc%", String.valueOf(this.location.getBlockZ())).replaceAll("%world%", this.location.getWorld().getName()));
            for (BaseComponent bc : msg) {
                bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.DEATHCHEST_LOCATED_HOVER.getMessage()).create()));
                bc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dc teleport " + chestUUID.toString()));
            }
            player.getPlayer().spigot().sendMessage(msg);
        } else {
            player.getPlayer().sendMessage(Message.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.location.getBlockX())).replaceAll("%yloc%", String.valueOf(this.location.getBlockY())).replaceAll("%zloc%", String.valueOf(this.location.getBlockZ())).replaceAll("%world%", this.location.getWorld().getName()));
        }
        player.getPlayer().sendMessage(Message.DEATHCHEST_WILL_DISAPPEAR.getChatMessage().replaceAll("%time%", timeLeft == -1 ? "∞" : String.valueOf(this.timeLeft)));
        this.announced = true;
    }

    public String getLockedString() {
        return locked ? Message.DEATHCHEST_LOCKED.getMessage() : Message.DEATHCHEST_UNLOCKED.getMessage();
    }

    public boolean teleportPlayer(Player p) {
        if (p.hasPermission("deathchestpro.teleport")) {
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

                if (DeathChestPro.isAutoEquipArmor() && (CompMaterial.isHelmet(i.getType()) || CompMaterial.isChestPlate(i.getType()) || CompMaterial.isLeggings(i.getType()) || CompMaterial.isBoots(i.getType()))) {
                    this.autoEquip(p, i);
                    chestInventory.remove(i);
                    continue;
                }

                chestInventory.remove(i);
                p.getInventory().addItem(i);
            }

            p.playSound(p.getLocation(), CompSound.ITEM_PICKUP.getSound(), 1F, 1F);

            if (isChestEmpty()) {
                removeDeathChest();
            } else {
                p.sendMessage(Message.DEATHCHEST_FASTLOOT_COMPLETE.getChatMessage().replaceAll("%amount%", String.valueOf(DeathChestManager.getAmountOfItems(chestInventory))));
            }

        } else {
            p.sendMessage(Message.NO_PERMISSION.getChatMessage());
        }
    }

    private void autoEquip(Player p, ItemStack i) {
        if (CompMaterial.isHelmet(i.getType())) {
            p.getInventory().setHelmet(i);
        } else if (CompMaterial.isChestPlate(i.getType())) {
            p.getInventory().setChestplate(i);
        } else if (CompMaterial.isLeggings(i.getType())) {
            p.getInventory().setLeggings(i);
        } else if (CompMaterial.isBoots(i.getType())) {
            p.getInventory().setBoots(i);
        }
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
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".location", this.location.serialize());
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".player", this.getOfflinePlayer().getUniqueId().toString());
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".items", this.chestInventory.getContents());
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".locked", this.locked);
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".timeleft", this.timeLeft);
        DeathChestPro.getFileManager().getConfig("deathchests.yml").save();
    }

    public Player getPlayer() {
        return player.getPlayer();
    }

    public void removeHologram() {
        this.hologram.delete();
    }

    public void removeChest() {
        this.location.getBlock().setType(CompMaterial.AIR.getMaterial());
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}
