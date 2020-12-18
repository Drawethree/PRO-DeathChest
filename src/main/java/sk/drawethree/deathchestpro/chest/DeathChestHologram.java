package sk.drawethree.deathchestpro.chest;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.misc.hook.DCHook;
import sk.drawethree.deathchestpro.utils.LocationUtil;
import sk.drawethree.deathchestpro.utils.comp.MinecraftVersion;
import sk.drawethree.deathchestpro.utils.Time;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DeathChestHologram {

    public static final String ENTITY_METADATA = "dcp";
    private static final double LINE_SPACER = 0.25;
    private static final double ARMOR_STAND_HEIGHT = 2.0;

    private DeathChest deathChest;
    @Getter
    private boolean spawned;
    private ArrayList<ArmorStand> armorStands;

    public DeathChestHologram(DeathChest deathChest) {
        this.deathChest = deathChest;
        this.spawn();
    }

    public void spawn() {

        if (this.spawned) {
            this.deathChest.getPlugin().debug(null, "Cannot spawn hologram because its already spawned.");
            return;
        }

        this.deathChest.getPlugin().debug(null, "Spawning hologram.");
        this.armorStands = new ArrayList<>();

        for (String s : this.deathChest.getPlugin().getSettings().getHologramLines()) {

            s = this.replaceHologramPlaceholders(s);
            s = this.replacePlaceholderAPI(s);

            this.appendTextLine(s);
        }

        this.show();
        this.spawned = true;
    }

    private String replacePlaceholderAPI(String s) {
        if (DCHook.getHook("PlaceholderAPI")) {
            s = PlaceholderAPI.setPlaceholders(deathChest.getOwner(), s);
        }
        return s;
    }

    private String replaceHologramPlaceholders(String s) {
        s = s.replaceAll("%locked%", deathChest.getLockedString())
                .replaceAll("%player%", deathChest.getOwner().getName())
                .replaceAll("%item_count%", String.valueOf(deathChest.getItemCount()))
                .replaceAll("%death_date%", this.deathChest.getPlugin().getSettings().getDeathDateFormat().format(this.deathChest.getDeathDate()))
                .replaceAll("%xp%", String.valueOf(deathChest.getPlayerExp()))
                .replaceAll("%timeleft%", deathChest.getTimeLeft() == -1 ? "∞" : new Time(deathChest.getTimeLeft(), TimeUnit.SECONDS).toString());
        return s;
    }

    private void removeLine(int lineNumber) {
        this.armorStands.remove(lineNumber).remove();
        this.update();
    }

    private void appendTextLine(String text) {
        ArmorStand as = this.deathChest.getLocation().getWorld().spawn(LocationUtil.getCenter(this.deathChest.getLocation().clone().subtract(0, this.armorStands.size() * LINE_SPACER, 0)), ArmorStand.class);

        as.setVisible(false);
        as.setCustomNameVisible(false);
        as.setSmall(true);
        as.setMarker(true);
        as.setArms(false);
        as.setBasePlate(false);
        as.setGravity(false);
        as.setCustomName(text);

        if(MinecraftVersion.atLeast(MinecraftVersion.V.v1_9)) {
            as.setAI(false);
            as.setCollidable(false);
            as.setInvulnerable(true);
        }

        as.setMetadata(DeathChestHologram.ENTITY_METADATA, new FixedMetadataValue(DeathChestPro.getInstance(), DeathChestHologram.ENTITY_METADATA));

        this.armorStands.add(as);

        this.update();
    }

    private void show() {
        this.armorStands.forEach(armorStand -> armorStand.setCustomNameVisible(true));
    }

    private void hide() {
        this.armorStands.forEach(armorStand -> armorStand.setCustomNameVisible(true));
    }

    private void update() {
        Location center = LocationUtil.getCenter(this.deathChest.getLocation().clone().add(0, 1.0 + this.getHeight(), 0));
        for (int i = 0; i < this.armorStands.size(); i++) {
            ArmorStand as = this.armorStands.get(i);
            as.teleport(center.subtract(0, LINE_SPACER, 0));
        }
    }

    private double getHeight() {
        return this.armorStands.size() * LINE_SPACER;
    }

    public void despawn() {

        if (!this.spawned) {
            this.deathChest.getPlugin().debug(null,"Cannot despawn hologram because its not spawned.");
            return;
        }

        this.deathChest.getPlugin().debug(null, "Despawning hologram.");

        this.armorStands.forEach(Entity::remove);
        this.armorStands.clear();

        this.spawned = false;
    }

    private boolean existsLine(int line) {
        try {
            this.armorStands.get(line);
            return this.armorStands.get(line) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void setLine(int lineNumber, String text) {
        if (this.armorStands != null) {
            if (!this.existsLine(lineNumber)) {
                this.appendTextLine(text);
                return;
            }
            this.armorStands.get(lineNumber).setCustomName(text);
        }
    }

    public void updateHologram() {

        if(!this.spawned || deathChest.isUnloaded()) {
            return;
        }

        for (int i = 0; i < this.deathChest.getPlugin().getSettings().getHologramLines().size(); i++) {
            String line = this.deathChest.getPlugin().getSettings().getHologramLines().get(i);
            line = this.replaceHologramPlaceholders(line);
            line = this.replacePlaceholderAPI(line);
            this.setLine(i, line);
        }
    }

}
