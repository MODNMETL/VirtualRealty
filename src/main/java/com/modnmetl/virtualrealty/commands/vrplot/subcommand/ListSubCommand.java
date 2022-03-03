package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandExecution;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.PermissionUtil;
import com.modnmetl.virtualrealty.utils.multiversion.Chat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public class ListSubCommand extends SubCommand {

    public ListSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        if (!PermissionUtil.hasPermission(sender, COMMAND_PERMISSION.getName() + args[0].toLowerCase())) return;
        if (PlotManager.getPlots().isEmpty()) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlots);
            return;
        }
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        sender.sendMessage(" ");
        sender.sendMessage("§7§m                                                                                ");
        sender.sendMessage("§7|  §a§l§oID§7  |  §a§l§oOwned By§7 |  §a§l§oOwned Until§7 |  §a§l§oSize§7 |  §a§l§oPlot Center§7  |");
        for (Plot plot : PlotManager.getPlots()) {
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
    }

}
