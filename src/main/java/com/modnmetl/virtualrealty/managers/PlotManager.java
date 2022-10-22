package com.modnmetl.virtualrealty.managers;

import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.objects.math.BlockVector2;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.math.BlockVector3;
import com.modnmetl.virtualrealty.sql.Database;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlotManager {

    @Getter
    private static final Set<Plot> plots = new LinkedHashSet<>();

    public static void loadPlots() {
        plots.clear();
        try {
            ResultSet rs = Database.getInstance().getStatement().executeQuery("SELECT * FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "`");
            while (rs.next()) {
                plots.add(new Plot(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Plot createPlot(Location creationLocation, PlotSize plotSize, int length, int height, int width, boolean natural) {
        Plot plot = new Plot(creationLocation, Material.matchMaterial(VirtualRealty.legacyVersion ? "GRASS" : "GRASS_BLOCK"), Material.matchMaterial(VirtualRealty.legacyVersion ? "STEP" : "STONE_BRICK_SLAB"), plotSize, length, width, height, natural);
        plots.add(plot);
        long time = System.currentTimeMillis();
        plot.insert();
        VirtualRealty.debug("Plot database insertion time: " + (System.currentTimeMillis() - time) + " ms");
        return plot;
    }

    public static Plot getPlot(int ID) {
        for (Plot plot : plots) {
            if (plot.getID() == ID) {
                return plot;
            }
        }
        return null;
    }

    public static List<Plot> getPlots(String world) {
        List<Plot> newPlots = new LinkedList<>();
        for (Plot plot : plots) {
            if (plot.getCreatedWorldString().equals(world)) newPlots.add(plot);
        }
        return newPlots;
    }

    public static HashMap<Integer, Plot> getPlots(UUID owner) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(owner)) {
                plotHashMap.put(plot.getID(), plot);
            }
        }
        return plotHashMap;
    }

    public static HashMap<Integer, Plot> getAccessPlots(UUID player) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if (plot.getMember(player) != null || (plot.getOwnedBy() != null && plot.getPlotOwner().getUniqueId() == player)) {
                plotHashMap.put(plot.getID(), plot);
            }
        }
        return plotHashMap;
    }

    public static int getPlotMinID() {
        return plots.isEmpty() ? 0 : plots.stream().findFirst().get().getID();
    }

    public static int getPlotMaxID() {
        Plot[] plotArray = PlotManager.plots.toArray(new Plot[0]);
        Plot lastPlot = plotArray[plotArray.length - 1];
        return lastPlot.getID();
    }

    public static List<Plot> getPlayerPlots(UUID owner) {
        LinkedList<Plot> playerPlots = new LinkedList<>();
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
            Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
            if (region.isIn(newVector, plot.getCreatedWorld())) {
                return plot;
            }
        }
        return null;
    }

    public static void removePlotFromList(Plot plot) {
        plots.remove(plot);
    }

    public static boolean isLocationInPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
        return region.isIn(newVector, location.getWorld());
    }

    public static Plot getBorderedPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
            if (region.isIn(newVector, plot.getCreatedWorld())) {
                return plot;
            }
        }
        return null;
    }

    public static boolean isLocationInBorderedPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
        return region.isIn(newVector, plot.getCreatedWorld());
    }

}