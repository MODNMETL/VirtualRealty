package me.plytki.virtualrealty.commands;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.Direction;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Cuboid;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.objects.math.BlockVector3;
import me.plytki.virtualrealty.utils.Permissions;
import me.plytki.virtualrealty.utils.PlotUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class VirtualRealtyCommand implements CommandExecutor {

    private static final Permission commandPermission = new Permission("virtualrealty.vrplot");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Permission tempPermission = new Permission(commandPermission.getName());
        if (!Permissions.hasPermission(sender, tempPermission.getName())) return false;
        Player p = null;
        Location location = null;
        if(sender instanceof Player) {
            p = (Player)sender;
            location = p.getLocation();
            location.add(0, -1, 0);
        }
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        if (args.length == 1) {
            switch (args[0].toUpperCase()) {
                case "CREATE": {
                    if (!Permissions.hasPermission(sender, tempPermission, "create")) return false;
                    if (sender instanceof Player) {
                        sender.sendMessage(" ");
                        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                        sender.sendMessage(" §a/vrplot create §8<§7small/medium/large§8> §8<§7floorMaterial (optional)§8>");
                        sender.sendMessage(" §a/vrplot create §8<§7length§8> §8<§7width§8> §8<§7height§8> §8<§7floorMaterial (optional)§8>");
                    }
                    break;
                }
                case "REMOVE": {
                    if (!Permissions.hasPermission(sender, tempPermission, "remove")) return false;
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/vrplot remove §8<§7plotID§8>");
                    break;
                }
                case "SET": {
                    if (!Permissions.hasPermission(sender, tempPermission, "set")) return false;
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownedBy §8<§7username§8>");
                    sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7floorMaterial §8<§7material§8> §8<§7initialize (optional)§8>");
                    sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownerExpires §8<§7dd/mm/YYYY§8> §8<§7HH:mm (optional)§8>");
                    break;
                }
                case "ASSIGN": {
                    if (!Permissions.hasPermission(sender, tempPermission, "assign")) return false;
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/vrplot assign §8<§7plotID§8> §8<§7username§8>");
                    break;
                }
                case "UNASSIGN": {
                    if (!Permissions.hasPermission(sender, tempPermission, "unassign")) return false;
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/vrplot unassign §8<§7plotID§8>");
                    break;
                }
                case "INFO": {
                    if (!Permissions.hasPermission(sender, tempPermission, "info")) return false;
                    Plot plot = PlotManager.getPlot(p.getLocation());
                    if (plot == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cYou aren't standing on any plot!");
                        return false;
                    }
                    LocalDateTime localDateTime = plot.getOwnedUntilDate();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §7Plot ID §8§l‣ §f" + plot.getID());
                    sender.sendMessage(" §7Owned By §8§l‣ §a" + (plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : "§cAvailable"));
                    String assignedBy = "§cNot assigned";
                    if (plot.getAssignedBy() != null) {
                        switch (plot.getAssignedBy().toUpperCase()) {
                            case "CONSOLE": {
                                assignedBy = "§eConsole";
                                break;
                            }
                            case "SHOP_PURCHASE": {
                                assignedBy = "§eShop Purchase";
                                break;
                            }
                            default: {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(plot.getAssignedBy()));
                                assignedBy = (offlinePlayer.isOnline() ? "§a" : "§c") + offlinePlayer.getName();
                            }
                        }
                    }
                    sender.sendMessage(" §7Assigned By §8§l‣ §a" + assignedBy);
                    sender.sendMessage(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime));
                    sender.sendMessage(" §7Size §8§l‣ §f" + plot.getPlotSize());
                    sender.sendMessage(" §7Length §8§l‣ §f" + plot.getLength());
                    sender.sendMessage(" §7Width §8§l‣ §f" + plot.getWidth());
                    sender.sendMessage(" §7Height §8§l‣ §f" + plot.getHeight());
                    sender.sendMessage(" §7Floor Material §8§l‣ §f" + plot.getFloorMaterial().name());
//                    BaseComponent text1 = new TextComponent(plot.getBottomLeftCorner().toString());
//                    text1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7§oClick to teleport to the cords!")}));
//                    text1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + plot.getBottomLeftCorner().getBlockX() + " " + plot.getBottomLeftCorner().getBlockY() + " " + plot.getBottomLeftCorner().getBlockZ()));
                    //BaseComponent text12 = new TextComponent(" §7Pos 1 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f");

                    p.sendMessage(" §7Pos 1 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f" + plot.getBottomLeftCorner().toString());
//                    BaseComponent text2 = new TextComponent(plot.getBottomLeftCorner().toString());
//                    text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7§oClick to teleport to the cords!")}));
//                    text2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + plot.getTopRightCorner().getBlockX() + " " + plot.getTopRightCorner().getBlockY() + " " + plot.getTopRightCorner().getBlockZ()));
                    //BaseComponent text22 = new TextComponent(" §7Pos 2 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f");
                    p.sendMessage(" §7Pos 2 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f" + plot.getTopRightCorner().toString());
                    sender.sendMessage(" §7Created Direction §8§l‣ §f" + plot.getCreatedDirection().name());
                    break;
                }
                case "LIST": {
                    if (!Permissions.hasPermission(sender, tempPermission, "list")) return false;
                    if (PlotManager.plots.isEmpty()) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cThere are no plots!");
                        return false;
                    }
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" ");
                    //sender.sendMessage("§7|   §a§l§oID   §7|    §a§l§oOwned By   §7|   §a§l§oOwned Until  §7|   §a§l§oSize   §7|   §a§l§oCreated At   §7|");
                    sender.sendMessage("§7§m                                                                                ");
                    sender.sendMessage(" §7|  §a§l§oID  §7|   §a§l§oOwned By §7|  §a§l§oOwned Until §7|  §a§l§oSize §7|  §a§l§oPlot Center  §7|");
                    for (Plot plot : PlotManager.plots) {
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
                        sender.sendMessage(" §f" + plot.getID() + "§8   §f" + ownedBy.substring(0, 14) + "§8   §f" + (isOwned ? " " : "") + dateTimeFormatter.format(localDateTime) + "§8    §f" + size + "§8  §f" + plot.getCenter().toSimpleString());
                    }
                    sender.sendMessage("§7§m                                                                                ");
                    break;
                }
                default: {
                    printHelp(sender);
                    break;
                }
            }
        }
        if (args.length > 1) {
            switch (args[0].toUpperCase()) {
                case "CREATE": {
                    if (!Permissions.hasPermission(sender, commandPermission, "create")) return false;
                    if (sender instanceof Player) {
                        if (args.length == 2 || args.length == 3) {
                            PlotSize plotSize = null;
                            try {
                                plotSize = PlotSize.valueOf(args[1].toUpperCase());
                            } catch (IllegalArgumentException ignored) {
                            }
                            if (plotSize != null) {
                                //TODO create a plot
                                if (PlotManager.isColliding(PlotUtil.getPlotRegion(location, Direction.byYaw(location.getYaw()), plotSize.getLength(), plotSize.getWidth(), plotSize.getHeight()))) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cYou cant create new plot on the existing plot!");
                                    return false;
                                } else {
                                    Material material = plotSize.getFloorMaterial();
                                    if (args.length == 3) {
                                        try {
                                            //String[] materialText = Arrays.copyOfRange(args, 3, args.length);
                                            material = Material.getMaterial(args[2].toUpperCase().replaceAll(" ", "_"));
                                        } catch (IllegalArgumentException e) {
                                            sender.sendMessage(VirtualRealty.PREFIX + "§cMaterial not found");
                                            return false;
                                        }
                                    }
                                    sender.sendMessage(VirtualRealty.PREFIX + "§aNot colliding. Creating plot..");
                                    long timeStart = System.currentTimeMillis();
                                    PlotManager.createPlot(location, plotSize, material);
                                    long timeEnd = System.currentTimeMillis();
                                    sender.sendMessage(VirtualRealty.PREFIX + "§aPlot created! §8(§7" + (timeEnd - timeStart) + " ms§8)");
                                }
                            } else {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cSize not recognized!");
                                return false;
                            }
                        } else {
                            PlotSize plotSize = PlotSize.CUSTOM;
                            int length;
                            int width;
                            int height;
                            try {
                                length = Integer.parseInt(args[1]);
                                width = Integer.parseInt(args[2]);
                                height = Integer.parseInt(args[3]);
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                                return false;
                            }
                            if (length > 500 || width > 500 || height > 500) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cL, W and H hard-limit is 500!");
                                return false;
                            }
                            if (PlotManager.isColliding(PlotUtil.getPlotRegion(location, Direction.byYaw(location.getYaw()), length, width, height))) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cYou cant create new plot on the existing plot!");
                                return false;
                            } else {
                                Material material = Material.GRASS;
                                if (args.length >= 5) {
                                    try {
                                        //String[] materialText = Arrays.copyOfRange(args, 5, args.length);
                                        material = Material.getMaterial(args[4].toUpperCase().replaceAll(" ", "_"));
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(VirtualRealty.PREFIX + "§cMaterial not found");
                                        return false;
                                    }
                                }
                                sender.sendMessage(VirtualRealty.PREFIX + "§aNot colliding. Creating plot..");
                                long timeStart = System.currentTimeMillis();
                                PlotManager.createPlot(location, length, width, height, material, ((byte) 0));
                                long timeEnd = System.currentTimeMillis();
                                sender.sendMessage(VirtualRealty.PREFIX + "§aPlot created! §8(§7" + (timeEnd - timeStart) + " ms§8)");
                            }
                        }
                    }
                    break;
                }
                case "REMOVE": {
                    if (!Permissions.hasPermission(sender, commandPermission, "remove")) return false;
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
                    plot.remove();
                    sender.sendMessage(VirtualRealty.PREFIX + "§aSuccessfully removed plot!");
                    break;
                }
                case "SET": {
                    if (!Permissions.hasPermission(sender, commandPermission, "set")) return false;
                    if (args.length >= 3) {
                        switch (args[2].toUpperCase()) {
                            case "OWNEDBY": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.ownedby")) return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cSpecify username!");
                                    return false;
                                }
                                int plotID;
                                OfflinePlayer offlinePlayer;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                                    return false;
                                }
                                try {
                                    offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
                                } catch (NullPointerException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get player with specified username!");
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get plot with specified ID!");
                                    return false;
                                }
                                plot.setOwnedBy(offlinePlayer.getUniqueId());
                                sender.sendMessage(VirtualRealty.PREFIX + "§aPlot has been assigned to §f" + offlinePlayer.getName() + "!");
                                plot.update();
                                break;
                            }
                            case "FLOORMATERIAL": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.floormaterial")) return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cSpecify material name!");
                                    return false;
                                }
                                int plotID;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                                    return false;
                                }
                                Material material;
                                try {
                                    material = Material.matchMaterial(args[3]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get material with specified name!");
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get plot with specified ID!");
                                    return false;
                                }
                                plot.setFloorMaterial(material);
                                if (args.length == 5 && (args[4].toLowerCase().startsWith("t"))) {
                                    plot.initializeFloor();
                                    sender.sendMessage(VirtualRealty.PREFIX + "§aNew floor material has been set and initialized!");
                                    plot.update();
                                    return false;
                                }
                                sender.sendMessage(VirtualRealty.PREFIX + "§aNew floor material has been set!");
                                plot.update();
                                break;
                            }
                            case "OWNEREXPIRES": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.ownerexpires")) return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cSpecify expiry date!");
                                    return false;
                                }
                                int plotID;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                                    return false;
                                }
                                String dateFormat = args[3];
                                String timeFormat;
                                int year;
                                int month;
                                int dayOfMonth;
                                int hour = 0;
                                int minute = 0;
                                LocalDateTime localDateTime;
                                try {
                                    year = Integer.parseInt(dateFormat.split("/")[2]);
                                    month = Integer.parseInt(dateFormat.split("/")[1]);
                                    dayOfMonth = Integer.parseInt(dateFormat.split("/")[0]);
                                    if (args.length == 5) {
                                        timeFormat = args[4];
                                        hour = Integer.parseInt(timeFormat.split(":")[0]);
                                        minute = Integer.parseInt(timeFormat.split(":")[1]);
                                    }
                                    localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cInvalid date format provided!");
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get plot with specified ID!");
                                    return false;
                                }
                                plot.setOwnedUntilDate(localDateTime);
                                sender.sendMessage(VirtualRealty.PREFIX + "§aOwned until date has been updated!");
                                plot.update();
                                break;
                            }
                            default: {
                                sender.sendMessage(" ");
                                sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                                sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownedBy §8<§7username§8>");
                                sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7floorMaterial §8<§7material§8> §8<§7initialize (optional)§8>");
                                sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownerExpires §8<§7dd/mm/YYYY§8> §8<§7HH:mm (optional)§8>");
                                break;
                            }
                        }
                    } else {
                        sender.sendMessage(" ");
                        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownedBy §8<§7username§8>");
                        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7floorMaterial §8<§7material§8> §8<§7initialize (optional)§8>");
                        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownerExpires §8<§7dd/mm/YYYY§8> §8<§7HH:mm (optional)§8>");
                    }
                    break;
                }
                case "ASSIGN": {
                    if (!Permissions.hasPermission(sender, commandPermission, "assign")) return false;
                    if (args.length == 3) {
                        int plotID;
                        OfflinePlayer offlinePlayer;
                        try {
                            plotID = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                            return false;
                        }
                        try {
                            offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                        } catch (NullPointerException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get player with specified username!");
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get plot with specified ID!");
                            return false;
                        }
                        if (sender instanceof Player) {
                            plot.setAssignedBy(p.getUniqueId().toString());
                        } else if (sender instanceof ConsoleCommandSender){
                            plot.setAssignedBy("CONSOLE");
                        } else {
                            plot.setAssignedBy("SHOP_PURCHASE");
                        }
                        plot.setOwnedBy(offlinePlayer.getUniqueId());
                        sender.sendMessage(VirtualRealty.PREFIX + "§aPlot has been assigned to §f" + offlinePlayer.getName() + " §aby §f" + sender.getName() + "!");
                        plot.update();
                    }
                    break;
                }
                case "UNASSIGN": {
                    if (!Permissions.hasPermission(sender, commandPermission, "unassign")) return false;
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
                        plot.setAssignedBy(null);
                        plot.setOwnedBy(null);
                        sender.sendMessage(VirtualRealty.PREFIX + "§aPlot has been unassigned!");
                        plot.update();
                    }
                    break;
                }
                case "INFO": {
                    if (!Permissions.hasPermission(sender, commandPermission, "info")) return false;
                    int plotID;
                    try {
                        plotID = Integer.parseInt(args[1]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                        return false;
                    }
                    if (PlotManager.plots.isEmpty()) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cThere are no plots!");
                        return false;
                    }
                    if (plotID < PlotManager.plots.get(0).getID()) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cMinimum plot ID is " + PlotManager.plots.get(0).getID() + "!");
                        return false;
                    }
                    if (plotID > PlotManager.plots.get(PlotManager.plots.size() - 1).getID()) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cMaximum plot ID is " + PlotManager.plots.get(PlotManager.plots.size() - 1).getID() + "!");
                        return false;
                    }
                    Plot plot = PlotManager.getPlot(plotID);
                    if (plot == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cCouldn't get plot with specified ID!");
                        return false;
                    }
                    LocalDateTime localDateTime = plot.getOwnedUntilDate();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §7Plot ID §8§l‣ §f" + plot.getID());
                    sender.sendMessage(" §7Owned By §8§l‣ §a" + (plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : "§cAvailable"));
                    String assignedBy = "§cNot assigned";
                    if (plot.getAssignedBy() != null) {
                        switch (plot.getAssignedBy().toUpperCase()) {
                            case "CONSOLE": {
                                assignedBy = "§eConsole";
                                break;
                            }
                            case "SHOP_PURCHASE": {
                                assignedBy = "§eShop Purchase";
                                break;
                            }
                            default: {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(plot.getAssignedBy()));
                                assignedBy = (offlinePlayer.isOnline() ? "§a" : "§c") + offlinePlayer.getName();
                            }
                        }
                    }
                    sender.sendMessage(" §7Assigned By §8§l‣ §a" + assignedBy);
                    sender.sendMessage(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime));
                    sender.sendMessage(" §7Size §8§l‣ §f" + plot.getPlotSize());
                    sender.sendMessage(" §7Length §8§l‣ §f" + plot.getLength());
                    sender.sendMessage(" §7Width §8§l‣ §f" + plot.getWidth());
                    sender.sendMessage(" §7Height §8§l‣ §f" + plot.getHeight());
                    sender.sendMessage(" §7Floor Material §8§l‣ §f" + plot.getFloorMaterial().name());
