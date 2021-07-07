package dev.drawethree.deathchestpro.misc.hook.hooks;

import dev.drawethree.deathchestpro.DeathChestPro;
import dev.drawethree.deathchestpro.misc.DCExpansion;
import dev.drawethree.deathchestpro.misc.hook.DCHook;

public class DCPlaceholderAPIHook extends DCHook {

    public DCPlaceholderAPIHook() {
        super("PlaceholderAPI");
    }

    @Override
    protected void runHookAction() {
        new DCExpansion(DeathChestPro.getInstance()).register();
    }
}
