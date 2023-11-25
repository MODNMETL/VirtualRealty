package com.modnmetl.virtualrealty.manager;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.model.other.HighlightType;
import com.modnmetl.virtualrealty.model.plot.Plot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class DynmapManager {

    private final VirtualRealty instance;

    @Getter
    public boolean dynmapPresent = false;
    public DynmapAPI dapi = null;
    public MarkerSet markerset = null;
    public MarkerIcon markerIcon = null;
    private static final String MARKER_STRING = "<h3>Plot #%s</h3><b>Owned By: </b>Available";
    private static final String MARKER_OWNED_STRING = "<h3>Plot #%s</h3><b>Owned By: </b>%s<br><b>Owned Until: </b>%s";
    public static Set<AreaMarker> areaMarkers = new HashSet<>();


    public DynmapManager(VirtualRealty instance) {
        this.instance = instance;
    }

    public void registerDynmap() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
                if (plugin == null) return;
                dynmapPresent = true;
                if (!plugin.isEnabled()) return;
                dapi = (DynmapAPI) plugin;
                if (!dapi.markerAPIInitialized()) return;
                markerset = dapi.getMarkerAPI().getMarkerSet("virtualrealty.plots");
                if (markerset == null)
                    markerset = dapi.getMarkerAPI().createMarkerSet("virutalrealty.plots", "Plots", dapi.getMarkerAPI().getMarkerIcons(), false);
                for (MarkerSet markerSet : dapi.getMarkerAPI().getMarkerSets()) {
                    if (markerSet.getMarkerSetLabel().equalsIgnoreCase("Plots")) {
                        markerset = markerSet;
                    }
                }
                try {
                    if (dapi.getMarkerAPI().getMarkerIcon("virtualrealty_main_icon") == null) {
                        InputStream in = this.getClass().getResourceAsStream("/ploticon.png");
                        if (in != null && in.available() > 0) {
                            markerIcon = dapi.getMarkerAPI().createMarkerIcon("virtualrealty_main_icon", "Plots", in);
                        }
                    } else {
                        markerIcon = dapi.getMarkerAPI().getMarkerIcon("virtualrealty_main_icon");
                    }
                } catch (IOException ignored) {}
                VirtualRealty.debug("Registering plots markers..");
                for (Plot plot : PlotManager.getInstance().getPlots()) {
                    resetPlotMarker(plot);
                }
                VirtualRealty.debug("Registered plots markers");
                this.cancel();
            }
        }.runTaskTimer(instance, 20, 20 * 5);
    }

    private static AreaMarker getAreaMarker(String areaMarkerName) {
        if (VirtualRealty.getDynmapManager() == null) return null;
        for (AreaMarker areaMarker : VirtualRealty.getDynmapManager().markerset.getAreaMarkers()) {
            if (areaMarker.getMarkerID().equalsIgnoreCase(areaMarkerName)) return areaMarker;
        }
        return null;
    }

    public static void resetPlotMarker(Plot plot) {
        if (VirtualRealty.getDynmapManager() == null || !VirtualRealty.getDynmapManager().isDynmapPresent()) return;
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
            marker = VirtualRealty.getDynmapManager().markerset.createAreaMarker("virtualrealty.plots." + plot.getID(),
                    plot.getOwnedBy() == null ? String.format(MARKER_STRING, plot.getID()) : String.format(MARKER_OWNED_STRING, plot.getID(), ownedBy, dateTimeFormatter.format(localDateTime)), true,
                    plot.getCreatedWorldRaw(), new double[]{plot.getXMin(), plot.getXMax()}, new double[]{plot.getZMin(), plot.getZMax()}, true);
            areaMarkers.add(marker);
        } else {
            marker.setLabel(
                    plot.getOwnedBy() == null ? String.format(MARKER_STRING, plot.getID()) : String.format(MARKER_OWNED_STRING, plot.getID(), ownedBy, dateTimeFormatter.format(localDateTime)), true);
        }
        marker.setFillStyle(opacity, color);
        marker.setLineStyle(2, 0.8, 0x474747);
        marker.setMarkerSet(VirtualRealty.getDynmapManager().markerset);
    }

    public static void removeDynMapMarker(Plot plot) {
        if (VirtualRealty.getDynmapManager() == null || !VirtualRealty.getDynmapManager().isDynmapPresent() || VirtualRealty.getDynmapManager().dapi == null || VirtualRealty.getDynmapManager().markerset == null)
            return;
        AreaMarker marker = VirtualRealty.getDynmapManager().markerset.findAreaMarker("virtualrealty.plots." + plot.getID());
        if (marker == null) return;
        areaMarkers.remove(marker);
        marker.deleteMarker();
    }

}
