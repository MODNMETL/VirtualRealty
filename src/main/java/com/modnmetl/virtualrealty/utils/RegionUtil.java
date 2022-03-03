package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.math.BlockVector3;
import org.bukkit.Location;

public class RegionUtil {

    public static Cuboid getRegion(Location location, Direction direction, int length, int height, int width) {
        Location location1;
        Location location2;
        switch(direction) {
            case SOUTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - width, location.getBlockY() + height, location.getBlockZ() + length);
                break;
            }
            case WEST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - length, location.getBlockY() + height, location.getBlockZ() - width);
                break;
            }
            case NORTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() + width, location.getBlockY() + height, location.getBlockZ() - length);
                break;
            }
            case EAST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + length, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() + height, location.getBlockZ() + width);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
        return new Cuboid(BlockVector3.at(location1.getBlockX(), location1.getBlockY(), location1.getBlockZ()),
                BlockVector3.at(location2.getBlockX(), location2.getBlockY(), location2.getBlockZ()), location.getWorld());
    }

}
