package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.multiversion.ChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.UUID;

public class InfoSubCommand extends SubCommand {

    public InfoSubCommand() {}

    public InfoSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        if (args.length < 2) {
            assertPlayer();
            Plot plot = PlotManager.getInstance().getPlot(((Player) sender).getLocation());
            if (plot == null) {
                ChatMessage.of(VirtualRealty.getMessages().notStandingOnPlot).sendWithPrefix(sender);
                return;
            }
            printInfo(sender, plot);
            return;
        }
        int plotID;
        try {
            plotID = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            ChatMessage.of(VirtualRealty.getMessages().useNaturalNumbersOnly).sendWithPrefix(sender);
            return;
        }
        if (PlotManager.getInstance().getPlots().isEmpty()) {
            ChatMessage.of(VirtualRealty.getMessages().noPlots).sendWithPrefix(sender);
            return;
        }
        if (plotID < PlotManager.getInstance().getPlotMinID()) {
            String message = VirtualRealty.getMessages().minPlotID.replaceAll("%min_id%", String.valueOf(PlotManager.getInstance().getPlotMinID()));
            ChatMessage.of(message).sendWithPrefix(sender);
            return;
        }
        if (plotID > PlotManager.getInstance().getPlotMaxID()) {
            String message = VirtualRealty.getMessages().maxPlotID.replaceAll("%max_id%", String.valueOf(PlotManager.getInstance().getPlotMaxID()));
            ChatMessage.of(message).sendWithPrefix(sender);
            return;
        }
        Plot plot = PlotManager.getInstance().getPlot(plotID);
        if (plot == null) {
            ChatMessage.of(VirtualRealty.getMessages().noPlotFound).sendWithPrefix(sender);
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
        ChatMessage.of(" ").sendWithPrefix(sender);
        ChatMessage.of(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»").sendWithPrefix(sender);
        ChatMessage.of(" §7Plot ID §8§l‣ §f" + plot.getID()).sendWithPrefix(sender);
        ChatMessage.of(" §7Owned By §8§l‣ §a" + (plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : "§cAvailable")).sendWithPrefix(sender);
        if (plot.getMembers().size() != 0) {
            ChatMessage.of(" §7Members §8§l↴").send(sender);
            for (OfflinePlayer offlinePlayer : plot.getPlayerMembers()) {
                ChatMessage.of(" §8§l⁍ §" + (offlinePlayer.isOnline() ? "a" : "c") + offlinePlayer.getName()).send(sender);
            }
        }
        ChatMessage.of(" §7Assigned By §8§l‣ §a" + assignedBy).send(sender);
        ChatMessage.of(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime)).send(sender);
        ChatMessage.of(" §7Size §8§l‣ §f" + plot.getPlotSize()).send(sender);
        ChatMessage.of(" §7Length §8§l‣ §f" + plot.getLength()).send(sender);
        ChatMessage.of(" §7Height §8§l‣ §f" + plot.getHeight()).send(sender);
        ChatMessage.of(" §7Width §8§l‣ §f" + plot.getWidth()).send(sender);
        ChatMessage.of(" §7Floor Material §8§l‣ §f" + plot.getFloorMaterialName()).send(sender);
        ChatMessage.of(" §7Border Material §8§l‣ §f" + plot.getBorderMaterialName()).send(sender);
        ChatMessage.of(" §7Pos 1 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getBottomLeftCorner().toString()).send(sender);
        ChatMessage.of(" §7Pos 2 §8( §7X §8| §7Y §8| §7Z §8) §8§l‣ §f" + plot.getTopRightCorner().toString()).send(sender);
        ChatMessage.of(" §7Created Direction §8§l‣ §f" + plot.getCreatedDirection().name()).send(sender);
    }

}
