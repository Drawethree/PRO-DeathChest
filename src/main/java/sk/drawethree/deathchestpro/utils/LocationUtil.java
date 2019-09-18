package sk.drawethree.deathchestpro.utils;

import org.bukkit.Location;

public class LocationUtil {

    public static Location getCenter(Location loc) {
        return loc.clone().add(loc.getX() >= 0 ? 0.5D : -0.5D, 0, loc.getZ() >= 0 ? 0.5D : -0.5D);
    }


}
