package com.modnmetl.virtualrealty.managers;

import com.modnmetl.virtualrealty.enums.dynmap.HighlightType;
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
import org.dynmap.markers.AreaMarker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlotManager {

    private static final String MARKER_STRING = "<h3>Plot #%s</h3><b>Owned By: </b>Available";
    private static final String MARKER_OWNED_STRING = "<h3>Plot #%s</h3><b>Owned By: </b>%s<br><b>Owned Until: </b>%s";

    public static Set<AreaMarker> areaMarkers = new HashSet<>();
    @Getter
    private static Set<Plot> plots = new LinkedHashSet<>();

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
            if(plot.getID() == ID) {
                return plot;
            }
        }
        return null;
    }

    public static HashMap<Integer, Plot> getPlots(UUID owner) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if(plot.getOwnedBy() != null && plot.getOwnedBy().equals(owner)) {
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
            if(region.contains(newVector)) {
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
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), plot.getCreatedLocation().getWorld());
            for (BlockVector2 vector2 : region.getWalls()) {
                if (vector2.containedWithin(newPlot.getMinimumPoint(), newPlot.getMaximumPoint())) {
                    return plot.getCreatedWorldString().equals(newPlot.getWorld().getName());
                }
            }
        }
        return false;
    }

    private static AreaMarker getAreaMarker(String areaMarkerName) {
        for (AreaMarker areaMarker : VirtualRealty.markerset.getAreaMarkers()) {
            if (areaMarker.getMarkerID().equalsIgnoreCase(areaMarkerName)) {
                return areaMarker;
            }
        }
        return null;
    }

    public static void resetPlotMarker(Plot plot) {
        if (!VirtualRealty.isDynmapPresent) return;
        LocalDateTime localDateTime = plot.getOwnedUntilDate();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String ownedBy;
        double opacity;
        int color;
        if (plot.getOwnedBy() == null) {
            ownedBy = "Available";
            color = VirtualRealty.getPluginConfiguration().dynmapMarkersColor.available.getHexColor();
            opacity = VirtualRealty.getPluginConfiguration().dynmapMarkersColor.available.opacity;
        } else {
            ownedBy = plot.getPlotOwner().getName();
            color = VirtualRealty.getPluginConfiguration().dynmapMarkersColor.owned.getHexColor();
            opacity = VirtualRealty.getPluginConfiguration().dynmapMarkersColor.owned.opacity;
        }
        if (VirtualRealty.getPluginConfiguration().dynmapType == HighlightType.OWNED && plot.getOwnedBy() == null) return;
        if (VirtualRealty.getPluginConfiguration().dynmapType == HighlightType.AVAILABLE && plot.getOwnedBy() != null) return;
        AreaMarker marker = getAreaMarker("virtualrealty.plots." + plot.getID());
        if (marker == null) {
            marker = VirtualRealty.markerset.createAreaMarker("virtualrealty.plots." + plot.getID(),
                    plot.getOwnedBy() == null ? String.format(MARKER_STRING, plot.getID()) : String.format(MARKER_OWNED_STRING, plot.getID(), ownedBy, dateTimeFormatter.format(localDateTime)), true,
                    plot.getCreatedWorldString(), new double[]{plot.getXMin(), plot.getXMax()}, new double[]{plot.getZMin(), plot.getZMax()}, true);
            areaMarkers.add(marker);
        } else {
            marker.setLabel(
                    plot.getOwnedBy() == null ? String.format(MARKER_STRING, plot.getID()) : String.format(MARKER_OWNED_STRING, plot.getID(), ownedBy, dateTimeFormatter.format(localDateTime)), true);
        }
        marker.setFillStyle(opacity, color);
        marker.setLineStyle(2, 0.8, 0x474747);
        marker.setMarkerSet(VirtualRealty.markerset);
    }

    public static void removeDynMapMarker(Plot plot) {
        if (!VirtualRealty.isDynmapPresent || VirtualRealty.dapi == null || VirtualRealty.markerset == null) return;
        AreaMarker marker = VirtualRealty.markerset.findAreaMarker("virtualrealty.plots." + plot.getID());
        areaMarkers.remove(marker);
        marker.deleteMarker();
    }

}