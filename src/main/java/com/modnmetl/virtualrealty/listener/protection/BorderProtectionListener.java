package com.modnmetl.virtualrealty.listener.protection;

import com.modnmetl.virtualrealty.listener.VirtualListener;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.plot.Plot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BorderProtectionListener extends VirtualListener {

    public BorderProtectionListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBorderBreak(BlockBreakEvent e) {
        Plot plot = PlotManager.getInstance().getPlot(e.getBlock().getLocation(), true);
        if (plot == null) return;
        if (plot.isBorderLess()) return;
        if (e.getPlayer().isOp()) return;
        if (!plot.getBorderBlocks().contains(e.getBlock())) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
    }

    @EventHandler
    public void onBorderPlace(BlockPlaceEvent e) {
        Plot plot = PlotManager.getInstance().getPlot(e.getBlock().getLocation(), true);
        if (plot == null) return;
        if (plot.isBorderLess()) return;
        if (e.getPlayer().isOp()) return;
        if (!plot.getBorderBlocks().contains(e.getBlock())) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
    }

}