//                    BaseComponent text1 = new TextComponent(plot.getBottomLeftCorner().toString());
//                    text1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7§oClick to teleport to the cords!")}));
//                    text1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + plot.getBottomLeftCorner().getBlockX() + " " + plot.getBottomLeftCorner().getBlockY() + " " + plot.getBottomLeftCorner().getBlockZ()));
//                    BaseComponent text12 = new TextComponent(" §7Pos 1 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f");
//                    p.spigot().sendMessage(text12, text1);
                    p.sendMessage(" §7Pos 1 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f" + plot.getBottomLeftCorner().toString());
//                    BaseComponent text2 = new TextComponent(plot.getBottomLeftCorner().toString());
//                    text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7§oClick to teleport to the cords!")}));
//                    text2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + plot.getTopRightCorner().getBlockX() + " " + plot.getTopRightCorner().getBlockY() + " " + plot.getTopRightCorner().getBlockZ()));
//                    BaseComponent text22 = new TextComponent(" §7Pos 2 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f");
                    p.sendMessage(" §7Pos 2 §8| §7X §8| §7Y §8| §7Z §8| §8§l‣ §f" + plot.getTopRightCorner().toString());
                    sender.sendMessage(" §7Created Direction §8§l‣ §f" + plot.getCreatedDirection().name());
                    break;
                }
                default: {
                    printHelp(sender);
                    break;
                }
            }
        }
        return false;
    }

    private static void printHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        sender.sendMessage(" §a/vrplot create §8- §7Creates a plot");
        sender.sendMessage(" §a/vrplot remove §8- §7Removes a plot");
        sender.sendMessage(" §a/vrplot set §8- §7Sets a variable for the plot");
        sender.sendMessage(" §a/vrplot assign §8- §7Assigns a plot to player");
        sender.sendMessage(" §a/vrplot unassign §8- §7Sets assigned to and assigned by to null");
        sender.sendMessage(" §a/vrplot info §8- §7Prints info about plot");
        sender.sendMessage(" §a/vrplot list §8- §7Prints all plots");
    }

}
