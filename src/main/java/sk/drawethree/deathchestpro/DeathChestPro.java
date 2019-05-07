package sk.drawethree.deathchestpro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import sk.drawethree.deathchestpro.commands.DeathChestCommand;
import sk.drawethree.deathchestpro.listeners.DeathChestListener;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.managers.FileManager;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;
import sk.drawethree.deathchestpro.utils.Metrics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public final class DeathChestPro extends JavaPlugin {

    private static final int CONFIG_VERSION = 1;

    private static DeathChestPro instance;
    private static FileManager fileManager;
    private static List<String> disabledworlds = new ArrayList<>();
    private static List<String> disabledRegions = new ArrayList<>();

    private static boolean useResidence = false;
    private static boolean useDeathFeathers = false;
    private static boolean useWorldGuard = false;
    private static boolean useGriefPrevention = false;

    private static boolean allowBreakChests = false;
    private static boolean deathchestFireworks = true;
    private static boolean spawnChestOnHighestBlock = true;
    private static boolean dropItemsAfterExpire = false;
    private static boolean clickableMessage = false;
    private static boolean lavaProtection = false;
    private static boolean voidSpawning = false;
    private static boolean autoEquipArmor = true;
    //private static boolean saveXP = false;

    private static SimpleDateFormat deathDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private static List<String> hologramLines = new ArrayList<>();
    private static String deathChestInvTitle = "&7%player%'s DeathChest";
    private static int fireworkInterval = 5;
    private static int unlockChestAfter = 0;
    private static int removeChestAfter = 20;

    public static int getUnlockChestAfter() {
        return unlockChestAfter;
    }

    @Override
    public void onEnable() {
        instance = this;
        fileManager = new FileManager(this);

        this.loadAllConfigs();
        this.approveConfigChanges();
        this.setupVariables();

        this.hook();

        DeathChestManager.getInstance().loadDeathChests();

        getServer().getPluginManager().registerEvents(new DeathChestListener(), this);
        getCommand("deathchest").setExecutor(new DeathChestCommand());
        info("§aThis server is using §e" + this.getName() + " §arunning on version §e" + this.getDescription().getVersion() + " §aby TheRealDrawe");
    }


    private void hook() {
        useResidence = getServer().getPluginManager().isPluginEnabled("Residence");
        useWorldGuard = getServer().getPluginManager().isPluginEnabled("WorldGuard");
        useGriefPrevention = getServer().getPluginManager().isPluginEnabled("GriefPrevention");
        //this.useDeathFeathers = getServer().getPluginManager().isPluginEnabled("DeathFeathers");

        if (useResidence) {
            info("Successfully hooked into Residence !");
        }

        if (useGriefPrevention) {
            info("Successfully hooked into GriefPrevention !");
        }

        if (useWorldGuard) {
            info("Successfully hooked into WorldGuard !");
        }

        if (useDeathFeathers) {
            info("Successfully hooked into DeathFeathers !");
        }

        new Metrics(this);
    }

    private static void setupVariables() {

        int configVersion = fileManager.getConfig("config.yml").get().getInt("config_version");

        if (configVersion > DeathChestPro.CONFIG_VERSION) {
            warn("Config version %d is invalid ! Loading default config values.", configVersion);
            return;
        }

        if (configVersion == 1) {
            allowBreakChests = fileManager.getConfig("config.yml").get().getBoolean("allow_break_chests");
            removeChestAfter = fileManager.getConfig("config.yml").get().getInt("remove_chest_time");
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
            //saveXP = fileManager.getConfig("config.yml").get().getBoolean("save_xp");
        }
    }

    public static void warn(String message, Object... placeholders) {
        Bukkit.getConsoleSender().sendMessage("§7(§4!§7) §cDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §c" + String.format(message, placeholders));
    }

    public static void info(String message, Object... placeholders) {
        Bukkit.getConsoleSender().sendMessage("§7(§aInfo§7) §aDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §a" + String.format(message, placeholders));
    }

    private void approveConfigChanges() {
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

    public static boolean isUseWorldGuard() {
        return useWorldGuard;
    }

    public static boolean isUseResidence() {
        return useResidence;
    }

    public static int getRemoveChestAfter() {
        return removeChestAfter;
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
    public static boolean isUseGriefPrevention() {
        return useGriefPrevention;
    }

    public static boolean isVoidSpawning() {
        return voidSpawning;
    }

    public static boolean isAutoEquipArmor() {
        return autoEquipArmor;
    }
}
