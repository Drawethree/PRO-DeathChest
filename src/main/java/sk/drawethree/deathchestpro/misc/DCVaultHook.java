package sk.drawethree.deathchestpro.misc;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import sk.drawethree.deathchestpro.DeathChestPro;

public class DCVaultHook extends DCHook {

    private Economy economy = null;

    public DCVaultHook() {
        super("Vault");
    }

    @Override
    protected void runHookAction() {
        if (DeathChestPro.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = DeathChestPro.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    public Economy getEconomy() {
        return economy;
    }
}
