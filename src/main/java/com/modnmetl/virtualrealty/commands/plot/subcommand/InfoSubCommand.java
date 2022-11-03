package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.multiversion.ChatMessage;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class InfoSubCommand extends SubCommand {

    public InfoSubCommand() {}

    public InfoSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if (plot == null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notStandingOnPlot);
            return;
        }
        printInfo(sender, plot);
    }

    private void printInfo(CommandSender sender, Plot plot) {
        LocalDateTime localDateTime = plot.getOwnedUntilDate();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        ChatMessage.of(" ").send(sender);
        ChatMessage.of(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»").send(sender);
        ChatMessage.of(" §7Plot ID §8§l‣ §f" + plot.getID()).send(sender);
        ChatMessage.of(" §7Owned By §8§l‣ §a" + (plot.getOwnedBy() != null ? (Bukkit.getOfflinePlayer(plot.getOwnedBy()).isOnline() ? "§a" : "§c") + Bukkit.getOfflinePlayer(plot.getOwnedBy()).getName() : "§cAvailable")).send(sender);
        if (plot.getMembers().size() != 0) {
            ChatMessage.of(" §7Members §8§l↴").send(sender);
            for (OfflinePlayer offlinePlayer : plot.getPlayerMembers()) {
                ChatMessage.of(" §8§l⁍ §" + (offlinePlayer.isOnline() ? "a" : "c") + offlinePlayer.getName()).send(sender);
            }
        }
        ChatMessage.of(" §7Owned Until §8§l‣ §f" + dateTimeFormatter.format(localDateTime)).send(sender);
        ChatMessage.of(" §7Size §8§l‣ §f" + plot.getPlotSize()).send(sender);
        ChatMessage.of(" §7Length §8§l‣ §f" + plot.getLength()).send(sender);
        ChatMessage.of(" §7Height §8§l‣ §f" + plot.getHeight()).send(sender);
        ChatMessage.of(" §7Width §8§l‣ §f" + plot.getWidth()).send(sender);
    }

}
