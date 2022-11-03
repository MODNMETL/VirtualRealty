package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.UUIDUtils;
import com.modnmetl.virtualrealty.utils.multiversion.ChatMessage;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.UUID;

public class AssignSubCommand extends SubCommand {

    public static LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot %command% §8<§7plot§8> §8<§7username§8>");
    }

    public AssignSubCommand() {}

    public AssignSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }
    
    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        if (args.length < 2) {
            printHelp();
            return;
        }
        int plotID;
        OfflinePlayer offlinePlayer;
        try {
            plotID = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            ChatMessage.of(VirtualRealty.getMessages().useNaturalNumbersOnly).sendWithPrefix(sender);
            return;
        }
        try {
            if (UUIDUtils.isValidUUID(args[2])) {
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[2]));
            } else {
                offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
            }
            if (offlinePlayer.getName() == null) {
                ChatMessage.of(VirtualRealty.getMessages().playerNotFoundWithUsername).sendWithPrefix(sender);
                return;
            }
        } catch (NullPointerException e) {
            ChatMessage.of(VirtualRealty.getMessages().playerNotFoundWithUsername).sendWithPrefix(sender);
            return;
        }
        Plot plot = PlotManager.getInstance().getPlot(plotID);
        if (plot == null) {
            ChatMessage.of(VirtualRealty.getMessages().noPlotFound).sendWithPrefix(sender);
            return;
        }
        if (sender instanceof Player) {
            plot.setAssignedBy(((Player) sender).getUniqueId().toString());
        } else if (sender instanceof ConsoleCommandSender) {
            plot.setAssignedBy("CONSOLE");
        } else {
            plot.setAssignedBy("SHOP_PURCHASE");
        }
        plot.setOwnedBy(offlinePlayer.getUniqueId());
        String text = VirtualRealty.getMessages().assignedToBy.replaceAll("%assigned_to%", offlinePlayer.getName()).replaceAll("%assigned_by%", sender.getName());
        ChatMessage.of(text).sendWithPrefix(sender);
        plot.update();
    }
    
}
