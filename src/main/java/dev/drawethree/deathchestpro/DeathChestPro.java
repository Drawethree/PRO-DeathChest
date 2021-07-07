package dev.drawethree.deathchestpro;

import dev.drawethree.deathchestpro.listeners.DeathChestListener;
import dev.drawethree.deathchestpro.misc.DeathChestSettings;
import dev.drawethree.deathchestpro.misc.hook.DCHook;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.drawethree.deathchestpro.api.DeathChestProAPI;
import dev.drawethree.deathchestpro.api.DeathChestProAPIImpl;
import dev.drawethree.deathchestpro.commands.DeathChestCommand;
import dev.drawethree.deathchestpro.enums.DeathChestMenuItems;
import dev.drawethree.deathchestpro.enums.DeathChestMessage;
import dev.drawethree.deathchestpro.listeners.DeathChestHologramListener;
import dev.drawethree.deathchestpro.managers.DeathChestManager;
import dev.drawethree.deathchestpro.managers.FileManager;
import dev.drawethree.deathchestpro.utils.Metrics;

@Getter
public final class DeathChestPro extends JavaPlugin {

    private static DeathChestPro instance;
    private FileManager fileManager;
    private DeathChestManager deathChestManager;
    private DeathChestSettings settings;

    private static DeathChestProAPI api;

    private FileManager.Config mainConfig;
    private FileManager.Config itemConfig;
    private FileManager.Config deathChestsConfig;
    private FileManager.Config messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        this.fileManager = new FileManager(this);

        this.loadAllConfigs();
        this.reloadAllConfigs();
        this.approveConfigChanges();

        this.settings = new DeathChestSettings(this);
        this.settings.loadSettings();

        this.hook();

        this.deathChestManager = new DeathChestManager(this);
        this.deathChestManager.removeExistingHolograms();

        this.deathChestManager.loadDeathChests();

        this.getServer().getPluginManager().registerEvents(new DeathChestListener(this), this);
        this.getServer().getPluginManager().registerEvents(new DeathChestHologramListener(this), this);

        this.getCommand("deathchest").setExecutor(new DeathChestCommand(this));
        this.broadcast(BroadcastType.INFO, "§aThis server is using §e" + this.getName() + " §arunning on version §e" + this.getDescription().getVersion() + " §aby TheRealDrawe");

        api = new DeathChestProAPIImpl(this);
    }

    private void hook() {
        DCHook.attemptHooks();
        new Metrics(this);
    }

    public void debug(Player p, String message, Object... placeholders) {

            if (this.settings.isDebugMode()) {
                Bukkit.getConsoleSender().sendMessage(BroadcastType.DEBUG + " §cDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §c" + String.format(message, placeholders));
                if (p != null && p.isOp()) {
                    p.sendMessage(BroadcastType.DEBUG + " §cDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §c" + String.format(message, placeholders));
                }
            }
        }


    public void broadcast(BroadcastType type, String message, Object... placeholders) {
        Bukkit.getConsoleSender().sendMessage(type + " §cDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §c" + String.format(message, placeholders));
    }

    private void approveConfigChanges() {
        this.mainConfig.get().set("remove_chest_time", null);
        this.mainConfig.get().set("protect_chests", null);
        this.mainConfig.get().set("hologram.display_player_head", null);
        this.mainConfig.get().set("teleport_cost", null);
        this.mainConfig.save();
    }

    private void loadAllConfigs() {
        this.mainConfig = fileManager.getConfig("config.yml");
        this.itemConfig = fileManager.getConfig("items.yml");
        this.deathChestsConfig = fileManager.getConfig("deathchests.yml");
        this.messagesConfig = fileManager.getConfig("messages.yml");

        this.mainConfig.copyDefaults(true).save();
        this.itemConfig .copyDefaults(true).save();
        this.deathChestsConfig.copyDefaults(true).save();
        this.messagesConfig.copyDefaults(true).save();
    }

    private void reloadAllConfigs() {
        for (FileManager.Config c : FileManager.configs.values()) {
            c.reload();
        }
    }

    public void reloadPlugin() {
        reloadAllConfigs();
        DeathChestMessage.reload();
        DeathChestMenuItems.reload();
        this.settings.loadSettings();
    }

    @Override
    public void onDisable() {
        this.deathChestManager.saveDeathChests();
    }

    public static DeathChestPro getInstance() {
        return instance;
    }

    public  FileManager getFileManager() {
        return fileManager;
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
