package sk.drawethree.deathchestpro.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.chest.DeathChestHologram;


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

        new ChunkLoadTask(event.getChunk()).runTaskLater(this.plugin, 0L);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (!this.plugin.getSettings().isHologramEnabled()) {
            return;
        }

        new ChunkUnloadTask(event.getChunk()).runTaskLater(this.plugin, 0L);
    }

    private class ChunkLoadTask extends BukkitRunnable {

        private Chunk chunk;

        public ChunkLoadTask(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run() {

            if (chunk == null || !chunk.isLoaded()) {
                return;
            }

            //Check for remaining holograms
            for (Entity e : chunk.getEntities()) {
                if (e.hasMetadata(DeathChestHologram.ENTITY_METADATA)) {
                    e.remove();
                }
            }

            /*for (DeathChest dc : plugin.getDeathChestManager().getDeathChestsByUUID().values()) {

                if (dc.getHologram().isSpawned() || !chunk.getWorld().equals(dc.getLocation().getWorld())) {
                    continue;
                }

                Location loc = dc.getLocation();

                if (loc.getWorld().getChunkAt(loc).equals(chunk)) {
                    dc.getHologram().spawn();
                }
            }*/
        }
    }

    private class ChunkUnloadTask extends BukkitRunnable {
        private Chunk chunk;

        public ChunkUnloadTask(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run() {
            for (DeathChest dc : plugin.getDeathChestManager().getDeathChestsByUUID().values()) {

                if (!dc.getHologram().isSpawned() || !chunk.getWorld().equals(dc.getLocation().getWorld())) {
                    continue;
                }

                Location loc = dc.getLocation();

                if (loc.getWorld().getChunkAt(loc).equals(chunk)) {
                    dc.getHologram().despawn();
                }
            }
        }
    }
}
