package sk.drawethree.deathchestpro.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.managers.DeathChestManager;


public class DeathChestHologramListener implements Listener {

    private DeathChestPro plugin;

    public DeathChestHologramListener(DeathChestPro plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {

        if (!this.plugin.getSettings().isHologramEnabled()) {
            return;
        }

        Chunk chunk = event.getChunk();

        if (chunk == null || !chunk.isLoaded()) {
            return;
        }

        for (DeathChest dc : this.plugin.getDeathChestManager().getDeathChestsByUUID().values()) {

            if(dc.getHologram().isSpawned()) {
                continue;
            }

            if (!chunk.getWorld().equals(dc.getLocation().getWorld())) {
                continue;
            }

            Location loc = dc.getLocation();

            if (loc.getWorld().getChunkAt(loc).equals(event.getChunk())) {
                dc.getHologram().spawn();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (!this.plugin.getSettings().isHologramEnabled()) {
            return;
        }

        for (DeathChest dc : this.plugin.getDeathChestManager().getDeathChestsByUUID().values()) {

            if(!dc.getHologram().isSpawned()) {
                continue;
            }

            if (!event.getChunk().getWorld().equals(dc.getLocation().getWorld())) {
                continue;
            }


            Location loc = dc.getLocation();

            if (loc.getWorld().getChunkAt(loc).equals(event.getChunk())) {
                dc.getHologram().despawn();
            }
        }
    }
}
