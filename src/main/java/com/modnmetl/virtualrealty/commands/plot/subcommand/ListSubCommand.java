package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exception.FailedCommandException;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.plot.Plot;
import com.modnmetl.virtualrealty.model.other.ChatMessage;
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

    public ListSubCommand() {}

    public ListSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        boolean hasPlot = false;
        for (Plot plot : PlotManager.getInstance().getPlots()) {
            if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(player.getUniqueId()) && plot.getOwnedUntilDate().isAfter(LocalDateTime.now())) {
                hasPlot = true;
                break;
            }
        }
        boolean isMember = false;
        for (Plot plot : PlotManager.getInstance().getPlots()) {
            if (plot.getMember(player.getUniqueId()) != null) {
                isMember = true;
                break;
            }
        }
        if (!hasPlot && !isMember) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlayerPlotsFound);
            return;
        }
        ChatMessage.of(" ").send(sender);
        ChatMessage.of(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»").send(sender);
        ChatMessage.of(" ").send(sender);
        if (hasPlot) {
            ChatMessage.of("§7§m                                                                                ").send(sender);
            ChatMessage.of("§7|  §a§l§oID§7  |  §a§l§oOwned Until§7 |  §a§l§oSize§7  |  §a§l§oPlot Center§7  |").send(sender);
            for (Plot plot : PlotManager.getInstance().getPlots()) {
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
                    ChatMessage.of(textComponent.toLegacyText()).send(sender);
                }
            }
            ChatMessage.of("§7§m                                                                                ").send(sender);
        }
        if (isMember) {
            ChatMessage.of(" ").send(sender);
            ChatMessage.of("§7                            §fMember of §8§l↴").send(sender);
            ChatMessage.of(" ").send(sender);
            ChatMessage.of("§7§m                                                                                ").send(sender);
            ChatMessage.of("§7|  §a§l§oID§7  |  §a§l§oOwned By§7 |  §a§l§oSize§7  |  §a§l§oPlot Center§7  |").send(sender);
            for (Plot plot : PlotManager.getInstance().getPlots()) {
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
                    ChatMessage.of(textComponent.toLegacyText()).send(sender);
                }
            }
            ChatMessage.of("§7§m                                                                                ").send(sender);
        }

    }

}
