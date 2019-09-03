package sk.drawethree.deathchestpro.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;
import sk.drawethree.deathchestpro.chest.DeathChestHologram;
import sk.drawethree.deathchestpro.managers.DeathChestManager;


public class DeathChestHologramListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG,"ChunkLoadEvent");
        Chunk chunk = event.getChunk();
        if (chunk == null || !chunk.isLoaded()) {
            return;
        }

        for (DeathChest dc : DeathChestManager.getInstance().getDeathChestsByUUID().values()) {
            Location loc = dc.getHologram().getLocation();
            if (loc.getBlockZ() >> 4 == chunk.getZ() && loc.getBlockX() >> 4 == chunk.getX()) {
                dc.getHologram().spawn();
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        DeathChestPro.broadcast(DeathChestPro.BroadcastType.DEBUG,"ChunkUnloadEvent");

        for (DeathChest dc : DeathChestManager.getInstance().getDeathChestsByUUID().values() ) {
            Location loc = dc.getHologram().getLocation();
            if (loc.getBlockZ() >> 4 == event.getChunk().getZ() && loc.getBlockX() >> 4 == event.getChunk().getX()) {
                dc.getHologram().despawn();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() && DeathChestHologram.existLine(event.getEntity())) {
            event.setCancelled(false);
        }
    }
}
