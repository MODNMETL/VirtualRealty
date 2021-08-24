package me.plytki.virtualrealty.listeners;

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
            //Sound enterSound = Sound.WATER;
            if (plot.getOwnedBy() != null) {
                offlinePlayer = Bukkit.getOfflinePlayer(plot.getOwnedBy());
                enterPlotString = "§eYou have entered §f" + offlinePlayer.getName() + "'s §eplot!";
            }
            if (!enteredPlot.containsKey(player)) {
                enteredPlot.put(player, new AbstractMap.SimpleEntry<>(plot, true));
//                PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(enterPlotString), (byte)2);
//                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(enterPlotString));
                //player.playSound(to, enterSound, 1, 1);
                if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(player.getUniqueId())) {
                    player.setGameMode(plot.getSelectedGameMode());
                }
            } else {
                if (!enteredPlot.get(player).getValue()) {
//                    PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(enterPlotString), (byte)2);
//                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(enterPlotString));
                    //player.playSound(to, enterSound, 1, 1);
                    enteredPlot.replace(player, new AbstractMap.SimpleEntry<>(plot, true));
                    if (plot.getOwnedBy().equals(player.getUniqueId())) {
                        player.setGameMode(plot.getSelectedGameMode());
                    }
                }
            }
        } else {
            if (enteredPlot.containsKey(player)) {
                if (enteredPlot.get(player).getValue()) {
                    OfflinePlayer offlinePlayer;
                    //Sound leaveSound = Sound.WATER;
                    String leavePlotString = "§cYou have left available plot!";
                    if (enteredPlot.get(player).getKey().getOwnedBy() != null) {
                        offlinePlayer = Bukkit.getOfflinePlayer(enteredPlot.get(player).getKey().getOwnedBy());
                        leavePlotString = "§cYou have left §f" + offlinePlayer.getName() + "'s §cplot!";
                        if (enteredPlot.get(player).getKey().getOwnedBy() != null && enteredPlot.get(player).getKey().getOwnedBy().equals(player.getUniqueId())) {
                            player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                        }
                    }
//                    PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(leavePlotString), (byte)2);
//                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(leavePlotString));
                    //player.playSound(to, leaveSound, 1, 1);
                    enteredPlot.remove(player);
                    return;
                }
                enteredPlot.replace(player, new AbstractMap.SimpleEntry<>(enteredPlot.get(player).getKey(), false));
            }
        }
    }

}
