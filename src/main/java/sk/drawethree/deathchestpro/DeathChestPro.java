package sk.drawethree.deathchestpro;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import sk.drawethree.deathchestpro.commands.DeathChestCommand;
import sk.drawethree.deathchestpro.listeners.DeathChestListener;
import sk.drawethree.deathchestpro.managers.FileManager;
import sk.drawethree.deathchestpro.utils.Items;
import sk.drawethree.deathchestpro.utils.Message;

import java.io.File;
import java.util.List;

public final class DeathChestPro extends JavaPlugin {

    private static DeathChestPro instance;
    private static FileManager fileManager;
    private List<String> disabledworlds;
    private boolean useHolograms = false;
    private boolean protectChests = false;
    private boolean displayPlayerHead = true;
    private boolean deathchestFireworks = true;
    private List<String> hologramLines;
    private int removeChestAfter = 20;

    @Override
    public void onEnable() {
        instance = this;
        fileManager = new FileManager(this);
        loadAllConfigs();
        setupVariables();
        checkForHologramsDependency();
        getServer().getPluginManager().registerEvents(new DeathChestListener(), this);
        getCommand("deathchest").setExecutor(new DeathChestCommand());
        Bukkit.getConsoleSender().sendMessage(Message.PREFIX.getMessage() + "§aThis server is using §e" + this.getName() + " §arunning on version §e" + this.getDescription().getVersion() + " §aby TheRealDrawe");
    }

    private void checkForHologramsDependency() {
        if (getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
            this.useHolograms = true;
            Bukkit.getConsoleSender().sendMessage("§7(§2Info§7) §eDeathChestPro " + getDescription().getVersion() + " §8>> §aPlugin for Holograms found !");
            Bukkit.getConsoleSender().sendMessage("§7(§2Info§7) §aHolograms will be used !");
        } else {
            Bukkit.getConsoleSender().sendMessage("§7(§4!§7) §eDeathChestPro " + getDescription().getVersion() + " §8>> §cHolograms plugin not found !");
            Bukkit.getConsoleSender().sendMessage("§7(§4!§7) §cDeathChestPro " + getDescription().getVersion() + " §8>> §cHolograms will not be used !");
        }
    }

    private void setupVariables() {
        this.protectChests = fileManager.getConfig("config.yml").get().getBoolean("protect_chests");
        this.removeChestAfter = fileManager.getConfig("config.yml").get().getInt("remove_chest_time");
        this.disabledworlds = fileManager.getConfig("config.yml").get().getStringList("disabled_worlds");
        this.displayPlayerHead = fileManager.getConfig("config.yml").get().getBoolean("hologram.display_player_head");
        this.deathchestFireworks = fileManager.getConfig("config.yml").get().getBoolean("deathchest_fireworks");
        this.hologramLines = fileManager.getConfig("config.yml").get().getStringList("hologram.lines");
    }

    private void loadAllConfigs() {
        fileManager.getConfig("config.yml").copyDefaults(true).save();
        fileManager.getConfig("items.yml").copyDefaults(true).save();
        fileManager.getConfig("deathchests.yml").copyDefaults(true).save();
        fileManager.getConfig("messages.yml").copyDefaults(true).save();
    }

    private void reloadAllConfigs() {
        for(FileManager.Config c : FileManager.configs.values()) {
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

    public boolean isProtectChests() {
        return protectChests;
    }

    public boolean isUseHolograms() {
        return useHolograms;
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
}
