package sk.drawethree.deathchestpro.chest;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.tasks.ChestRemoveTask;
import sk.drawethree.deathchestpro.chest.tasks.ChestUnlockTask;
import sk.drawethree.deathchestpro.chest.tasks.HologramUpdateTask;
import sk.drawethree.deathchestpro.enums.DeathChestMenuItems;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.misc.hook.DCHook;
import sk.drawethree.deathchestpro.misc.hook.hooks.DCVaultHook;
import sk.drawethree.deathchestpro.utils.LocationUtil;
import sk.drawethree.deathchestpro.utils.comp.CompMaterial;
import sk.drawethree.deathchestpro.utils.comp.CompSound;
import sk.drawethree.deathchestpro.utils.cooldown.Cooldown;
import sk.drawethree.deathchestpro.utils.cooldown.CooldownMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathChest {

    private static final CooldownMap<Player> teleportCooldown = CooldownMap.create(Cooldown.of(DeathChestPro.getInstance().getSettings().getTeleportCooldown(), TimeUnit.SECONDS));

    private DeathChestPro plugin;

    private UUID chestUUID;
    private OfflinePlayer player;
    private OfflinePlayer killer;
    private DeathChestHologram hologram;
    private Location location;
    private BlockState replacedBlock;
    private Inventory chestInventory;

    private BukkitTask hologramUpdateTask;
    private BukkitTask chestRemoveTask;
    private BukkitTask unlockTask;

    private ItemStack listItem;
    private boolean announced;
    private boolean locked;

    @Setter
    private int timeLeft;
    @Getter
    private int playerExp;
    private Date deathDate;
    @Getter
    private boolean unloaded;
    @Getter
    private long unlockTime;

    public DeathChest(DeathChestPro plugin, Player p, Location locationToSpawn, OfflinePlayer killer, List<ItemStack> items, int playerExp) {
        this.plugin = plugin;
        this.chestUUID = UUID.randomUUID();
        this.player = p;
        this.killer = killer;
        this.locked = p.hasPermission("deathchestpro.lock");
        this.timeLeft = this.plugin.getSettings().getExpireGroup(p).getExpireTime();
        this.playerExp = playerExp;
        this.deathDate = new Date();

        this.setupChest(false, locationToSpawn, items);

        this.listItem = this.createListItem();
        this.announced = false;
    }

    public DeathChest(DeathChestPro plugin, UUID chestUuid, OfflinePlayer p, OfflinePlayer killer, Location loc, boolean locked, int timeLeft, long diedAt, List<ItemStack> items, int playerExp) {
        this.plugin = plugin;
        this.chestUUID = chestUuid;
        this.player = p;
        this.killer = killer;
        this.locked = locked;
        this.timeLeft = timeLeft;
        this.playerExp = playerExp;
        this.deathDate = new Date(diedAt);

        this.setupChest(true, loc, items);

        this.listItem = this.createListItem();

        this.announce();
        this.runRemoveTask();
        this.runUnlockTask();
    }

    private ItemStack createListItem() {
        final ItemStack returnItem = DeathChestMenuItems.DEATHCHEST_LIST_ITEM.getItemStack().clone();
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
        this.hologramUpdateTask = new HologramUpdateTask(this).runTaskTimer(this.plugin, 20L, 20L);

        /*if (this.plugin.isDisplayPlayerHead()) {
            hologram.appendItemLine(ItemUtil.getPlayerSkull(player, null, null));
        }*/

    }

    private void setupChest(boolean fromConfig, Location loc, List<ItemStack> items) {


        if (items == null) {
            items = new ArrayList<>();
        }

        if (!fromConfig) {


            if (loc.getWorld().getEnvironment() != World.Environment.NETHER && (this.plugin.getSettings().isSpawnChestOnHighestBlock() || loc.getY() <= 0)) {
                loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
            }

            if (loc.getY() <= 0) {
                loc.setY(1);
            }

            if (loc.getY() > 255) {
                loc.setY(255);
            }

            while (loc.getBlock().getType() == CompMaterial.CHEST.getMaterial()) {
                loc.setY(loc.getY() + 1);
            }

            if (this.plugin.getSettings().isLavaProtection() && !this.plugin.getSettings().isOldLavaProtection()) {
                while (loc.getBlock().getType().name().contains("LAVA")) {
                    loc.setY(loc.getY()+1);
                }
            }
        }

        this.replacedBlock = loc.getBlock().getState();

        if (this.plugin.getSettings().isOldLavaProtection()) {
            this.buildProtectionCage(loc);
        }

        loc.getBlock().setType(CompMaterial.CHEST.getMaterial());

        this.location = loc.getBlock().getLocation();

        this.chestInventory = Bukkit.createInventory(null, items.size() > 27 ? 54 : 27, this.plugin.getSettings().getDeathChestInvTitle().replace("%player%", player.getName()));

        for (ItemStack i : items) {
            if (i == null || i.getItemMeta().hasEnchant(Enchantment.getByName("VANISHING_CURSE"))) continue;
            this.chestInventory.addItem(i);
        }

        this.setupHologram();
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

        int unlockAfter = this.plugin.getSettings().getExpireGroup(this.getPlayer()).getUnlockAfter();

        if (unlockAfter == -1) {
            return;
        }

        this.unlockTask = new ChestUnlockTask(this).runTaskLater(this.plugin, unlockAfter * 20);
        this.unlockTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(unlockAfter);

    }

    public void runRemoveTask() {

        //We do not want to run remove task when its never ending deathchest
        if (this.timeLeft == -1) {
            return;
        }

        this.chestRemoveTask = new ChestRemoveTask(this).runTaskTimer(this.plugin, 20L, 20L);

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

        new BukkitRunnable() {

            @Override
            public void run() {
                replacedBlock.update(true);
            }
        }.runTaskLater(this.getPlugin(), 20L);


        if (this.playerExp != 0) {
            ExperienceOrb experienceOrb = location.getWorld().spawn(location, ExperienceOrb.class);
            experienceOrb.setExperience(this.playerExp);
        }
    }

    public void removeDeathChest(boolean closeInventories) {

        this.stopAllTasks();

        if (hologram != null) {
            this.hologram.despawn();
        }

        this.removeChests(closeInventories);

        if (this.player.isOnline()) {
            this.player.getPlayer().sendMessage(DeathChestMessage.DEATHCHEST_DISAPPEARED.getChatMessage());
        }

        this.plugin.getDeathChestManager().removeDeathChest(this);
    }

    private void stopAllTasks() {
        if (this.hologramUpdateTask != null)
            this.hologramUpdateTask.cancel();
        if (this.chestRemoveTask != null)
            this.chestRemoveTask.cancel();
        if (this.unlockTask != null)
            this.unlockTask.cancel();
    }

    public boolean isAnnounced() {
        return announced;
    }

    public void announce() {

        if (!this.getOwner().isOnline()) {
            return;
        }

        if (this.plugin.getSettings().isClickableMessage()) {

            BaseComponent[] msg = TextComponent.fromLegacyText(DeathChestMessage.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.location.getBlockX())).replaceAll("%yloc%", String.valueOf(this.location.getBlockY())).replaceAll("%zloc%", String.valueOf(this.location.getBlockZ())).replaceAll("%world%", this.location.getWorld().getName()));
            for (BaseComponent bc : msg) {
                bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DeathChestMessage.DEATHCHEST_LOCATED_HOVER.getMessage()).create()));
                bc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dc teleport " + chestUUID.toString()));
            }
            player.getPlayer().spigot().sendMessage(msg);
        } else {
            player.getPlayer().sendMessage(DeathChestMessage.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.location.getBlockX())).replaceAll("%yloc%", String.valueOf(this.location.getBlockY())).replaceAll("%zloc%", String.valueOf(this.location.getBlockZ())).replaceAll("%world%", this.location.getWorld().getName()));
        }

        if (timeLeft != -1) {
            player.getPlayer().sendMessage(DeathChestMessage.DEATHCHEST_WILL_DISAPPEAR.getChatMessage().replaceAll("%time%", timeLeft == -1 ? "∞" : String.valueOf(this.timeLeft)));
        }

        this.announced = true;
        teleportCooldown.reset(getOwner().getPlayer());
        teleportCooldown.test(getOwner().getPlayer());
    }

    public String getLockedString() {
        return locked ? DeathChestMessage.DEATHCHEST_LOCKED.getMessage() : DeathChestMessage.DEATHCHEST_UNLOCKED.getMessage();
    }

    public boolean teleportPlayer(Player p) {
        if (p.hasPermission("deathchestpro.teleport")) {

            if (!teleportCooldown.test(p)) {
                p.sendMessage(DeathChestMessage.DEATHCHEST_TELEPORT_COOLDOWN.getChatMessage().replace("%time%", String.valueOf(teleportCooldown.remainingTime(p, TimeUnit.SECONDS))));
                return false;
            }

            DCVaultHook vaultHook = (DCVaultHook) DCHook.getHookByName("Vault");

            double cost = this.plugin.getSettings().getExpireGroup(p).getTeleportCost();

            if (vaultHook.getEconomy() != null) {
                if (!vaultHook.getEconomy().has(p, cost)) {
                    p.sendMessage(DeathChestMessage.DEATHCHEST_TELEPORT_NO_MONEY.getChatMessage());
                    return false;
                }

                vaultHook.getEconomy().withdrawPlayer(p, cost);
            }

            p.teleport(LocationUtil.getCenter(this.location.clone().add(0, 1, 0)));
            p.sendMessage(DeathChestMessage.DEATHCHEST_TELEPORTED.getChatMessage());
            return true;
        } else {
            p.sendMessage(DeathChestMessage.NO_PERMISSION.getChatMessage());
            return false;
        }
    }

    public void fastLoot(Player p) {
        this.plugin.debug(p, "Player " + p.getName() + " attempted to fast loot.");
        if (p.hasPermission("deathchestpro.fastloot")) {
            this.plugin.debug(p, "Player " + p.getName() + " has permission to fast loot.");
            for (ItemStack i : chestInventory.getContents()) {
                if (i == null) continue;

                if (p.getInventory().firstEmpty() == -1) {
                    break;
                }

                if (this.plugin.getSettings().isAutoEquipArmor() && (CompMaterial.is(i.getType(), "helmet") || CompMaterial.is(i.getType(), "chestplate") || CompMaterial.is(i.getType(), "leggings") || CompMaterial.is(i.getType(), "boots"))) {
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
                restoreExp(p);
                removeDeathChest(true);
            } else {
                p.sendMessage(DeathChestMessage.DEATHCHEST_FASTLOOT_COMPLETE.getChatMessage().replaceAll("%amount%", String.valueOf(DeathChestManager.getAmountOfItems(chestInventory))));
            }

        } else {
            this.plugin.debug(p, "Player " + p.getName() + " does not have permission to fast loot.");
            p.sendMessage(DeathChestMessage.NO_PERMISSION.getChatMessage());
        }
    }

    private boolean autoEquip(Player p, ItemStack i) {
        if (CompMaterial.is(i.getType(),"helmet") && p.getInventory().getHelmet() == null) {
            p.getInventory().setHelmet(i);
            return true;
        } else if (CompMaterial.is(i.getType(),"chestplate") && p.getInventory().getChestplate() == null) {
            p.getInventory().setChestplate(i);
            return true;
        } else if (CompMaterial.is(i.getType(),"leggings") && p.getInventory().getLeggings() == null) {
            p.getInventory().setLeggings(i);
            return true;
        } else if (CompMaterial.is(i.getType(),"boots") && p.getInventory().getBoots() == null) {
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
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".died", this.deathDate.getTime());
        this.plugin.getFileManager().getConfig("deathchests.yml").set("chests." + this.chestUUID.toString() + ".exp", this.playerExp);
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
            this.hologram.updateHologram();
        }
    }

    public OfflinePlayer getKiller() {
        return killer;
    }

    public DeathChestHologram getHologram() {
        return this.hologram;
    }

    public DeathChestPro getPlugin() {
        return this.plugin;
    }

    public Date getDeathDate() {
        return this.deathDate;
    }


    public void restoreExp(Player p) {
        p.giveExp((int) (this.playerExp / 100.0 * this.plugin.getSettings().getStoreExperiencePercentage()));
        this.playerExp = 0;
    }

    public void unlock() {
        if (this.locked) {
            this.locked = false;
        }
    }

    public void unload() {
        DeathChestPro.getInstance().debug(null, "Unloading hologram at %s", this);
        this.unloaded = true;
        if (this.hologram != null) {
            this.hologram.despawn();
        }
    }

    public void load() {
        DeathChestPro.getInstance().debug(null,"Loading hologram at %s", this);
        this.unloaded = false;
        if (this.hologram != null) {
            this.hologram.spawn();
        }
    }

    @Override
    public String toString() {
        return "DeathChest{" +
                "chestUUID=" + chestUUID +
                ", player=" + player +
                ", location=" + location +
                ", locked=" + locked +
                '}';
    }
}
