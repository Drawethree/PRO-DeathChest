package sk.drawethree.deathchestpro.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.managers.DeathChestManager;

public class DeathChestHologramListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "ChunkUnloadEvent fired.");
        if (event.getChunk() == null) {
            return;
        }
        DeathChestManager.getInstance().getDeathChests().stream().filter(deathChest -> deathChest.getLocation().getChunk().equals(event.getChunk())).forEach(DeathChest::despawnHologram);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "ChunkLoadEvent fired.");
        if (event.getChunk() == null) {
            return;
        }
        DeathChestManager.getInstance().getDeathChests().stream().filter(deathChest -> deathChest.getLocation().getChunk().equals(event.getChunk())).forEach(DeathChest::spawnHologram);
    }

}
