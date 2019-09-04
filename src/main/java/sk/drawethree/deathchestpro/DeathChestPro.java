package sk.drawethree.deathchestpro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sk.drawethree.deathchestpro.commands.DeathChestCommand;
import sk.drawethree.deathchestpro.listeners.DeathChestHologramListener;
import sk.drawethree.deathchestpro.listeners.DeathChestListener;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.managers.FileManager;
import sk.drawethree.deathchestpro.misc.DCHook;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;
import sk.drawethree.deathchestpro.utils.Metrics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class DeathChestPro extends JavaPlugin {


    private static final int CONFIG_VERSION = 1;
    private static final int DEFAULT_EXPIRE_TIME = 60;

    private static DeathChestPro instance;
    private static FileManager fileManager;
    private static LinkedHashMap<String, Integer> expireGroups = new LinkedHashMap<>();
    private static List<String> disabledworlds = new ArrayList<>();
    private static List<String> disabledRegions = new ArrayList<>();

    private static boolean allowBreakChests = false;
    private static boolean deathchestFireworks = true;
    private static boolean spawnChestOnHighestBlock = true;
    private static boolean dropItemsAfterExpire = false;
    private static boolean clickableMessage = false;
    private static boolean lavaProtection = false;
    private static boolean voidSpawning = false;
    private static boolean autoEquipArmor = true;
    private static boolean lavaSpawning = true;
    private static boolean debugMode = true;
    private static boolean hologramEnabled = true;
    private static boolean startTimerAtDeath = false;

    //private static boolean saveXP = false;

    private static SimpleDateFormat deathDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private static List<String> hologramLines = new ArrayList<>();
    private static String deathChestInvTitle = "&7%player%'s DeathChest";
    private static int fireworkInterval = 5;
    private static int unlockChestAfter = -1;
    private static boolean allowKillerLooting = false;
    private static double teleportCost = 0;


    @Override
    public void onEnable() {
        instance = this;
        fileManager = new FileManager(this);

        this.loadAllConfigs();
        this.approveConfigChanges();
        this.setupVariables();

        this.hook();

        DeathChestManager.getInstance().removeExistingHolograms();
        DeathChestManager.getInstance().loadDeathChests();

        this.getServer().getPluginManager().registerEvents(new DeathChestListener(), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestHologramListener(), this);

        getCommand("deathchest").setExecutor(new DeathChestCommand());
        broadcast(BroadcastType.INFO, "§aThis server is using §e" + this.getName() + " §arunning on version §e" + this.getDescription().getVersion() + " §aby TheRealDrawe");
    }

    private void hook() {
        DCHook.attemptHooks();
        new Metrics(this);
    }

    private static void setupVariables() {

        int configVersion = fileManager.getConfig("config.yml").get().getInt("config_version");

        if (configVersion > DeathChestPro.CONFIG_VERSION) {
            broadcast(BroadcastType.WARN, "Config version %d is invalid ! Loading default config values.", configVersion);
            return;
        }

        if (configVersion == 1) {
            allowBreakChests = fileManager.getConfig("config.yml").get().getBoolean("allow_break_chests");
            disabledworlds = fileManager.getConfig("config.yml").get().getStringList("disabled_worlds");
            disabledRegions = fileManager.getConfig("config.yml").get().getStringList("disabled_regions");
            deathchestFireworks = fileManager.getConfig("config.yml").get().getBoolean("deathchest_fireworks.enabled");
            fireworkInterval = fileManager.getConfig("config.yml").get().getInt("deathchest_fireworks.interval");
            hologramLines = color(fileManager.getConfig("config.yml").get().getStringList("hologram.lines"));
            spawnChestOnHighestBlock = fileManager.getConfig("config.yml").get().getBoolean("spawn_chest_on_highest_block");
            dropItemsAfterExpire = fileManager.getConfig("config.yml").get().getBoolean("drop_items_after_expire");
            clickableMessage = fileManager.getConfig("config.yml").get().getBoolean("clickable_message");
            deathDateFormat = new SimpleDateFormat(fileManager.getConfig("config.yml").get().getString("hologram.death_date_format"));
            deathChestInvTitle = ChatColor.translateAlternateColorCodes('&', fileManager.getConfig("config.yml").get().getString("deathchest_inv_title"));
            lavaProtection = fileManager.getConfig("config.yml").get().getBoolean("lava_protection");
            voidSpawning = fileManager.getConfig("config.yml").get().getBoolean("void_spawning_chest");
            unlockChestAfter = fileManager.getConfig("config.yml").get().getInt("unlock_chest_after");
            autoEquipArmor = fileManager.getConfig("config.yml").get().getBoolean("auto_equip_armor");
            lavaSpawning = fileManager.getConfig("config.yml").get().getBoolean("lava_spawning");
            debugMode = fileManager.getConfig("config.yml").get().getBoolean("debug_messages");
            hologramEnabled = fileManager.getConfig("config.yml").get().getBoolean("hologram.enabled");
            allowKillerLooting = fileManager.getConfig("config.yml").get().getBoolean("allow_killer_looting");
            startTimerAtDeath = fileManager.getConfig("config.yml").get().getBoolean("start_timer_at_death");
            teleportCost = fileManager.getConfig("config.yml").get().getDouble("teleport_cost");
            //saveXP = fileManager.getConfig("config.yml").get().getBoolean("save_xp");
            loadExpireGroups();
        }
    }

    private static void loadExpireGroups() {
        expireGroups = new LinkedHashMap<>();
        for (String key : fileManager.getConfig("config.yml").get().getConfigurationSection("expire_groups").getKeys(false)) {

            String permission = fileManager.getConfig("config.yml").get().getString("expire_groups." + key + ".permission");
            int time = fileManager.getConfig("config.yml").get().getInt("expire_groups." + key + ".time");

            broadcast(BroadcastType.DEBUG, String.format("Loaded group " + key + " with permission " + permission + " and expire time " + time + " seconds."));

            expireGroups.put(permission, time);
        }
    }

    public static void broadcast(BroadcastType type, String message, Object... placeholders) {
        if (type == BroadcastType.DEBUG && !debugMode) {
            return;
        }
        Bukkit.getConsoleSender().sendMessage(type + " §cDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §c" + String.format(message, placeholders));

    }

    private void approveConfigChanges() {
        fileManager.getConfig("config.yml").get().set("remove_chest_time", null);
        fileManager.getConfig("config.yml").get().set("protect_chests", null);
        fileManager.getConfig("config.yml").get().set("hologram.display_player_head", null);
        fileManager.getConfig("config.yml").save();
    }

    private static List<String> color(List<String> stringList) {
        for (int i = 0; i < stringList.size(); i++) {
            stringList.set(i, ChatColor.translateAlternateColorCodes('&', stringList.get(i)));
        }
        return stringList;
    }

    private static void loadAllConfigs() {
        fileManager.getConfig("config.yml").copyDefaults(true).save();
        fileManager.getConfig("items.yml").copyDefaults(true).save();
        fileManager.getConfig("deathchests.yml").copyDefaults(true).save();
        fileManager.getConfig("messages.yml").copyDefaults(true).save();
    }

    private static void reloadAllConfigs() {
        for (FileManager.Config c : FileManager.configs.values()) {
            c.reload();
        }
    }

    public static void reloadPlugin() {
        reloadAllConfigs();
        Message.reload();
        Items.reload();
        setupVariables();
    }

    @Override
    public void onDisable() {
        DeathChestManager.getInstance().saveDeathChests();
    }

    public static List<String> getDisabledworlds() {
        return disabledworlds;
    }

    public static DeathChestPro getInstance() {
        return instance;
    }

    public static boolean isAllowBreakChests() {
        return allowBreakChests;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public static List<String> getDisabledRegions() {
        return disabledRegions;
    }

    public static boolean isDeathchestFireworks() {
        return deathchestFireworks;
    }

    public static boolean isSpawnChestOnHighestBlock() {
        return spawnChestOnHighestBlock;
    }

    public static boolean isDropItemsAfterExpire() {
        return dropItemsAfterExpire;
    }

    public static boolean isClickableMessage() {
        return clickableMessage;
    }

    public static SimpleDateFormat getDeathDateFormat() {
        return deathDateFormat;
    }

    public static List<String> getHologramLines() {
        return hologramLines;
    }

    public static String getDeathChestInvTitle() {
        return deathChestInvTitle;
    }

    public static int getFireworkInterval() {
        return fireworkInterval;
    }

    public static boolean isLavaProtection() {
        return lavaProtection;
    }


    /*public static boolean isSaveXP() {
        return saveXP;
    }*/
    public static boolean isVoidSpawning() {
        return voidSpawning;
    }

    public static boolean isAutoEquipArmor() {
        return autoEquipArmor;
    }

    public static boolean isLavaSpawning() {
        return lavaSpawning;
    }

    public static int getUnlockChestAfter() {
        return unlockChestAfter;
    }

    public static boolean isAllowKillerLooting() {
        return allowKillerLooting;
    }

    public static boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public static boolean isStartTimerAtDeath() {
        return startTimerAtDeath;
    }

    public static double getTeleportCost() {
        return teleportCost;
    }

    public static int getExpireTimeForPlayer(Player player) {
        for (String perm : expireGroups.keySet()) {
            if (player.hasPermission(perm)) {
                return expireGroups.get(perm);
            }
        }
        return DeathChestPro.DEFAULT_EXPIRE_TIME;
    }

    public enum BroadcastType {
        WARN("§7(§4!§7)"),
        DEBUG("§7(§eDEBUG§7)"),
        INFO("§7(§aInfo§7)");

        private final String prefix;

        BroadcastType(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return this.prefix;
        }

    }
}
