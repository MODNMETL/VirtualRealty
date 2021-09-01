package me.plytki.virtualrealty.listeners;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class WorldListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlock().getLocation());
        if (!VirtualRealty.getPluginConfiguration().allowOutPlotBuild) {
            if (!player.hasPermission("virtualrealty.build")) {
                if (plot == null) {
                    e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                    player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                } else {
                    if (plot.getOwnedBy() != null && !plot.getOwnedBy().equals(player.getUniqueId())) {
                        e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                    } else {
                        if (plot.getOwnedBy() == null) {
                            e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                            player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                        }
                    }
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
            if (!player.hasPermission("virtualrealty.build")) {
                if (plot == null) {
                    e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                    player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                } else {
                    if (plot.getOwnedBy() != null && !plot.getOwnedBy().equals(player.getUniqueId())) {
                        e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                    } else {
                        if (plot.getOwnedBy() == null) {
                            e.setCancelled(!VirtualRealty.getPluginConfiguration().allowOutPlotBuild);
                            player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                        }
                    }
                }
            }
        }
    }

}
