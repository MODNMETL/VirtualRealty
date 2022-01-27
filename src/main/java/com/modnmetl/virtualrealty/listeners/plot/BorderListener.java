package com.modnmetl.virtualrealty.listeners.plot;

import com.modnmetl.virtualrealty.enums.Permission;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BorderListener extends VirtualListener {

    public BorderListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBorderBreak(BlockBreakEvent e) {
        Plot plot = PlotManager.getBorderedPlot(e.getBlock().getLocation());
        if (plot != null) {
            if (!e.getPlayer().hasPermission(Permission.BORDER_BUILD.getPermission())) {
                if (plot.getBorderBlocks().contains(e.getBlock())) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
                }
            }
        }
    }

    @EventHandler
    public void onBorderPlace(BlockPlaceEvent e) {
        Plot plot = PlotManager.getBorderedPlot(e.getBlock().getLocation());
        if (plot != null) {
            if (!e.getPlayer().hasPermission(Permission.BORDER_BUILD.getPermission())) {
                if (plot.getBorderBlocks().contains(e.getBlock())) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
                }
            }
        }
    }

}
