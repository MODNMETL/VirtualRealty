package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.UUID;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public class InfoSubCommand extends SubCommand {

    public InfoSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission(COMMAND_PERMISSION.getName() + "." + args[0].toLowerCase());
        if (args.length < 2) {
            assertPlayer();
            Plot plot = PlotManager.getPlot(((Player) sender).getLocation());
            if (plot == null) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notStandingOnPlot);
                return;
            }
            printInfo(sender, plot);
            return;
        }
        int plotID;
        try {
            plotID = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
            return;
        }
        if (PlotManager.getPlots().isEmpty()) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlots);
            return;
        }
        if (plotID < PlotManager.getPlotMinID()) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().minPlotID.replaceAll("%min_id%", String.valueOf(PlotManager.getPlotMinID())));
            return;
        }
        if (plotID > PlotManager.getPlotMaxID()) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().maxPlotID.replaceAll("%max_id%", String.valueOf(PlotManager.getPlotMaxID())));
            return;
        }
        Plot plot = PlotManager.getPlot(plotID);
        if (plot == null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
            return;
        }
        printInfo(sender, plot);
    }

    private void printInfo(CommandSender sender, Plot plot) {
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
            for (OfflinePlayer offlinePlayer : plot.getPlayerMembers()) {
                sender.sendMessage(" §8§l⁍ §" + (offlinePlayer.isOnline() ? "a" : "c") + offlinePlayer.getName());
            }
        }
        sender.sendMessage(" §7Assigned By §8§l‣ §a" + assignedBy);
        sender.sendMessage(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime));
        sender.sendMessage(" §7Size §8§l‣ §f" + plot.getPlotSize());
        sender.sendMessage(" §7Length §8§l‣ §f" + plot.getLength());
        sender.sendMessage(" §7Height §8§l‣ §f" + plot.getHeight());
        sender.sendMessage(" §7Width §8§l‣ §f" + plot.getWidth());
        sender.sendMessage(" §7Floor Material §8§l‣ §f" + plot.getFloorMaterial().name());
        sender.sendMessage(" §7Border Material §8§l‣ §f" + plot.getBorderMaterial().name());
        sender.sendMessage(" §7Pos 1 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getBottomLeftCorner().toString());
        sender.sendMessage(" §7Pos 2 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getTopRightCorner().toString());
        sender.sendMessage(" §7Created Direction §8§l‣ §f" + plot.getCreatedDirection().name());
    }

}
