package sk.drawethree.deathchestpro.misc.hook.hooks;

import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.misc.DCExpansion;
import sk.drawethree.deathchestpro.misc.hook.DCHook;

public class DCPlaceholderAPIHook extends DCHook {

    public DCPlaceholderAPIHook() {
        super("PlaceholderAPI");
    }

    @Override
    protected void runHookAction() {
        new DCExpansion(DeathChestPro.getInstance()).register();
    }
}
