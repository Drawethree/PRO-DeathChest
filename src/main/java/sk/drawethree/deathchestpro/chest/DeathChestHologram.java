package sk.drawethree.deathchestpro.chest;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.misc.DCHook;
import sk.drawethree.deathchestpro.utils.LocationUtil;
import sk.drawethree.deathchestpro.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DeathChestHologram {

    public static final String ENTITY_METADATA = "dcp";
    private static final double LINE_SPACER = 0.25;
    private static final double ARMOR_STAND_HEIGHT = 2.0;

    private DeathChest deathChest;
    private Location location;
    @Getter
    private boolean spawned;
    private ArrayList<ArmorStand> armorStands;

    public DeathChestHologram(DeathChest deathChest) {
        this.deathChest = deathChest;
        this.location = deathChest.getLocation().clone();
        this.spawn();
    }

    public void spawn() {

        if (this.spawned) {
            this.deathChest.getPlugin().broadcast(DeathChestPro.BroadcastType.DEBUG, "Cannot spawn hologram because its already spawned.");
            return;
        }

        this.deathChest.getPlugin().broadcast(DeathChestPro.BroadcastType.DEBUG, "Spawning hologram.");
        this.armorStands = new ArrayList<>();
        this.location = this.deathChest.getLocation().clone();

        for (String s : this.deathChest.getPlugin().getSettings().getHologramLines()) {
            s = s.replaceAll("%locked%", deathChest.getLockedString())
                    .replaceAll("%player%", deathChest.getOwner().getName())
                    .replaceAll("%item_count%", String.valueOf(deathChest.getItemCount()))
                    .replaceAll("%death_date%", this.deathChest.getPlugin().getSettings().getDeathDateFormat().format(new Date()))
                    .replaceAll("%timeleft%", deathChest.getTimeLeft() == -1 ? "∞" : new Time(deathChest.getTimeLeft(), TimeUnit.SECONDS).toString());

            if (DCHook.getHook("PlaceholderAPI")) {
                s = PlaceholderAPI.setPlaceholders(deathChest.getOwner(), s);
            }

            this.appendTextLine(s);
        }

        this.teleport(LocationUtil.getCenter(this.location.clone().add(0, 0.5 + this.getHeight(), 0)));

        this.spawned = true;
    }

    private void removeLine(int lineNumber) {
        this.armorStands.remove(lineNumber).remove();
        this.update();
    }

    private void appendTextLine(String text) {
        ArmorStand as = (ArmorStand) this.location.getWorld().spawnEntity(this.location.clone().subtract(0, this.armorStands.size() * LINE_SPACER, 0), EntityType.ARMOR_STAND);

        as.setGravity(false);
        as.setCanPickupItems(false);
        as.setBasePlate(false);
        as.setCustomName(text);
        as.setCustomNameVisible(true);
        as.setVisible(false);

        as.setMetadata(DeathChestHologram.ENTITY_METADATA, new FixedMetadataValue(DeathChestPro.getInstance(), DeathChestHologram.ENTITY_METADATA));

        this.armorStands.add(as);

        this.update();
    }

    private void update() {
        for (int i = 0; i < this.armorStands.size(); i++) {
            ArmorStand as = this.armorStands.get(i);
            as.teleport(this.location.clone().subtract(0, ARMOR_STAND_HEIGHT + (i * LINE_SPACER), 0));
        }
    }


    private void teleport(Location newLocation) {
        this.location = newLocation;
        this.update();
    }

    public Location getLocation() {
        return location;
    }

    private double getHeight() {
        return this.armorStands.size() * LINE_SPACER;
    }

    public void despawn() {

        if (!this.spawned) {
            this.deathChest.getPlugin().broadcast(DeathChestPro.BroadcastType.DEBUG, "Cannot despawn hologram because its not spawned.");
            return;
        }

        this.deathChest.getPlugin().broadcast(DeathChestPro.BroadcastType.DEBUG, "Despawning hologram.");

        for (ArmorStand as : new ArrayList<>(this.armorStands)) {
            this.deathChest.getPlugin().broadcast(DeathChestPro.BroadcastType.DEBUG, "Removing hologram line.");
            armorStands.remove(as);
            as.remove();
            as = null;
        }

        this.spawned = false;
    }

    private void setLine(int lineNumber, String text) {
        if (this.armorStands != null) this.armorStands.get(lineNumber).setCustomName(text);
    }

    public void updateHologram(int timeLeft) {
        for (int i = 0; i < this.deathChest.getPlugin().getSettings().getHologramLines().size(); i++) {
            String line = this.deathChest.getPlugin().getSettings().getHologramLines().get(i);
            if (line.contains("%timeleft%")) {
                int lineNumber = i;

                /*if (DeathChestPro.isDisplayPlayerHead()) {
                    lineNumber += 1;
                }*/

                this.setLine(lineNumber, line.replaceAll("%timeleft%", timeLeft == -1 ? "∞" : new Time(timeLeft, TimeUnit.SECONDS).toString()));
            } else if (line.contains("%locked%")) {
                this.setLine(i, line.replaceAll("%locked%", deathChest.getLockedString())
                        .replaceAll("%item_count%", String.valueOf(deathChest.getItemCount())));
            }
        }
    }

}
