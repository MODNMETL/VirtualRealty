package me.plytki.virtualrealty.listeners.plot;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.listeners.VirtualListener;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.AbstractMap;
import java.util.HashMap;

public class PlotListener extends VirtualListener {

    public HashMap<Player, AbstractMap.SimpleEntry<Plot, Boolean>> enteredPlot = new HashMap<>();

    public PlotListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlotMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Location to = e.getTo();
        Plot plot = PlotManager.getPlot(to);
        if (plot != null) {
            OfflinePlayer offlinePlayer;
            String enterPlotString = VirtualRealty.getMessages().enteredAvailablePlot;
            if (plot.getOwnedBy() != null) {
                offlinePlayer = Bukkit.getOfflinePlayer(plot.getOwnedBy());
                enterPlotString = VirtualRealty.getMessages().enteredOwnedPlot.replaceAll("%owner%", offlinePlayer.getName()).replaceAll("%plot_id%", plot.getID() + "");
            }
            if (!enteredPlot.containsKey(player)) {
                enteredPlot.put(player, new AbstractMap.SimpleEntry<>(plot, true));
                Plot newPlot = enteredPlot.get(player).getKey();
                if (VirtualRealty.getPluginConfiguration().enablePlotGameMode) {
                    if (newPlot.getOwnedBy() != null && newPlot.getOwnedBy().equals(player.getUniqueId())) {
                        if (newPlot.getSelectedGameMode() != VirtualRealty.getInstance().getServer().getDefaultGameMode() || newPlot.getSelectedGameMode() != VirtualRealty.getPluginConfiguration().getGameMode()) {
                            newPlot.setSelectedGameMode(VirtualRealty.getPluginConfiguration().getGameMode());
                        }
                        player.setGameMode(newPlot.getSelectedGameMode());
                    } else if (newPlot.getMembers().contains(player.getUniqueId())) {
                        player.setGameMode(VirtualRealty.getPluginConfiguration().getGameMode());
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
                        player.setGameMode(VirtualRealty.getPluginConfiguration().getGameMode());
                    }
                }
            }
        } else {
            if (enteredPlot.containsKey(player)) {
                if (enteredPlot.get(player).getValue()) {
                    OfflinePlayer offlinePlayer;
                    String leavePlotString = VirtualRealty.getMessages().leftAvailablePlot;
                    if (enteredPlot.get(player).getKey().getOwnedBy() != null) {
                        offlinePlayer = Bukkit.getOfflinePlayer(enteredPlot.get(player).getKey().getOwnedBy());
                        leavePlotString = VirtualRealty.getMessages().leftOwnedPlot.replaceAll("%owner%", offlinePlayer.getName()).replaceAll("%plot_id%", enteredPlot.get(player).getKey().getID() + "");
                        if (VirtualRealty.getPluginConfiguration().enablePlotGameMode) {
                            if (enteredPlot.get(player).getKey().hasPlotMembership(player)) {
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
