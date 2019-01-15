package sk.drawethree.deathchestpro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import sk.drawethree.deathchestpro.commands.DeathChestCommand;
import sk.drawethree.deathchestpro.listeners.DeathChestListener;
import sk.drawethree.deathchestpro.managers.FileManager;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;

import java.util.List;

public final class DeathChestPro extends JavaPlugin {

    private static DeathChestPro instance;
    private static FileManager fileManager;
    private List<String> disabledworlds;

    private boolean useResidence = false;
    private boolean useHolograms = false;
    private boolean useDeathFeathers = false;

    private boolean allowBreakChests = false;
    private boolean displayPlayerHead = true;
    private boolean deathchestFireworks = true;
    private boolean spawnChestOnHighestBlock = true;
    private boolean dropItemsAfterExpire = false;
    private boolean clickableMessage = false;

    private List<String> hologramLines;
    private int removeChestAfter = 20;

    @Override
    public void onEnable() {
        instance = this;
        fileManager = new FileManager(this);

        loadAllConfigs();
        approveConfigChanges();
        setupVariables();

        hook();

        getServer().getPluginManager().registerEvents(new DeathChestListener(), this);
        getCommand("deathchest").setExecutor(new DeathChestCommand());
        Bukkit.getConsoleSender().sendMessage(Message.PREFIX.getMessage() + "§aThis server is using §e" + this.getName() + " §arunning on version §e" + this.getDescription().getVersion() + " §aby TheRealDrawe");
    }


    private void hook() {
        this.useResidence = getServer().getPluginManager().isPluginEnabled("Residence");
        this.useHolograms = getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
        //this.useDeathFeathers = getServer().getPluginManager().isPluginEnabled("DeathFeathers");

        if (this.useResidence) {
            Bukkit.getConsoleSender().sendMessage("§7(§2Info§7) §eDeathChestPro " + getDescription().getVersion() + " §8>> §aSuccessfully hooked into Residence !");
        }

        if (this.useDeathFeathers) {
            Bukkit.getConsoleSender().sendMessage("§7(§2Info§7) §eDeathChestPro " + getDescription().getVersion() + " §8>> §aSuccessfully hooked into DeathFeathers !");
        }

        if (this.useHolograms) {
            Bukkit.getConsoleSender().sendMessage("§7(§2Info§7) §eDeathChestPro " + getDescription().getVersion() + " §8>> §aSuccessfully hooked into HolographicDisplays !");
        } else {
            Bukkit.getConsoleSender().sendMessage("§7(§4!§7) §eDeathChestPro " + getDescription().getVersion() + " §8>> §cHolograms plugin not found !");
            Bukkit.getConsoleSender().sendMessage("§7(§4!§7) §cDeathChestPro " + getDescription().getVersion() + " §8>> §cHolograms will not be used !");
        }
    }

    private void setupVariables() {
        this.allowBreakChests = fileManager.getConfig("config.yml").get().getBoolean("allow_break_chests");
        this.removeChestAfter = fileManager.getConfig("config.yml").get().getInt("remove_chest_time");
        this.disabledworlds = fileManager.getConfig("config.yml").get().getStringList("disabled_worlds");
        this.displayPlayerHead = fileManager.getConfig("config.yml").get().getBoolean("hologram.display_player_head");
        this.deathchestFireworks = fileManager.getConfig("config.yml").get().getBoolean("deathchest_fireworks");
        this.hologramLines = color(fileManager.getConfig("config.yml").get().getStringList("hologram.lines"));
        this.spawnChestOnHighestBlock = fileManager.getConfig("config.yml").get().getBoolean("spawn_chest_on_highest_block");
        this.dropItemsAfterExpire = fileManager.getConfig("config.yml").get().getBoolean("drop_items_after_expire");
        this.clickableMessage = fileManager.getConfig("config.yml").get().getBoolean("clickable_message");
    }

    private void approveConfigChanges() {
        fileManager.getConfig("config.yml").get().set("protect_chests", null);

        fileManager.getConfig("config.yml").save();
    }

    private List<String> color(List<String> stringList) {
        for (int i = 0; i < stringList.size(); i++) {
            stringList.set(i, ChatColor.translateAlternateColorCodes('&', stringList.get(i)));
        }
        return stringList;
    }

    private void loadAllConfigs() {
        fileManager.getConfig("config.yml").copyDefaults(true).save();
        fileManager.getConfig("items.yml").copyDefaults(true).save();
        fileManager.getConfig("deathchests.yml").copyDefaults(true).save();
        fileManager.getConfig("messages.yml").copyDefaults(true).save();
    }

    private void reloadAllConfigs() {
        for (FileManager.Config c : FileManager.configs.values()) {
            c.reload();
        }
    }

    @Override
    public void onDisable() {

    }

    public List<String> getDisabledworlds() {
        return disabledworlds;
    }

    public static DeathChestPro getInstance() {
        return instance;
    }

    public boolean isAllowBreakChests() {
        return allowBreakChests;
    }

    public boolean isUseHolograms() {
        return useHolograms;
    }

    public boolean isUseResidence() {
        return useResidence;
    }

    public void reloadPlugin() {
        reloadAllConfigs();
        Message.reload();
        Items.reload();
        setupVariables();
    }


    public int getRemoveChestAfter() {
        return removeChestAfter;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public boolean isDisplayPlayerHead() {
        return displayPlayerHead;
    }

    public boolean isDeathchestFireworks() {
        return deathchestFireworks;
    }

    public boolean isSpawnChestOnHighestBlock() {
        return spawnChestOnHighestBlock;
    }

    public boolean isUseDeathFeathers() {
        return useDeathFeathers;
    }

    public boolean isDropItemsAfterExpire() {
        return dropItemsAfterExpire;
    }

    public boolean isClickableMessage() {
        return clickableMessage;
    }
}
