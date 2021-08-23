package me.plytki.virtualrealty.managers;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.objects.Cuboid;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.objects.math.BlockVector2;
import me.plytki.virtualrealty.objects.math.BlockVector3;
import me.plytki.virtualrealty.sql.SQL;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotManager {

    public static ArrayList<Plot> plots = new ArrayList<>();

    public static void loadPlots() {
        try {
            ResultSet rs = SQL.getStatement().executeQuery("SELECT * FROM `vr_plots`");
            while (rs.next()) {
                plots.add(new Plot(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Plot createPlot(Location creationLocation, PlotSize plotSize) {
        Plot plot = new Plot(creationLocation, plotSize.getFloorMaterial(), plotSize.getFloorMaterialData(), plotSize);
        plots.add(plot);
        plot.insert();
        return plot;
    }

    public static Plot createPlot(Location creationLocation, PlotSize plotSize, Material material) {
        Plot plot = new Plot(creationLocation, material, plotSize.getFloorMaterialData(), plotSize);
        plots.add(plot);
        plot.insert();
        return plot;
    }

    public static Plot createPlot(Location creationLocation, int length, int width, int height) {
        Plot plot = new Plot(creationLocation, Material.matchMaterial("GRASS_BLOCK"), ((byte) 0), length, width, height);
        plots.add(plot);
        plot.insert();
        return plot;
    }

    public static Plot createPlot(Location creationLocation, int length, int width, int height, Material material, byte materialData) {
        Plot plot = new Plot(creationLocation, material, materialData, length, width, height);
        plots.add(plot);
        plot.insert();
        return plot;
    }

    public static Plot getPlot(int ID) {
        for (Plot plot : plots) {
            if(plot.getID() == ID) {
                return plot;
            }
        }
        return null;
    }

    public static List<Plot> getPlayerPlots(UUID owner) {
        List<Plot> playerPlots = new ArrayList<>();
        for (Plot plot : plots) {
            if (plot.getOwnedBy() != null) {
                if (plot.getOwnedBy().equals(owner)) {
                    playerPlots.add(plot);
                }
            }
        }
        return playerPlots;
    }

    public static Plot getPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            //CuboidRegion region = new CuboidRegion(plot.getBottomLeftCorner(), plot.getTopRightCorner());
            Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
            if(region.contains(newVector)) {
                return plot;
            }
        }
        return null;
    }

    public static boolean isLocationInPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
        return region.contains(newVector);
    }

    public static Plot getBorderedPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
            if(region.contains(newVector)) {
                return plot;
            }
        }
        return null;
    }

    public static boolean isLocationInBorderedPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
        return region.contains(newVector);
    }

    public static boolean isColliding(Cuboid newPlot) {
        //long time = System.currentTimeMillis();
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), plot.getCreatedLocation().getWorld());
            for (BlockVector2 vector2 : region.getWalls()) {
                if (vector2.containedWithin(newPlot.getMinimumPoint(), newPlot.getMaximumPoint())) {
                    return true;
                }
            }
        }
        //System.out.println("isColliding() time " + (System.currentTimeMillis() - time));
        return false;
    }

}