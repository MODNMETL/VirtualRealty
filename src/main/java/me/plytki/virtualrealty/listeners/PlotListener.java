package me.plytki.virtualrealty.listeners;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
            String enterPlotString = "§2You have entered available plot!";
            if (plot.getOwnedBy() != null) {
                offlinePlayer = Bukkit.getOfflinePlayer(plot.getOwnedBy());
                enterPlotString = "§7You have entered §2" + offlinePlayer.getName() + "'s §7plot!";
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
                if (!VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8")) {
                    if (VirtualRealty.getPluginConfiguration().plotSound) {
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_OPEN, 0.4f, 0.8f);
                    }
                }
                if (!(VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8") || VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8"))) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(enterPlotString));
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
                        leavePlotString = "§7You have left §2" + offlinePlayer.getName() + "'s §7plot!";
                        if (VirtualRealty.getPluginConfiguration().enablePlotGameMode) {
                            if (enteredPlot.get(player).getKey().getOwnedBy() != null && enteredPlot.get(player).getKey().getOwnedBy().equals(player.getUniqueId())) {
                                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                            }
                        }
                    }
                    if (!VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8")) {
                        if (VirtualRealty.getPluginConfiguration().plotSound) {
                            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 0.3f, 1f);
                        }
                        if (!(VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8") || VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8"))) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(leavePlotString));
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
