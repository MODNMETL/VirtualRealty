package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class ListSubCommand extends SubCommand {

    public ListSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        boolean hasPlot = false;
        for (Plot plot : PlotManager.getPlots()) {
            if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(player.getUniqueId()) && plot.getOwnedUntilDate().isAfter(LocalDateTime.now())) {
                hasPlot = true;
                break;
            }
        }
        boolean isMember = false;
        for (Plot plot : PlotManager.getPlots()) {
            if (plot.getMember(player.getUniqueId()) != null) {
                isMember = true;
                break;
            }
        }
        if (!hasPlot && !isMember) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlayerPlotsFound);
            return;
        }
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        sender.sendMessage(" ");
        if (hasPlot) {
            sender.sendMessage("§7§m                                                                                ");
            sender.sendMessage("§7|  §a§l§oID§7  |  §a§l§oOwned Until§7 |  §a§l§oSize§7  |  §a§l§oPlot Center§7  |");
            for (Plot plot : PlotManager.getPlots()) {
                if (plot.getPlotOwner() != null && plot.getPlotOwner().getUniqueId().equals(player.getUniqueId())) {
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
            for (Plot plot : PlotManager.getPlots()) {
                if (plot.getPlotOwner() != null && !plot.getPlotOwner().getUniqueId().equals(player.getUniqueId()) && plot.hasMembershipAccess(player.getUniqueId())) {
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
    }

}
