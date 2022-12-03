package com.modnmetl.virtualrealty.manager;

import com.modnmetl.virtualrealty.model.plot.PlotSize;
import com.modnmetl.virtualrealty.model.plot.PlotMember;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.model.region.Cuboid;
import com.modnmetl.virtualrealty.model.plot.Plot;
import com.modnmetl.virtualrealty.model.math.BlockVector3;
import com.modnmetl.virtualrealty.sql.Database;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

@Data
public final class PlotManager {

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
            while (rs.next())
                plots.add(new Plot(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMembers() {
        try (Connection conn = Database.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "`"); ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                plotMembers.add(new PlotMember(rs));
            checkDupes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkDupes() {
        HashMap<UUID, Integer> dupes = new HashMap<>();
        for (PlotMember plotMember : plotMembers) {
            int plotId;
            try {
                plotId = plotMember.getPlot().getID();
            } catch (Exception e) {
                VirtualRealty.getInstance().getLogger().log(Level.WARNING, "Removal of plot member for lack of allocated plot | UUID: " + plotMember.getUuid());
                plotMember.delete();
                continue;
            }
            if (dupes.containsKey(plotMember.getUuid()) && dupes.get(plotMember.getUuid()) == plotId) {
                VirtualRealty.debug("Found duped plot member: " + plotMember.getUuid() + " | " + plotId + " - Removing from database..");
                plotMember.getPlot().removeMember(plotMember);
            } else {
                dupes.put(plotMember.getUuid(), plotMember.getPlot().getID());
            }
        }
    }

    public Plot createPlot(Location creationLocation, PlotSize plotSize, boolean natural) {
        return createPlot(
                creationLocation,
                plotSize,
                plotSize.getLength(),
                plotSize.getHeight(),
                plotSize.getWidth(),
                plotSize.getFloorMaterial(),
                plotSize.getFloorMaterial(),
                natural
        );
    }

    public Plot createPlot(Location creationLocation, PlotSize plotSize, boolean natural, Material floorMaterial, Material borderMaterial) {
        return createPlot(
                creationLocation,
                plotSize,
                plotSize.getLength(),
                plotSize.getHeight(),
                plotSize.getWidth(),
                floorMaterial,
                borderMaterial,
                natural
        );
    }

    public Plot createCustomPlot(Location creationLocation, int length, int height, int width, boolean natural) {
        return createPlot(
                creationLocation,
                PlotSize.CUSTOM,
                length,
                height,
                width,
                PlotSize.CUSTOM.getFloorMaterial(),
                PlotSize.CUSTOM.getBorderMaterial(),
                natural
        );
    }

    public Plot createCustomPlot(Location creationLocation, int length, int height, int width, boolean natural, Material floorMaterial, Material borderMaterial) {
        return createPlot(
                creationLocation,
                PlotSize.CUSTOM,
                length,
                height,
                width,
                floorMaterial,
                borderMaterial,
                natural
        );
    }

    public Plot createArea(Location creationLocation, int length, int height, int width) {
        return createPlot(
                creationLocation,
                PlotSize.AREA,
                length,
                height,
                width,
                Material.AIR,
                Material.AIR,
                true
        );
    }

    private Plot createPlot(Location creationLocation, PlotSize plotSize, int length, int height, int width, Material floorMaterial, Material borderMaterial, boolean natural) {
        Plot plot = new Plot(creationLocation, floorMaterial, borderMaterial, plotSize, length, width, height, natural);
        plots.add(plot);
        long time = System.currentTimeMillis();
        plot.insert();
        VirtualRealty.debug("Plot database insertion time: " + (System.currentTimeMillis() - time) + " ms");
        return plot;
    }

    public Plot getPlot(int ID) {
        for (Plot plot : plots) {
            if (plot.getID() == ID)
                return plot;
        }
        return null;
    }

    public List<Plot> getPlots(String world) {
        List<Plot> newPlots = new LinkedList<>();
        for (Plot plot : plots)
            if (plot.getCreatedWorldString().equals(world)) newPlots.add(plot);
        return newPlots;
    }

    public HashMap<Integer, Plot> getPlots(UUID owner) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(owner))
                plotHashMap.put(plot.getID(), plot);
        }
        return plotHashMap;
    }

    public HashMap<Integer, Plot> getAccessPlots(UUID player) {
        HashMap<Integer, Plot> plotHashMap = new HashMap<>();
        for (Plot plot : plots) {
            if (plot.getMember(player) != null || (plot.getOwnedBy() != null && plot.getPlotOwner().getUniqueId() == player))
                plotHashMap.put(plot.getID(), plot);
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
            if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(owner))
                playerPlots.add(plot);
        }
        return playerPlots;
    }

    public void removePlotFromCollection(Plot plot) {
        plots.remove(plot);
    }

    public boolean isLocationInPlot(Location location, Plot plot) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
        return region.isIn(newVector, location.getWorld());
    }

//    public Plot getPlot(Location location) {
//        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
//        for (Plot plot : plots) {
//            Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
//            if (region.isIn(newVector, plot.getCreatedWorld())) {
//                return plot;
//            }
//        }
//        return null;
//    }
//

    public Plot getPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            if (plot.isBorderLess()) {
                return getBorderedPlot(location);
            } else {
                Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
                if (region.isIn(newVector, plot.getCreatedWorld()))
                    return plot;
            }
        }
        return null;
    }

    public Plot getPlot(Location location, boolean withBorder) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            if (withBorder) {
                return getBorderedPlot(location);
            } else {
                Cuboid region = new Cuboid(plot.getBottomLeftCorner(), plot.getTopRightCorner(), location.getWorld());
                if (region.isIn(newVector, plot.getCreatedWorld()))
                    return plot;
            }
        }
        return null;
    }

    private Plot getBorderedPlot(Location location) {
        BlockVector3 newVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (Plot plot : plots) {
            Cuboid region = new Cuboid(plot.getBorderBottomLeftCorner(), plot.getBorderTopRightCorner(), location.getWorld());
            if (region.isIn(newVector, plot.getCreatedWorld()))
                return plot;
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