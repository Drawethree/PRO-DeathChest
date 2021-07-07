package dev.drawethree.deathchestpro.listeners;

import dev.drawethree.deathchestpro.DeathChestPro;
import dev.drawethree.deathchestpro.chest.DeathChest;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import dev.drawethree.deathchestpro.chest.DeathChestHologram;


public class DeathChestHologramListener implements Listener {

    private DeathChestPro plugin;

    public DeathChestHologramListener(DeathChestPro plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {

        if (!this.plugin.getSettings().isHologramEnabled()) {
            return;
        }

        Chunk chunk = event.getChunk();

        if (chunk == null || !chunk.isLoaded()) {
            return;
        }

        //Check for remaining holograms
        for (Entity e : chunk.getEntities()) {
            if (e.hasMetadata(DeathChestHologram.ENTITY_METADATA)) {
                e.remove();
            }
        }

        for (DeathChest dc : plugin.getDeathChestManager().getDeathChestsByUUID().values()) {

            if (dc.getHologram().isSpawned() || !chunk.getWorld().equals(dc.getLocation().getWorld())) {
                continue;
            }

            Location loc = dc.getLocation();

            if (loc.getBlockX() >> 4 == chunk.getX() && loc.getBlockZ() >> 4 == chunk.getZ()) {
                dc.load();
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {


        if (!this.plugin.getSettings().isHologramEnabled()) {
            return;
        }

        Chunk chunk = event.getChunk();

        for (DeathChest dc : plugin.getDeathChestManager().getDeathChestsByUUID().values()) {

            if (!chunk.getWorld().equals(dc.getLocation().getWorld())) {
                continue;
            }

            Location loc = dc.getLocation();

            if (loc.getBlockX() >> 4 == chunk.getX() && loc.getBlockZ() >> 4 == chunk.getZ()) {
                dc.unload();
            }
        }
    }
}
