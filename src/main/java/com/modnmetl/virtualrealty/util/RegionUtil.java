package com.modnmetl.virtualrealty.util;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.model.math.Direction;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.plot.Plot;
import com.modnmetl.virtualrealty.model.math.BlockVector2;
import com.modnmetl.virtualrealty.model.region.Cuboid;
import com.modnmetl.virtualrealty.model.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class RegionUtil {

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

    public static boolean isCollidingWithBedrock(Cuboid cuboid) {
        for (Block block : cuboid.getBlocks()) {
            if (block.getType() == Material.BEDROCK)
                return true;
        }
        return false;
    }

    public static boolean isCollidingWithAnotherPlot(Cuboid cuboid) {
        long time = System.currentTimeMillis();
        int spacing = VirtualRealty.getPluginConfiguration().plotSpacing;

        // Adjust the cuboid boundaries to include the spacing
        Cuboid expandedCuboid = new Cuboid(
                BlockVector3.at(cuboid.getMinimumPoint().getBlockX() - spacing,
                        cuboid.getMinimumPoint().getBlockY(),
                        cuboid.getMinimumPoint().getBlockZ() - spacing),
                BlockVector3.at(cuboid.getMaximumPoint().getBlockX() + spacing,
                        cuboid.getMaximumPoint().getBlockY(),
                        cuboid.getMaximumPoint().getBlockZ() + spacing),
                cuboid.getWorld()
        );

        for (Plot plot : PlotManager.getInstance().getPlots(cuboid.getWorld().getName())) {
            if (expandedCuboid.isColliding2D(plot.getCuboid()) || expandedCuboid.isWithin2D(plot.getCuboid())) {
                VirtualRealty.debug("Collision checked! (Found) " + (System.currentTimeMillis() - time) + " ms | Spacing: " + spacing);
                return true;
            }
        }
        VirtualRealty.debug("Collision checked! " + (System.currentTimeMillis() - time) + " ms | Spacing: " + spacing);
        return false;
    }

//    public static boolean isCollidingWithAnotherPlot(Cuboid cuboid) {
//        long time = System.currentTimeMillis();
//        for (Plot plot : PlotManager.getInstance().getPlots(cuboid.getWorld().getName())) {
//            for (BlockVector2 blockVector2 : plot.getCuboid().getFlatRegion()) {
//                if (cuboid.isIn(blockVector2, cuboid.getWorld(), VirtualRealty.getPluginConfiguration().plotSpacing)) {
//                    VirtualRealty.debug("Collision checked! (Found) " + (System.currentTimeMillis() - time) + " ms | Spacing: " + VirtualRealty.getPluginConfiguration().plotSpacing);
//                    return true;
//                }
//            }
//        }
//        VirtualRealty.debug("Collision checked! " + (System.currentTimeMillis() - time) + " ms | Spacing: " + VirtualRealty.getPluginConfiguration().plotSpacing);
//        return false;
//    }

}
