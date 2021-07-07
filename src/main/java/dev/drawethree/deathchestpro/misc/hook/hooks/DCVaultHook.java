package dev.drawethree.deathchestpro.misc.hook.hooks;

import dev.drawethree.deathchestpro.misc.hook.DCHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import dev.drawethree.deathchestpro.DeathChestPro;

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
