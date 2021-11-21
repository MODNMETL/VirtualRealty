package me.plytki.virtualrealty.managers;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.HighlightType;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.objects.Cuboid;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.objects.math.BlockVector2;
import me.plytki.virtualrealty.objects.math.BlockVector3;
import me.plytki.virtualrealty.sql.SQL;
import org.bukkit.Location;
import org.bukkit.Material;
import org.dynmap.markers.AreaMarker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlotManager {

    private static final String markerString = "<h3>Plot #%s</h3><b>Owned By: </b>Available";
    private static final String markerOwnedString = "<h3>Plot #%s</h3><b>Owned By: </b>%s<br><b>Owned Until: </b>%s";

    public static Set<AreaMarker> areaMarkers = new HashSet<>();
    public static Set<Plot> plots = new LinkedHashSet<>();

    public static void loadPlots() {
        plots.clear();
        try {
            ResultSet rs = SQL.getStatement().executeQuery("SELECT * FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "`");
            while (rs.next()) {
                plots.add(new Plot(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Plot createPlot(Location creationLocation, PlotSize plotSize) {
        Plot plot = new Plot(creationLocation, plotSize.getFloorMaterial(), plotSize.getBorderMaterial(), plotSize);
        plots.add(plot);
        plot.insert();
        return plot;
    }

    public static Plot createPlot(Location creationLocation, int length, int width, int height) {
        Plot plot = new Plot(creationLocation, Material.matchMaterial(VirtualRealty.isLegacy ? "GRASS" : "GRASS_BLOCK"), Material.matchMaterial(VirtualRealty.isLegacy ? "STEP" : "STONE_BRICK_SLAB"), length, width, height);
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

    public static int getPlotMinID() {
        return plots.isEmpty() ? null : plots.stream().findFirst().get().getID();
    }

    public static int getPlotMaxID() {
        Plot[] plotArray = PlotManager.plots.toArray(new Plot[PlotManager.plots.size()]);
        Plot lastPlot = plotArray[plotArray.length - 1];
        return lastPlot.getID();
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
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), plot.getCreatedLocation().getWorld());
            for (BlockVector2 vector2 : region.getWalls()) {
                if (vector2.containedWithin(newPlot.getMinimumPoint(), newPlot.getMaximumPoint())) {
                    return true;
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

                    plot.getOwnedBy() == null ? String.format(markerString, plot.getID()) : String.format(markerOwnedString, plot.getID(), ownedBy, dateTimeFormatter.format(localDateTime)), true,

                    plot.getCreatedWorld(), new double[]{plot.getXMin(), plot.getXMax()}, new double[]{plot.getZMin(), plot.getZMax()}, true);

            areaMarkers.add(marker);
        } else {
            marker.setLabel(

                    plot.getOwnedBy() == null ? String.format(markerString, plot.getID()) : String.format(markerOwnedString, plot.getID(), ownedBy, dateTimeFormatter.format(localDateTime)), true);

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