package sk.drawethree.deathchestpro.utils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.bukkit.Location;

public class WorldGuard_1_8 {

    public static ApplicableRegionSet getRegions(Location loc) {
        return WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(BlockVector3.at(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()));
    }
}
