package com.modnmetl.virtualrealty.managers;

import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.objects.data.PlotMember;
import com.modnmetl.virtualrealty.objects.math.BlockVector2;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.math.BlockVector3;
import com.modnmetl.virtualrealty.sql.Database;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class PlotManager {

    private final VirtualRealty plugin;

    private final Set<Plot> plots;

    private final List<PlotMember> plotMembers;

    public PlotManager(VirtualRealty plugin) {
        this.plugin = plugin;
        this.plots = new LinkedHashSet<>();
        this.plotMembers = new ArrayList<>();
    }


    public void loadPlots() {
        try (Connection conn = Database.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "`"); ResultSet rs = ps.executeQuery()) {
            plots.clear();
            while (rs.next()) {
                plots.add(new Plot(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMembers() {
        try (Connection conn = Database.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "`"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                plotMembers.add(new PlotMember(rs));
            }
            checkDupes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkDupes() {
        HashMap<UUID, Integer> dupes = new HashMap<>();
        for (PlotMember plotMember : plotMembers) {
            if (dupes.containsKey(plotMember.getUuid()) && dupes.get(plotMember.getUuid()) == plotMember.getPlot().getID()) {
                VirtualRealty.debug("Found duped plot member: " + plotMember.getUuid() + " | " + plotMember.getPlot().getID() + " - Removing..");
                plotMember.getPlot().removeMember(plotMember);
            } else {
                dupes.put(plotMember.getUuid(), plotMember.getPlot().getID());
            }
        }
    }

    public Plot createPlot(Location creationLocation, PlotSize plotSize, int length, int height, int width, boolean natural) {
        Plot plot = new Plot(creationLocation, Material.matchMaterial(VirtualRealty.legacyVersion ? "GRASS" : "GRASS_BLOCK"), Material.matchMaterial(VirtualRealty.legacyVersion ? "STEP" : "STONE_BRICK_SLAB"), plotSize, length, width, height, natural);
        plots.add(plot);
        long time = System.currentTimeMillis();
        plot.insert();
        VirtualRealty.debug("Plot database insertion time: " + (System.currentTimeMillis() - time) + " ms");
        return plot;
    }

    public Plot getPlot(int ID) {
        for (Plot plot : plots) {
            if (plot.getID() == ID) {
                return plot;
            }
        }
        return null;
    }

    public List<Plot> getPlots(String world) {
        List<Plot> newPlots = new LinkedList<>();
        for (Plot plot : plots) {
            if (plot.getCreatedWorldString().equals(world)) newPlots.add(plot);
        }
        return newPlots;
    }

    public HashMap<Integer, Plot> getPlots(UUID owner) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(owner)) {
                plotHashMap.put(plot.getID(), plot);
            }
        }
        return plotHashMap;
    }

    public HashMap<Integer, Plot> getAccessPlots(UUID player) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if (plot.getMember(player) != null || (plot.getOwnedBy() != null && plot.getPlotOwner().getUniqueId() == player)) {
                plotHashMap.put(plot.getID(), plot);
            }
        }
        return plotHashMap;
    }

    public int getPlotMinID() {
        return plots.isEmpty() ? 0 : plots.stream().findFirst().get().getID();
    }

    public int getPlotMaxID() {
        Plot[] plotArray = plots.toArray(new Plot[0]);
        Plot lastPlot = plotArray[plotArray.length - 1];
        return lastPlot.getID();
    }

    public List<Plot> getPlayerPlots(UUID owner) {
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

    public Plot getPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
            if (region.isIn(newVector, plot.getCreatedWorld())) {
                return plot;
            }
        }
        return null;
    }

    public void removePlotFromList(Plot plot) {
        plots.remove(plot);
    }

    public boolean isLocationInPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
        return region.isIn(newVector, location.getWorld());
    }

    public Plot getBorderedPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
            if (region.isIn(newVector, plot.getCreatedWorld())) {
                return plot;
            }
        }
        return null;
    }

    public boolean isLocationInBorderedPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
        return region.isIn(newVector, plot.getCreatedWorld());
    }

    public static PlotManager getInstance() {
        return VirtualRealty.getInstance().getPlotManager();
    }

}