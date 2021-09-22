package me.plytki.virtualrealty.commands;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.Direction;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.utils.ConfigurationFactory;
import me.plytki.virtualrealty.utils.Permissions;
import me.plytki.virtualrealty.utils.PlotUtil;
import me.plytki.virtualrealty.utils.UUIDUtils;
import me.plytki.virtualrealty.utils.multiversion.Chat;
import me.plytki.virtualrealty.utils.multiversion.VMaterial;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
                        sender.sendMessage(" §a/vrplot create §8<§7small/medium/large§8> §8<§7floorMaterial (optional)§8> §8<§7borderMaterial (optional)§8>");
                        sender.sendMessage(" §a/vrplot create §8<§7length§8> §8<§7width§8> §8<§7height§8> §8<§7floorMaterial (optional)§8> §8<§7borderMaterial (optional)§8>");
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
                    printSetHelp(sender);
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
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notStandingOnPlot);
                        return false;
                    }
                    printInfo(sender, plot);
                    break;
                }
                case "LIST": {
                    if (!Permissions.hasPermission(sender, tempPermission, "list")) return false;
                    if (PlotManager.plots.isEmpty()) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlots);
                        return false;
                    }
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" ");
                    sender.sendMessage("§7§m                                                                                ");
                    sender.sendMessage("§7|  §a§l§oID§7  |  §a§l§oOwned By§7 |  §a§l§oOwned Until§7 |  §a§l§oSize§7 |  §a§l§oPlot Center§7  |");
                    for (Plot plot : PlotManager.plots) {
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
                        BaseComponent textComponent = new TextComponent("§f" + plot.getID() + "§8   §f" + ownedBy.substring(0, 14) + "§8  §f" + (isOwned ? " " : "") + dateTimeFormatter.format(localDateTime) + "§8    §f" + size + "§8  §f" + plot.getCenter().toSimpleString());
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(VirtualRealty.getMessages().clickToShowDetailedInfo.replaceAll("%plot_id%", String.valueOf(plot.getID())))}));
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vrplot info " + plot.getID()));
                        new Chat(textComponent).sendTo(sender);
                    }
                    sender.sendMessage("§7§m                                                                                ");
                    break;
                }
                case "TP": {
                    if (!Permissions.hasPermission(sender, tempPermission, "tp")) return false;
                    sender.sendMessage(" ");
                    sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
                    sender.sendMessage(" §a/vrplot tp §8<§7plotID§8>");
                    break;
                }
                case "RELOAD": {
                    if (!Permissions.hasPermission(sender, tempPermission, "reload")) return false;
                    try {
                        VirtualRealty.getInstance().reloadConfigs();
                        if (VirtualRealty.getPluginConfiguration().dynmapMarkers) {
                            if (VirtualRealty.markerset != null) {
                                VirtualRealty.markerset.deleteMarkerSet();
                            }
                            VirtualRealty.getInstance().registerDynmap();
                            for (Plot plot : PlotManager.plots) {
                                PlotManager.resetPlotMarker(plot);
                            }
                        } else {
                            if (VirtualRealty.markerset != null) {
                                VirtualRealty.markerset.deleteMarkerSet();
                            }
                        }
                        PlotManager.loadPlots();
                        VirtualRealty.getInstance().loadSizesConfiguration();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().reloadCompleted);
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
                        if (Arrays.stream(PlotSize.values()).anyMatch(plotSize -> plotSize.name().equalsIgnoreCase(args[1])) && !args[1].equalsIgnoreCase(PlotSize.CUSTOM.name())) {
                            PlotSize plotSize = null;
                            try {
                                plotSize = PlotSize.valueOf(args[1].toUpperCase());
                            } catch (IllegalArgumentException ignored) {
                            }
                            if (plotSize != null) {
                                if (PlotManager.isColliding(PlotUtil.getPlotRegion(location, Direction.byYaw(location.getYaw()), plotSize.getLength(), plotSize.getWidth(), plotSize.getHeight()))) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantCreateOnExisting);
                                    return false;
                                } else {
                                    Material floorMaterial = null;
                                    byte floorData = 0;
                                    if (args.length >= 3) {
                                        try {
                                            floorMaterial = VMaterial.getMaterial(args[2].split(":")[0].toUpperCase());
                                            if (floorMaterial == null) {
                                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                                                return false;
                                            }
                                        } catch (IllegalArgumentException e) {
                                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                                            return false;
                                        }
                                        if (args[2].split(":").length == 2) {
                                            floorData = Byte.parseByte(args[2].split(":")[1]);
                                        }
                                    }
                                    Material borderMaterial = null;
                                    byte borderData = 0;
                                    if (args.length >= 4) {
                                        try {
                                            borderMaterial = VMaterial.getMaterial(args[3].split(":")[0].toUpperCase());
                                            if (borderMaterial == null) {
                                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                                                return false;
                                            }
                                        } catch (IllegalArgumentException e) {
                                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                                            return false;
                                        }
                                        if (args[3].split(":").length == 2) {
                                            borderData = Byte.parseByte(args[3].split(":")[1]);
                                        }
                                    }
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
                                    long timeStart = System.currentTimeMillis();
                                    Plot plot = PlotManager.createPlot(location, plotSize);
                                    if (floorMaterial != null) {
                                        plot.setFloorMaterial(floorMaterial, floorData);
                                    }
                                    if (borderMaterial != null) {
                                        plot.setBorder(borderMaterial, borderData);
                                    }
                                    long timeEnd = System.currentTimeMillis();
                                    BaseComponent textComponent = new TextComponent(VirtualRealty.PREFIX + VirtualRealty.getMessages().creationPlotComponent1);
                                    BaseComponent textComponent2 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent2.replaceAll("%plot_id%", String.valueOf(plot.getID())));
                                    BaseComponent textComponent3 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent3.replaceAll("%creation_time%", String.valueOf(timeEnd - timeStart)));
                                    textComponent2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(VirtualRealty.getMessages().clickToShowDetailedInfo.replaceAll("%plot_id%", String.valueOf(plot.getID())))}));
                                    textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vrplot info " + plot.getID()));
                                    textComponent.addExtra(textComponent2);
                                    textComponent.addExtra(textComponent3);
                                    new Chat(textComponent).sendTo(p);
                                }
                            } else {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().sizeNotRecognised);
                                return false;
                            }
                        } else {
                            int length;
                            int width;
                            int height;
                            try {
                                length = Integer.parseInt(args[1]);
                                width = Integer.parseInt(args[2]);
                                height = Integer.parseInt(args[3]);
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                                return false;
                            }
                            if (length > 500 || width > 500 || height > 500) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().LWHHardLimit);
                                return false;
                            }
                            if (PlotManager.isColliding(PlotUtil.getPlotRegion(location, Direction.byYaw(location.getYaw()), length, width, height))) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantCreateOnExisting);
                                return false;
                            } else {
                                Material floorMaterial = null;
                                byte floorData = 0;
                                if (args.length >= 5) {
                                    try {
                                        floorMaterial = VMaterial.getMaterial(args[4].split(":")[0].toUpperCase());
                                        if (floorMaterial == null) {
                                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                                            return false;
                                        }
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                                        return false;
                                    }
                                    if (args[4].split(":").length == 2) {
                                        floorData = Byte.parseByte(args[4].split(":")[1]);
                                    }
                                }
                                Material borderMaterial = null;
                                byte borderData = 0;
                                if (args.length >= 6) {
                                    try {
                                        borderMaterial = VMaterial.getMaterial(args[5].split(":")[0].toUpperCase());
                                        if (borderMaterial == null) {
                                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                                            return false;
                                        }
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                                        return false;
                                    }
                                    if (args[5].split(":").length == 2) {
                                        borderData = Byte.parseByte(args[5].split(":")[1]);
                                    }
                                }
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
                                long timeStart = System.currentTimeMillis();
                                Plot plot = PlotManager.createPlot(location, length, width, height);
                                if (floorMaterial != null) {
                                    plot.setFloorMaterial(floorMaterial, floorData);
                                }
                                if (borderMaterial != null) {
                                    plot.setBorder(borderMaterial, borderData);
                                }
                                long timeEnd = System.currentTimeMillis();
                                BaseComponent textComponent = new TextComponent(VirtualRealty.PREFIX + VirtualRealty.getMessages().creationPlotComponent1);
                                BaseComponent textComponent2 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent2.replaceAll("%plot_id%", String.valueOf(plot.getID())));
                                BaseComponent textComponent3 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent3.replaceAll("%creation_time%", String.valueOf(timeEnd - timeStart)));
                                textComponent2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(VirtualRealty.getMessages().clickToShowDetailedInfo.replaceAll("%plot_id%", String.valueOf(plot.getID())))}));
                                textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vrplot info " + plot.getID()));
                                textComponent.addExtra(textComponent2);
                                textComponent.addExtra(textComponent3);
                                new Chat(textComponent).sendTo(p);
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
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                        return false;
                    }
                    Plot plot = PlotManager.getPlot(plotID);
                    if (plot == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                        return false;
                    }
                    plot.remove();
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().removedPlot);
                    break;
                }
                case "SET": {
                    if (!Permissions.hasPermission(sender, commandPermission, "set")) return false;
                    if (args.length >= 3) {
                        switch (args[2].toUpperCase()) {
                            case "OWNEDBY": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.ownedby")) return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyUsername);
                                    return false;
                                }
                                int plotID;
                                OfflinePlayer offlinePlayer;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                                    return false;
                                }
                                try {
                                    if (UUIDUtils.isValidUUID(args[3])) {
                                        offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[3]));
                                    } else {
                                        offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
                                    }
                                    if (offlinePlayer.getName() == null) {
                                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                                        return false;
                                    }
                                } catch (NullPointerException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                                    return false;
                                }
                                if (args.length >= 5) {
                                    String dateFormat = args[4];
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
                                        if (args.length >= 6) {
                                            timeFormat = args[5];
                                            hour = Integer.parseInt(timeFormat.split(":")[0]);
                                            minute = Integer.parseInt(timeFormat.split(":")[1]);
                                        }
                                        localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().invalidDateProvided);
                                        return false;
                                    }
                                    plot.setOwnedUntilDate(localDateTime);
                                }
                                if (sender instanceof RemoteConsoleCommandSender && args.length >= 7 && args[6].equalsIgnoreCase("assign")) {
                                    plot.setAssignedBy("SHOP_PURCHASE");
                                }
                                plot.setOwnedBy(offlinePlayer.getUniqueId());
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().assignedTo.replaceAll("%assigned_to%", offlinePlayer.getName()));
                                plot.update();
                                break;
                            }
                            case "FLOORMATERIAL": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.floormaterial")) return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyMaterialName);
                                    return false;
                                }
                                int plotID;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                                    return false;
                                }
                                Material material;
                                try {
                                    material = Material.matchMaterial(args[3].split(":")[0].toUpperCase());
                                    if (material == null) {
                                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                                        return false;
                                    }
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                                    return false;
                                }
                                byte data = 0;
                                if (args[3].split(":").length == 2) {
                                    data = Byte.parseByte(args[3].split(":")[1]);
                                }
                                plot.setFloorMaterial(material, data);
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().newFloorMaterialSet);
                                plot.update();
                                break;
                            }
                            case "BORDERMATERIAL": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.bordermaterial"))
                                    return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyMaterialName);
                                    return false;
                                }
                                int plotID;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                                    return false;
                                }
                                Material material;
                                try {
                                    material = VMaterial.getMaterial(args[3].split(":")[0].toUpperCase());
                                    if (material == null) {
                                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                                        return false;
                                    }
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                                    return false;
                                }
                                byte data = 0;
                                if (args[3].split(":").length == 2) {
                                    data = Byte.parseByte(args[3].split(":")[1]);
                                }
                                plot.setBorder(material, data);
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().newBorderMaterialSet);
                                plot.update();
                                return false;
                            }
                            case "OWNERSHIPEXPIRES": {
                                if (!Permissions.hasPermission(sender, commandPermission, "set.ownerexpires")) return false;
                                if (args.length < 4) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyExpiryDate);
                                    return false;
                                }
                                int plotID;
                                try {
                                    plotID = Integer.parseInt(args[1]);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
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
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().invalidDateProvided);
                                    return false;
                                }
                                Plot plot = PlotManager.getPlot(plotID);
                                if (plot == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                                    return false;
                                }
                                plot.setOwnedUntilDate(localDateTime);
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownedUntilUpdated);
                                plot.update();
                                break;
                            }
                            default: {
                                printSetHelp(sender);
                                break;
                            }
                        }
                    } else {
                        printSetHelp(sender);
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
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                            return false;
                        }
                        try {
                            if (UUIDUtils.isValidUUID(args[2])) {
                                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[2]));
                            } else {
                                offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            }
                            if (offlinePlayer.getName() == null) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                                return false;
                            }
                        } catch (NullPointerException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
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
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().assignedToBy.replaceAll("%assigned_to%", offlinePlayer.getName()).replaceAll("%assigned_by%", sender.getName()));
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
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                            return false;
                        }
                        Plot plot = PlotManager.getPlot(plotID);
                        if (plot == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                            return false;
                        }
                        plot.setAssignedBy(null);
                        plot.setOwnedBy(null);
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().unassigned);
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
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                        return false;
                    }
                    if (PlotManager.plots.isEmpty()) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlots);
                        return false;
                    }
                    if (plotID < PlotManager.getPlotMinID()) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().minPlotID.replaceAll("%min_id%", String.valueOf(PlotManager.getPlotMinID())));
                        return false;
                    }
                    if (plotID > PlotManager.getPlotMaxID()) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().maxPlotID.replaceAll("%max_id%", String.valueOf(PlotManager.getPlotMaxID())));
                        return false;
                    }
                    Plot plot = PlotManager.getPlot(plotID);
                    if (plot == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                        return false;
                    }
                    printInfo(sender, plot);
                    break;
                }
                case "TP": {
                    if (!Permissions.hasPermission(sender, commandPermission, "tp")) return false;
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cmdOnlyPlayers);
                        return false;
                    }
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
                        Location loc = new Location(plot.getCreatedLocation().getWorld(), plot.getCenter().getBlockX(), plot.getCenter().getBlockY() + 1, plot.getCenter().getBlockZ());
                        loc.setY(loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getY() + 1);
                        p.teleport(loc);
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().teleportedToPlot);
                    }
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

    private static void printInfo(CommandSender sender, Plot plot) {
        LocalDateTime localDateTime = plot.getOwnedUntilDate();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String assignedBy = VirtualRealty.getMessages().notAssigned;
        if (plot.getAssignedBy() != null) {
            switch (plot.getAssignedBy().toUpperCase()) {
                case "CONSOLE": {
                    assignedBy = VirtualRealty.getMessages().assignedByConsole;
                    break;
                }
                case "SHOP_PURCHASE": {
                    assignedBy = VirtualRealty.getMessages().assignedByShopPurchase;
                    break;
                }
                default: {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(plot.getAssignedBy()));
                    assignedBy = (offlinePlayer.isOnline() ? "§a" : "§c") + offlinePlayer.getName();
                }
            }
        }
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
        sender.sendMessage(" §7Assigned By §8§l‣ §a" + assignedBy);
        sender.sendMessage(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime));
        sender.sendMessage(" §7Size §8§l‣ §f" + plot.getPlotSize());
        sender.sendMessage(" §7Length §8§l‣ §f" + plot.getLength());
        sender.sendMessage(" §7Width §8§l‣ §f" + plot.getWidth());
        sender.sendMessage(" §7Height §8§l‣ §f" + plot.getHeight());
        sender.sendMessage(" §7Floor Material §8§l‣ §f" + plot.getFloorMaterial().name());
        sender.sendMessage(" §7Border Material §8§l‣ §f" + plot.getBorderMaterial().name());
        sender.sendMessage(" §7Pos 1 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getBottomLeftCorner().toString());
        sender.sendMessage(" §7Pos 2 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getTopRightCorner().toString());
        sender.sendMessage(" §7Created Direction §8§l‣ §f" + plot.getCreatedDirection().name());
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
        sender.sendMessage(" §a/vrplot tp §8- §7Teleports to the plot");
        sender.sendMessage(" §a/vrplot reload §8- §7Reloads plugin");
    }

    private static void printSetHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownedBy §8<§7username§8>");
        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7floorMaterial §8<§7material§8>");
        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7borderMaterial §8<§7material§8>");
        sender.sendMessage(" §a/vrplot set §8<§7plotID§8> §7ownershipExpires §8<§7dd/mm/YYYY§8> §8<§7HH:mm (optional)§8>");
    }

}