package com.modnmetl.virtualrealty.listeners.world;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.Permission;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class WorldListener extends VirtualListener {

    public WorldListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlock().getLocation());
        if (!VirtualRealty.getPluginConfiguration().allowOutPlotBuild) {
            if (!player.hasPermission(Permission.WORLD_BUILD.getPermission())) {
                if (plot == null) {
                    e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlock().getLocation());
        if (!VirtualRealty.getPluginConfiguration().allowOutPlotBuild) {
            if (!player.hasPermission(Permission.WORLD_BUILD.getPermission())) {
                if (plot == null) {
                    e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
                }
            }
        }
    }

}