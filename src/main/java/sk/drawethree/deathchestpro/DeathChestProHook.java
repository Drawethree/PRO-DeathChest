package sk.drawethree.deathchestpro;

public enum DeathChestProHook {

    RESIDENCE("Residence"),
    PLACEHOLDER_API("PlaceholderAPI"),
    WORLDGUARD("WorldGuard"),
    GRIEF_PREVENTION("GriefPrevention");

    private String pluginName;
    private boolean enabled;

    DeathChestProHook(String pluginName) {
        this.pluginName = pluginName;
        this.enabled = false;
    }

    public void hook() {
        this.enabled = DeathChestPro.getInstance().getServer().getPluginManager().isPluginEnabled(this.pluginName);

        if(this.enabled) {
            DeathChestPro.broadcast(DeathChestPro.BroadcastType.INFO, "Successfully hooked into " + this.pluginName + " !");
        }

    }

    public boolean isEnabled() {
        return enabled;
    }
}
