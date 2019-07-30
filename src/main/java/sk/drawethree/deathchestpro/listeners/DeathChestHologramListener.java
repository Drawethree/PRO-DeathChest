package sk.drawethree.deathchestpro.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChestHologram;


public class DeathChestHologramListener implements Listener {


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getChunk() == null) {
            return;
        }

        if (event.getChunk().isLoaded()) {
            for (Entity e : event.getChunk().getEntities()) {
                if (!(e instanceof ArmorStand)) {
                    continue;
                }
                if (e.hasMetadata(DeathChestHologram.ENTITY_METADATA) && !DeathChestHologram.existHologram(e)) {
                    DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG, "Removing hologram entity.");
                    e.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() && DeathChestHologram.existHologram(event.getEntity())) {
            event.setCancelled(false);
        }
    }
}
