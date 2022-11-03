package com.modnmetl.virtualrealty.listeners.protection;

import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BorderProtectionListener extends VirtualListener {

    public BorderProtectionListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBorderBreak(BlockBreakEvent e) {
        Plot plot = PlotManager.getInstance().getBorderedPlot(e.getBlock().getLocation());
        if (plot == null) return;
        if (e.getPlayer().isOp()) return;
        if (!plot.getBorderBlocks().contains(e.getBlock())) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
    }

    @EventHandler
    public void onBorderPlace(BlockPlaceEvent e) {
        Plot plot = PlotManager.getInstance().getBorderedPlot(e.getBlock().getLocation());
        if (plot == null) return;
        if (e.getPlayer().isOp()) return;
        if (!plot.getBorderBlocks().contains(e.getBlock())) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
    }

}
