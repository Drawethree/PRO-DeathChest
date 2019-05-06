package sk.drawethree.deathchestpro.chest;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.utils.LocationUtil;
import sk.drawethree.deathchestpro.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DeathChestHologram {

    private static final double LINE_SPACER = 0.25;

    private Location location;
    private ArrayList<ArmorStand> armorStands;

    public DeathChestHologram(DeathChest deathChest, Location loc) {
        this.location = loc;
        this.armorStands = new ArrayList<>();
        this.inicialize(deathChest);
    }

    private void inicialize(DeathChest chest) {
        for (String s : DeathChestPro.getHologramLines()) {
            this.appendTextLine(s
                    .replaceAll("%locked%", chest.getLockedString())
                    .replaceAll("%player%", chest.getPlayer().getName())
                    .replaceAll("%death_date%", DeathChestPro.getDeathDateFormat().format(new Date()))
                    .replaceAll("%timeleft%", new Time(DeathChestPro.getRemoveChestAfter(), TimeUnit.SECONDS).toString()));
        }
        this.teleport(LocationUtil.getCenter(this.location.add(0, 0.5 + this.getHeight(), 0)));
    }

    private void removeLine(int lineNumber) {
        this.armorStands.remove(lineNumber).remove();
        this.update();
    }

    public void appendTextLine(String text) {
        ArmorStand as = (ArmorStand) this.location.getWorld().spawnEntity(this.location.clone().subtract(0, this.armorStands.size() * LINE_SPACER, 0), EntityType.ARMOR_STAND);

        as.setVisible(false);
        as.setCollidable(false);
        as.setInvulnerable(true);
        as.setGravity(false);
        as.setBasePlate(false);
        as.setCustomName(text);
        as.setCustomNameVisible(true);

        this.armorStands.add(as);
        this.update();
    }

    private void update() {
        for (int i = 0; i < this.armorStands.size(); i++) {
            ArmorStand as = this.armorStands.get(i);
            as.teleport(this.location.clone().subtract(0, as.getHeight() + (i * LINE_SPACER), 0));
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

    public void delete() {
        for (ArmorStand as : this.armorStands) {
            as.remove();
        }
    }

    private void setLine(int lineNumber, String text) {
        this.armorStands.get(lineNumber).setCustomName(text);
    }

    public void updateHologram(int timeLeft) {
        for (int i = 0; i < DeathChestPro.getHologramLines().size(); i++) {
            String line = DeathChestPro.getHologramLines().get(i);
            if (line.contains("%timeleft%")) {
                int lineNumber = i;

                /*if (DeathChestPro.isDisplayPlayerHead()) {
                    lineNumber += 1;
                }*/

                this.setLine(lineNumber, line.replaceAll("%timeleft%", new Time(timeLeft, TimeUnit.SECONDS).toString()));
            }
        }
    }

}
