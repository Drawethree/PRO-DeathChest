package sk.drawethree.deathchestpro;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import sk.drawethree.deathchestpro.commands.DeathChestCommand;
import sk.drawethree.deathchestpro.enums.DeathChestItems;
import sk.drawethree.deathchestpro.enums.DeathChestMessage;
import sk.drawethree.deathchestpro.listeners.DeathChestHologramListener;
import sk.drawethree.deathchestpro.listeners.DeathChestListener;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.managers.FileManager;
import sk.drawethree.deathchestpro.misc.hook.DCHook;
import sk.drawethree.deathchestpro.misc.DeathChestSettings;
import sk.drawethree.deathchestpro.utils.Metrics;

@Getter
public final class DeathChestPro extends JavaPlugin {

    private static DeathChestPro instance;
    private FileManager fileManager;
    private DeathChestManager deathChestManager;
    private DeathChestSettings settings;

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
    }

    private void hook() {
        DCHook.attemptHooks();
        new Metrics(this);
    }

    public void broadcast(BroadcastType type, String message, Object... placeholders) {
        if (type == BroadcastType.DEBUG && !this.settings.isDebugMode()) {
            return;
        }
        Bukkit.getConsoleSender().sendMessage(type + " §cDeathChestPro " + getInstance().getDescription().getVersion() + " §8>> §c" + String.format(message, placeholders));

    }

    private void approveConfigChanges() {
        fileManager.getConfig("config.yml").get().set("remove_chest_time", null);
        fileManager.getConfig("config.yml").get().set("protect_chests", null);
        fileManager.getConfig("config.yml").get().set("hologram.display_player_head", null);
        fileManager.getConfig("config.yml").get().set("teleport_cost", null);
        fileManager.getConfig("config.yml").save();
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

    public void reloadPlugin() {
        reloadAllConfigs();
        DeathChestMessage.reload();
        DeathChestItems.reload();
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
