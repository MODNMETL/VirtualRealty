package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandExecution;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.PermissionUtil;
import com.modnmetl.virtualrealty.utils.UUIDUtils;
import com.modnmetl.virtualrealty.utils.multiversion.VMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

import java.time.LocalDateTime;
import java.util.*;

public class SetSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot set §8<§7plot§8> §aowner §8<§7username§8>");
        HELP.add(" §a/vrplot set §8<§7plot§8> §afloor §8<§7material§8>");
        HELP.add(" §a/vrplot set §8<§7plot§8> §aborder §8<§7material§8>");
        HELP.add(" §a/vrplot set §8<§7plot§8> §aexpiry §8<§7dd/mm/YYYY§8> §8<§7HH:mm (optional)§8>");
    }

    public SetSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        if (!PermissionUtil.hasPermission(sender, VirtualRealtyCommand.COMMAND_PERMISSION.getName() + args[0].toLowerCase())) return;
        if (args.length < 3) {
            printHelp();
            return;
        }
        switch (args[2].toUpperCase()) {
            case "OWNER": {
                if (!PermissionUtil.hasPermission(sender, VirtualRealtyCommand.COMMAND_PERMISSION.getName() + args[0].toLowerCase() + "." + args[2].toLowerCase())) return;
                if (args.length < 4) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyUsername);
                    return;
                }
                int plotID;
                OfflinePlayer offlinePlayer;
                try {
                    plotID = Integer.parseInt(args[1]);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                    return;
                }
                try {
                    if (UUIDUtils.isValidUUID(args[3])) {
                        offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[3]));
                    } else {
                        offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
                    }
                    if (offlinePlayer.getName() == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                        return;
                    }
                } catch (NullPointerException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
                    return;
                }
                Plot plot = PlotManager.getPlot(plotID);
                if (plot == null) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                    return;
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
                        return;
                    }
                    plot.setOwnedUntilDate(localDateTime);
                }
                if (sender instanceof RemoteConsoleCommandSender && args.length >= 7 && args[6].equalsIgnoreCase("assign")) {
                    plot.setAssignedBy("SHOP_PURCHASE");
                }
                plot.setOwnedBy(offlinePlayer.getUniqueId());
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().assignedTo.replaceAll("%assigned_to%", offlinePlayer.getName()));
                plot.update();
                return;
            }
            case "FLOOR": {
                if (!PermissionUtil.hasPermission(sender, VirtualRealtyCommand.COMMAND_PERMISSION.getName() + args[0].toLowerCase() + "." + args[2].toLowerCase())) return;
                if (args.length < 4) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyMaterialName);
                    return;
                }
                int plotID;
                try {
                    plotID = Integer.parseInt(args[1]);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                    return;
                }
                Material material;
                try {
                    material = Material.matchMaterial(args[3].split(":")[0].toUpperCase());
                    if (material == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                    return;
                }
                Plot plot = PlotManager.getPlot(plotID);
                if (plot == null) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                    return;
                }
                byte data = 0;
                if (args[3].split(":").length == 2) {
                    data = Byte.parseByte(args[3].split(":")[1]);
                }
                plot.setFloorMaterial(material, data);
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().newFloorMaterialSet);
                plot.update();
                return;
            }
            case "BORDER": {
                if (!PermissionUtil.hasPermission(sender, VirtualRealtyCommand.COMMAND_PERMISSION.getName() + args[0].toLowerCase() + "." + args[2].toLowerCase())) return;
                if (args.length < 4) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyMaterialName);
                    return;
                }
                int plotID;
                try {
                    plotID = Integer.parseInt(args[1]);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                    return;
                }
                Material material;
                try {
                    material = VMaterial.getMaterial(args[3].split(":")[0].toUpperCase());
                    if (material == null) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetMaterial);
                    return;
                }
                Plot plot = PlotManager.getPlot(plotID);
                if (plot == null) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                    return;
                }
                byte data = 0;
                if (args[3].split(":").length == 2) {
                    data = Byte.parseByte(args[3].split(":")[1]);
                }
                plot.setBorderMaterial(material, data);
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().newBorderMaterialSet);
                plot.update();
                return;
            }
            case "EXPIRY": {
                if (!PermissionUtil.hasPermission(sender, VirtualRealtyCommand.COMMAND_PERMISSION.getName() + args[0].toLowerCase() + "." + args[2].toLowerCase())) return;
                if (args.length < 4) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().specifyExpiryDate);
                    return;
                }
                int plotID;
                try {
                    plotID = Integer.parseInt(args[1]);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                    return;
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
                    return;
                }
                Plot plot = PlotManager.getPlot(plotID);
                if (plot == null) {
                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
                    return;
                }
                plot.setOwnedUntilDate(localDateTime);
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownedUntilUpdated);
                plot.update();
                return;
            }
            default: {
                for (String helpMessage : HELP) {
                    sender.sendMessage(helpMessage);
                }
            }
        }
    }

}
