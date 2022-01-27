package com.modnmetl.virtualrealty.commands;

import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.Plot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
                case "ADD": {
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/plot add §8<§7player§8> §8<§7plot§8>");
                    break;
                }
                case "KICK": {
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/plot kick §8<§7player§8> §8<§7plot§8>");
                    break;
                }
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
                case "INFO": {
                    Plot plot = PlotManager.getPlot(p.getLocation());
                    if (plot == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notStandingOnPlot);
                        return false;
                    }
                    printInfo(sender, plot);
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
                    boolean isMember = false;
                    for (Plot plot : PlotManager.plots) {
                        if (plot.getMembers().contains(p.getUniqueId())) {
                            isMember = true;
                            break;
                        }
                    }
                    if (!hasPlot && !isMember) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cYou don't have any plots!");
                        return false;
                    }
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" ");
                    if (hasPlot) {
                        sender.sendMessage("§7§m                                                                                ");
                        sender.sendMessage("§7|  §a§l§oID§7  |  §a§l§oOwned Until§7 |  §a§l§oSize§7  |  §a§l§oPlot Center§7  |");
                        for (Plot plot : PlotManager.plots) {
                            if (plot.getPlotOwner() != null && plot.getPlotOwner().getUniqueId().equals(p.getUniqueId())) {
                                LocalDateTime localDateTime = plot.getOwnedUntilDate();
                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                StringBuilder ownedBy = new StringBuilder();
                                ownedBy.append((plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : VirtualRealty.getMessages().available));
                                boolean isOwned = !ownedBy.toString().equals(VirtualRealty.getMessages().available);
                                for (int i = ownedBy.length(); i < 16; i++) {
                                    ownedBy.append(" ");
                                }
                                StringBuilder size = new StringBuilder(plot.getPlotSize().name());
                                for (int i = size.length(); i < 6; i++) {
                                    size.append(" ");
                                }
                                BaseComponent textComponent = new TextComponent("§f" + plot.getID() + "§8  §f" + (isOwned ? " " : "") + dateTimeFormatter.format(localDateTime) + "§8    §f" + size + "§8   §f" + plot.getCenter().toSimpleString());
                                sender.sendMessage(textComponent.toLegacyText());
                            }
                        }
                        sender.sendMessage("§7§m                                                                                ");
                    }
                    if (isMember) {
                        sender.sendMessage(" ");
                        sender.sendMessage("§7                            §fMember of §8§l↴");
                        sender.sendMessage(" ");
                        sender.sendMessage("§7§m                                                                                ");
                        sender.sendMessage("§7|  §a§l§oID§7  |  §a§l§oOwned By§7 |  §a§l§oSize§7  |  §a§l§oPlot Center§7  |");
                        for (Plot plot : PlotManager.plots) {
                            if (plot.getPlotOwner() != null && !plot.getPlotOwner().getUniqueId().equals(p.getUniqueId()) && plot.getMembers().contains(p.getUniqueId())) {
                                StringBuilder ownedBy = new StringBuilder();
                                ownedBy.append((plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : VirtualRealty.getMessages().available));
                                boolean isOwned = !ownedBy.toString().equals(VirtualRealty.getMessages().available);
                                for (int i = ownedBy.length(); i < 16; i++) {
                                    ownedBy.append(" ");
                                }
                                StringBuilder size = new StringBuilder(plot.getPlotSize().name());
                                for (int i = size.length(); i < 6; i++) {
                                    size.append(" ");
                                }
                                BaseComponent textComponent = new TextComponent("§f" + plot.getID() + "§8  §f" + (isOwned ? " " : "") + ownedBy + "§8 §f" + size + "§8   §f" + plot.getCenter().toSimpleString());
                                sender.sendMessage(textComponent.toLegacyText());
                            }
                        }
                        sender.sendMessage("§7§m                                                                                ");
                    }
                    break;
                }
                default: {
                    printHelp(sender);
                }
            }
        }
        if (args.length >= 2) {
            switch (args[0].toUpperCase()) {
                case "ADD": {
                    if (args.length == 3) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        if (offlinePlayer.getName() == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                            return false;
                        }
                        int plotID;
                        try {
                            plotID = Integer.parseInt(args[2]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                            return false;
                        }
                        if (!(plot.getOwnedBy() != null && plot.getOwnedBy().equals(p.getUniqueId()))) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notYourPlot);
                            return false;
                        }
                        if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                            return false;
                        }
                        if (plot.getOwnedBy().equals(offlinePlayer.getUniqueId())) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantAddYourself);
                            return false;
                        }
                        if (plot.getMembers().contains(offlinePlayer.getUniqueId())) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().alreadyInMembers);
                            return false;
                        }
                        plot.addMember(offlinePlayer.getUniqueId());
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerAdd.replaceAll("%player%", offlinePlayer.getName()));
                        return false;
                    }
                    break;
                }
                case "KICK": {
                    if (args.length == 3) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        if (offlinePlayer.getName() == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                            return false;
                        }
                        int plotID;
                        try {
                            plotID = Integer.parseInt(args[2]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                            return false;
                        }
                        if (!(plot.getOwnedBy() != null && plot.getOwnedBy().equals(p.getUniqueId()))) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notYourPlot);
                            return false;
                        }
                        if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                            return false;
                        }
                        if (plot.getOwnedBy().equals(offlinePlayer.getUniqueId())) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantKickYourself);
                            return false;
                        }
                        if (!plot.getMembers().contains(offlinePlayer.getUniqueId())) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notInMembers);
                            return false;
                        }
                        plot.removeMember(offlinePlayer.getUniqueId());
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerKick.replaceAll("%player%", offlinePlayer.getName()));
                        return false;
                    }
                    break;
                }
                case "TP": {
                    if (args.length == 2) {
                        int plotID;
                        try {
                            plotID = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                            return false;
                        }
                        if (!plot.hasPlotMembership(p)) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notYourPlot);
                            return false;
                        }
                        if (plot.isOwnershipExpired()) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                            return false;
                        }
                        Location loc = new Location(plot.getCreatedLocation().getWorld(), plot.getCenter().getBlockX(), plot.getCenter().getBlockY() + 1, plot.getCenter().getBlockZ());
                        loc.setY(loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getY() + 1);
                        p.teleport(loc);
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().teleportedToPlot);
                    }
                    break;
                }
                case "GM": {
                    if (VirtualRealty.getPluginConfiguration().lockPlotGameMode) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().gamemodeFeatureDisabled);
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
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().incorrectGamemode);
                            return false;
                        }
                    }
                    GameMode defaultGamemode = VirtualRealty.getInstance().getServer().getDefaultGameMode();
                    GameMode configGamemode = VirtualRealty.getPluginConfiguration().getGameMode();
                    if (!(gameModeID != configGamemode.getValue() && gameModeID != defaultGamemode.getValue())) {
                        gameMode = GameMode.getByValue(Integer.parseInt(args[1]));
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().gamemodeDisabled);
                        return false;
                    }
                    Plot plot = PlotManager.getBorderedPlot(p.getLocation());
                    if (plot != null) {
                        if (!plot.hasPlotMembership(p)) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantSwitchGamemode);
                        } else {
                            if (plot.isOwnershipExpired()) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                            } else {
                                if (p.getGameMode().equals(gameMode)) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().gamemodeAlreadySelected);
                                    return false;
                                }
                                if (plot.getPlotOwner() != null && plot.getPlotOwner().getUniqueId().equals(p.getUniqueId())) {
                                    plot.setSelectedGameMode(gameMode);
                                }
                                p.setGameMode(gameMode);
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().gamemodeSwitched);
                            }
                        }
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantSwitchGamemode);
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
        sender.sendMessage(" §a/plot add §8- §7Adds a member");
        sender.sendMessage(" §a/plot kick §8- §7Kicks a member");
        sender.sendMessage(" §a/plot list §8- §7Shows your plots");
        sender.sendMessage(" §a/plot info §8- §7Shows plot info");
        sender.sendMessage(" §a/plot gm §8- §7Changes gamemode");
        sender.sendMessage(" §a/plot tp §8- §7Teleports to the plot");
    }

    private static void printInfo(CommandSender sender, Plot plot) {
        LocalDateTime localDateTime = plot.getOwnedUntilDate();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        String assignedBy = VirtualRealty.getMessages().notAssigned;
//        if (plot.getAssignedBy() != null) {
//            switch (plot.getAssignedBy().toUpperCase()) {
//                case "CONSOLE": {
//                    assignedBy = VirtualRealty.getMessages().assignedByConsole;
//                    break;
//                }
//                case "SHOP_PURCHASE": {
//                    assignedBy = VirtualRealty.getMessages().assignedByShopPurchase;
//                    break;
//                }
//                default: {
//                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(plot.getAssignedBy()));
//                    assignedBy = (offlinePlayer.isOnline() ? "§a" : "§c") + offlinePlayer.getName();
//                }
//            }
//        }
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        sender.sendMessage(" §7Plot ID §8§l‣ §f" + plot.getID());
        sender.sendMessage(" §7Owned By §8§l‣ §a" + (plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : "§cAvailable"));
        if (plot.getMembers().size() != 0) {
            sender.sendMessage(" §7Members §8§l↴");
            for (OfflinePlayer offlinePlayer : plot.getMembersPlayer()) {
                sender.sendMessage(" §8§l⁍ §" + (offlinePlayer.isOnline() ? "a" : "c") + offlinePlayer.getName());
            }
        }
        //sender.sendMessage(" §7Assigned By §8§l‣ §a" + assignedBy);
        sender.sendMessage(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime));
        sender.sendMessage(" §7Size §8§l‣ §f" + plot.getPlotSize());
        sender.sendMessage(" §7Length §8§l‣ §f" + plot.getLength());
        sender.sendMessage(" §7Width §8§l‣ §f" + plot.getWidth());
        sender.sendMessage(" §7Height §8§l‣ §f" + plot.getHeight());
        //sender.sendMessage(" §7Floor Material §8§l‣ §f" + plot.getFloorMaterial().name());
        //sender.sendMessage(" §7Border Material §8§l‣ §f" + plot.getBorderMaterial().name());
        //sender.sendMessage(" §7Pos 1 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getBottomLeftCorner().toString());
        //sender.sendMessage(" §7Pos 2 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getTopRightCorner().toString());
        //sender.sendMessage(" §7Created Direction §8§l‣ §f" + plot.getCreatedDirection().name());
    }

}