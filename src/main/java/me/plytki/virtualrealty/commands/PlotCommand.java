package me.plytki.virtualrealty.commands;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlotCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = ((Player) sender);
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        if (args.length == 1) {
            switch (args[0].toUpperCase()) {
                case "TP": {
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/plot tp §8<§7plotID§8>");
                    break;
                }
                case "GM": {
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/plot gm §8<§7gamemode§8>");
                    break;
                }
                case "LIST": {
                    boolean hasPlot = false;
                    for (Plot plot : PlotManager.plots) {
                        if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(p.getUniqueId()) && plot.getOwnedUntilDate().isAfter(LocalDateTime.now())) {
                            hasPlot = true;
                            break;
                        }
                    }
                    if (!hasPlot) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cYou don't have any plots!");
                        return false;
                    }
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" ");
                    sender.sendMessage("§7§m                                                                                ");
                    sender.sendMessage(" §7|   §a§l§oID§7  |  §a§l§oOwned Until§7 |  §a§l§oSize§7  |  §a§l§oPlot Center§7  |");
                    for (Plot plot : PlotManager.plots) {
                        if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(p.getUniqueId()) && plot.getOwnedUntilDate().isAfter(LocalDateTime.now())) {
                            LocalDateTime localDateTime = plot.getOwnedUntilDate();
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            StringBuilder ownedBy = new StringBuilder();
                            ownedBy.append((plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : "§cAvailable"));
                            boolean isOwned = !ownedBy.toString().equals("§cAvailable");
                            for (int i = ownedBy.length(); i < 16; i++) {
                                ownedBy.append(" ");
                            }
                            StringBuilder size = new StringBuilder(plot.getPlotSize().name());
                            for (int i = size.length(); i < 6; i++) {
                                size.append(" ");
                            }
                            BaseComponent textComponent = new TextComponent(" §f" + plot.getID() + "§8  §f" + (isOwned ? " " : "") + dateTimeFormatter.format(localDateTime) + "§8   §f" + size + "§8   §f" + plot.getCenter().toSimpleString());
                            sender.sendMessage(textComponent.toLegacyText());
                        }
                    }
                    sender.sendMessage("§7§m                                                                                ");
                    break;
                }
                default: {
                    printHelp(sender);
                }
            }
        }
        if (args.length >= 2) {
            switch (args[0].toUpperCase()) {
                case "TP": {
                    if (args.length == 2) {
                        int plotID;
                        try {
                            plotID = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get plot with specified ID!");
                            return false;
                        }
                        if (!(plot.getOwnedBy() != null && plot.getOwnedBy().equals(p.getUniqueId()))) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cIt's not your plot!");
                            return false;
                        }
                        if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                            return false;
                        }
                        Location loc = new Location(plot.getCreatedLocation().getWorld(), plot.getCenter().getBlockX(), plot.getCenter().getBlockY() + 1, plot.getCenter().getBlockZ());
                        loc.setY(loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getY() + 1);
                        p.teleport(loc);
                        sender.sendMessage(VirtualRealty.PREFIX + "§aYou have been teleported to the plot!");
                    }
                    break;
                }
                case "GM": {
                    if (!VirtualRealty.getPluginConfiguration().enablePlotGameMode || VirtualRealty.getPluginConfiguration().forcePlotGameMode) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cGamemode feature is disabled!");
                        return false;
                    }
                    GameMode gameMode;
                    int gameModeID;
                    try {
                        gameMode = GameMode.valueOf(args[1]);
                        gameModeID = gameMode.getValue();
                    } catch (IllegalArgumentException e) {
                        try {
                            gameModeID = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException ex) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cIncorrect gamemode vaule!");
                            return false;
                        }
                    }
                    if (!(gameModeID != 1 && gameModeID != 2)) {
                        gameMode = GameMode.getByValue(Integer.parseInt(args[1]));
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cUse only numbers from 1 to 2!");
                        return false;
                    }

                    Plot plot = PlotManager.getBorderedPlot(p.getLocation());
                    if (plot != null) {
                        if (plot.getOwnedBy() == null || (plot.getOwnedBy() != null && !plot.getOwnedBy().equals(p.getUniqueId()))) {
                            if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                                return false;
                            }
                            sender.sendMessage(VirtualRealty.PREFIX + "§cYou can't switch gamemode here!");
                        } else {
                            if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                            } else {
                                if (plot.getSelectedGameMode().equals(gameMode)) {
                                    if (plot.getSelectedGameMode().equals(GameMode.CREATIVE)) {
                                        sender.sendMessage(VirtualRealty.PREFIX + "§cPlot Creative-Mode is already enabled!");
                                    } else {
                                        sender.sendMessage(VirtualRealty.PREFIX + "§cPlot Creative-Mode is already disabled!");
                                    }
                                    return false;
                                }
                                plot.setSelectedGameMode(gameMode);
                                p.setGameMode(plot.getSelectedGameMode());
                                if (plot.getSelectedGameMode().equals(GameMode.CREATIVE)) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§aPlot Creative-Mode has been enabled!");
                                } else {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§ePlot Creative-Mode has been disabled!");
                                }

                            }
                        }
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cYou can't switch gamemode here!");
                    }
                    break;
                }
                default: {
                    printHelp(sender);
                }
            }
        }
        return false;
    }

    private static void printHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        sender.sendMessage(" §a/plot list §8- §7Shows your plots");
        sender.sendMessage(" §a/plot gm §8- §7Changes gamemode");
    }

}
