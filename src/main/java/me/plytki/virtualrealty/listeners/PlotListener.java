package me.plytki.virtualrealty.listeners;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.AbstractMap;
import java.util.HashMap;

public class PlotListener implements Listener {

    public static HashMap<Player, AbstractMap.SimpleEntry<Plot, Boolean>> enteredPlot = new HashMap<>();

    @EventHandler
    public void onPlotMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location to = e.getTo();
        Plot plot = PlotManager.getPlot(to);
        if (plot != null) {
            OfflinePlayer offlinePlayer;
            String enterPlotString = "§aYou have entered available plot!";
            if (plot.getOwnedBy() != null) {
                offlinePlayer = Bukkit.getOfflinePlayer(plot.getOwnedBy());
                enterPlotString = "§eYou have entered §f" + offlinePlayer.getName() + "'s §eplot!";
            }
            if (!enteredPlot.containsKey(player)) {
                enteredPlot.put(player, new AbstractMap.SimpleEntry<>(plot, true));
                if (VirtualRealty.getPluginConfiguration().enablePlotGameMode) {
                    if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(player.getUniqueId())) {
                        if (VirtualRealty.getPluginConfiguration().forcePlotGameMode) {
                            player.setGameMode(VirtualRealty.getPluginConfiguration().getGameMode());
                        } else {
                            player.setGameMode(plot.getSelectedGameMode());
                        }
                    }
                }
            } else {
                if (!enteredPlot.get(player).getValue()) {
                    enteredPlot.replace(player, new AbstractMap.SimpleEntry<>(plot, true));
                    if (VirtualRealty.getPluginConfiguration().enablePlotGameMode) {
                        if (VirtualRealty.getPluginConfiguration().forcePlotGameMode) {
                            player.setGameMode(VirtualRealty.getPluginConfiguration().getGameMode());
                        } else {
                            player.setGameMode(plot.getSelectedGameMode());
                        }
                    }
                }
            }
        } else {
            if (enteredPlot.containsKey(player)) {
                if (enteredPlot.get(player).getValue()) {
                    OfflinePlayer offlinePlayer;
                    String leavePlotString = "§cYou have left available plot!";
                    if (enteredPlot.get(player).getKey().getOwnedBy() != null) {
                        offlinePlayer = Bukkit.getOfflinePlayer(enteredPlot.get(player).getKey().getOwnedBy());
                        leavePlotString = "§cYou have left §f" + offlinePlayer.getName() + "'s §cplot!";
                        if (VirtualRealty.getPluginConfiguration().enablePlotGameMode) {
                            if (enteredPlot.get(player).getKey().getOwnedBy() != null && enteredPlot.get(player).getKey().getOwnedBy().equals(player.getUniqueId())) {
                                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                            }
                        }
                    }
                    enteredPlot.remove(player);
                    return;
                }
                enteredPlot.replace(player, new AbstractMap.SimpleEntry<>(enteredPlot.get(player).getKey(), false));
            }
        }
    }

}
