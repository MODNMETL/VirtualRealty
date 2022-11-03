package com.modnmetl.virtualrealty.listeners;

import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.PlotMember;
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
import java.util.Map;
import java.util.Objects;

public class PlotEntranceListener extends VirtualListener {

    public HashMap<Player, Map.Entry<Plot, Boolean>> enteredPlot = new HashMap<>();

    public PlotEntranceListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlotMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Location to = e.getTo();
        if (to == null) return;
        Plot plot = PlotManager.getInstance().getPlot(to);
        if (plot != null) {
            OfflinePlayer offlinePlayer;
            String enterPlotString = VirtualRealty.getMessages().enteredAvailablePlot;
            if (plot.getOwnedBy() != null) {
                offlinePlayer = Bukkit.getOfflinePlayer(plot.getOwnedBy());
                enterPlotString = VirtualRealty.getMessages().enteredOwnedPlot.replaceAll("%owner%", Objects.requireNonNull(offlinePlayer.getName())).replaceAll("%plot_id%", plot.getID() + "");
            }
            if (!enteredPlot.containsKey(player)) {
                enteredPlot.put(player, new AbstractMap.SimpleEntry<>(plot, true));
                Plot newPlot = enteredPlot.get(player).getKey();
                if (VirtualRealty.getPluginConfiguration().enablePlotGamemode) {
                    if (newPlot.hasMembershipAccess(player.getUniqueId())) {
                        if (newPlot.getOwnedBy() != null && newPlot.getOwnedBy().equals(player.getUniqueId())) {
                            player.setGameMode(newPlot.getSelectedGameMode());
                        } else if (newPlot.getMember(player.getUniqueId()) != null) {
                            PlotMember plotMember = newPlot.getMember(player.getUniqueId());
                            player.setGameMode(plotMember.getSelectedGameMode());
                        }
                    }
                }
                if (!VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8")) {
                    if (VirtualRealty.getPluginConfiguration().plotSound) {
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_OPEN, 0.4f, 0.8f);
                    }
                    if (enteredPlot.get(player).getKey().getPlotSize() == PlotSize.AREA) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(VirtualRealty.getMessages().enteredProtectedArea));
                    } else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(enterPlotString));
                    }
                }
            } else {
                if (!enteredPlot.get(player).getValue()) {
                    enteredPlot.replace(player, new AbstractMap.SimpleEntry<>(plot, true));
                    if (VirtualRealty.getPluginConfiguration().enablePlotGamemode) {
                        player.setGameMode(VirtualRealty.getPluginConfiguration().getDefaultPlotGamemode());
                    }
                }
            }
        } else {
            if (!enteredPlot.containsKey(player)) return;
            if (enteredPlot.get(player).getValue()) {
                OfflinePlayer offlinePlayer;
                String leavePlotString = VirtualRealty.getMessages().leftAvailablePlot;
                if (enteredPlot.get(player).getKey().getOwnedBy() != null) {
                    offlinePlayer = Bukkit.getOfflinePlayer(enteredPlot.get(player).getKey().getOwnedBy());
                    leavePlotString = VirtualRealty.getMessages().leftOwnedPlot.replaceAll("%owner%", Objects.requireNonNull(offlinePlayer.getName())).replaceAll("%plot_id%", enteredPlot.get(player).getKey().getID() + "");
                    if (VirtualRealty.getPluginConfiguration().enablePlotGamemode) {
                        if (enteredPlot.get(player).getKey().hasMembershipAccess(player.getUniqueId())) {
                            player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                        }
                    }
                }
                if (!VirtualRealty.getInstance().getServer().getBukkitVersion().startsWith("1.8")) {
                    if (VirtualRealty.getPluginConfiguration().plotSound) {
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 0.3f, 1f);
                    }
                    if (enteredPlot.get(player).getKey().getPlotSize() == PlotSize.AREA) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(VirtualRealty.getMessages().leftProtectedArea));
                    } else {
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
