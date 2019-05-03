package sk.drawethree.deathchestpro.chest;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.utils.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathChest {

    private UUID chestUUID;
    private OfflinePlayer player;
    private Hologram hologram;
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
        this.setupChest(p.getLocation(), items);
        this.setupHologram();
        this.listItem = createListItem();
        this.timeLeft = DeathChestPro.getRemoveChestAfter();
        this.announced = false;
    }

    public DeathChest(UUID chestUuid, OfflinePlayer p, Location loc, boolean locked, int timeLeft, List<ItemStack> items) {
        this.chestUUID = chestUuid;
        this.player = p;
        this.locked = locked;
        this.setupChest(loc,items);
        this.setupHologram();
        this.listItem = createListItem();
        this.timeLeft = timeLeft;
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
        if (DeathChestPro.isUseHolograms()) {

            Location hologramLoc = LocationUtil.getCenter(this.location.clone().add(0, 2.5, 0));

            this.hologram = HologramsAPI.createHologram(DeathChestPro.getInstance(), hologramLoc);

            if (DeathChestPro.isDisplayPlayerHead()) {
                hologram.appendItemLine(ItemUtil.getPlayerSkull(player, null, null));
            }

            for (String s : DeathChestPro.getHologramLines()) {
                hologram.appendTextLine(s
                        .replaceAll("%locked%", getLockedString())
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%death_date%", DeathChestPro.getDeathDateFormat().format(new Date()))
                        .replaceAll("%timeleft%", new Time(DeathChestPro.getRemoveChestAfter(), TimeUnit.SECONDS).toString()))
                ;
            }
            hologram.teleport(LocationUtil.getCenter(this.location.clone().add(0, 1 + hologram.getHeight(), 0)));
        }
    }

    private void setupChest(Location loc, List<ItemStack> items) {

        if (DeathChestPro.isSpawnChestOnHighestBlock() || loc.getY() <= 0) {
            loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
        }


        this.replacedBlock = loc.getBlock().getState();

        if(this.replacedBlock.getType() == CompMaterial.CHEST.getMaterial()) {
            this.replacedBlock.setType(Material.AIR);
        }

        //Build glass cage if lava protection is on
        if (loc.getBlock().getType() == CompMaterial.LAVA.getMaterial() && DeathChestPro.isLavaProtection()) {
            this.buildProtectionCage(loc);
        }

        loc.getBlock().setType(CompMaterial.CHEST.getMaterial());
        this.location = loc.getBlock().getLocation();
        this.location = loc.getBlock().getLocation();

        this.chestInventory = Bukkit.createInventory(null, items.size() > 27 ? 54 : 27, DeathChestPro.getDeathChestInvTitle().replaceAll("%player%", player.getName()));
        for (ItemStack i : items) {
            if(i == null) continue;
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

    public Hologram getHologram() {
        return hologram;
    }

    public boolean isLocked() {
        return locked;
    }

    public ItemStack getListItem() {
        return listItem;
    }

    public void runRemoveTask() {
        this.removeTask = new BukkitRunnable() {
            int nextFireworkIn = DeathChestPro.getFireworkInterval();
            int unlockChestAfter = DeathChestPro.getUnlockChestAfter();

            @Override
            public void run() {

                if(unlockChestAfter == 0) {
                    locked = false;
                }

                if (timeLeft == 0) {
                    removeDeathChest();
                    cancel();
                } else {
                    timeLeft--;

                    if (hologram != null) {
                        updateHologram(timeLeft);
                    }

                    if (DeathChestPro.isDeathchestFireworks() && hologram != null) {
                        nextFireworkIn--;
                        if (nextFireworkIn == 0) {
                            FireworkUtil.spawnRandomFirework(hologram.getLocation());
                            nextFireworkIn = DeathChestPro.getFireworkInterval();
                        }
                    }

                    if(DeathChestPro.getUnlockChestAfter() >= 0) {
                        unlockChestAfter--;
                    }

                }
            }
        }.runTaskTimer(DeathChestPro.getInstance(), 20L, 20L);

    }

    private void updateHologram(int timeLeft) {
        for (int i = 0; i < DeathChestPro.getHologramLines().size(); i++) {
            String line = DeathChestPro.getHologramLines().get(i);
            if (line.contains("%timeleft%")) {
                int lineNumber = i;
                if (DeathChestPro.isDisplayPlayerHead()) {
                    lineNumber += 1;
                }
                hologram.removeLine(lineNumber);
                hologram.insertTextLine(lineNumber, line.replaceAll("%timeleft%", new Time(timeLeft, TimeUnit.SECONDS).toString()));
            }
        }
    }

    public boolean isChestEmpty() {
        return DeathChestManager.isInventoryEmpty(this.chestInventory);
    }

    public void removeChests() {
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

        if (hologram != null) {
            hologram.delete();
        }

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

        if (!this.player.isOnline()) {
            return;
        }

        if (this.location.getBlock().getType() != CompMaterial.CHEST.getMaterial()) {
            this.location.getBlock().setType(CompMaterial.CHEST.getMaterial());
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
        player.getPlayer().sendMessage(Message.DEATHCHEST_WILL_DISAPPEAR.getChatMessage().replaceAll("%time%", String.valueOf(this.timeLeft)));
        this.announced = true;
    }

    private String getLockedString() {
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
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".player", this.player.getUniqueId().toString());
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".items", this.chestInventory.getContents());
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".locked", this.locked);
        DeathChestPro.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".timeleft", this.timeLeft);
        DeathChestPro.getFileManager().getConfig("deathchests.yml").save();
    }

    public Player getPlayer() {
        return player.getPlayer();
    }
}
