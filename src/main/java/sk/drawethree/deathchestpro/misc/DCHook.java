package sk.drawethree.deathchestpro.misc;

import lombok.Getter;
import org.bukkit.Bukkit;
import sk.drawethree.deathchestpro.DeathChestPro;

import java.util.HashMap;

public abstract class DCHook {

    private static final HashMap<String, DCHook> hooks = new HashMap<>();

    static {
        hooks.put("Residence", new DCResidenceHook());
        hooks.put("PlaceholderAPI", new DCPlaceholderAPIHook());
        hooks.put("WorldGuard", new DCWorldGuardHook());
        hooks.put("GriefPrevention", new DCGriefPreventionHook());
        hooks.put("Vault", new DCVaultHook());
    }

    public static void attemptHooks() {
        for (DCHook hook : hooks.values()) {
            hook.hook();
        }
    }

    public static boolean getHook(String pluginName) {
        return hooks.get(pluginName).isEnabled();
    }

    public static DCHook getHookByName(String pluginName) {
        return hooks.get(pluginName);
    }


    @Getter
    private String pluginName;
    @Getter
    private boolean enabled;

    public DCHook(String pluginName) {
        this.pluginName = pluginName;
    }

    public void hook() {
        if (Bukkit.getPluginManager().isPluginEnabled(this.pluginName)) {
            DeathChestPro.getInstance().broadcast(DeathChestPro.BroadcastType.INFO, "§aSuccessfully hooked into §e" + this.pluginName + " §a!");
            this.enabled = true;
            this.runHookAction();
        }
    }

    protected abstract void runHookAction();
}
