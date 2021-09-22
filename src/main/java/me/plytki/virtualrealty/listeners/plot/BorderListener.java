package me.plytki.virtualrealty.listeners.plot;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.Permission;
import me.plytki.virtualrealty.listeners.VirtualListener;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
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
