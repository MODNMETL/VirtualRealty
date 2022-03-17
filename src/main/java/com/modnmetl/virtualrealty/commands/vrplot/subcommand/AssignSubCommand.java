package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.UUID;

public class AssignSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot assign §8<§7plot§8> §8<§7username§8>");
    }

    public AssignSubCommand(CommandSender sender, Command command, String label, String[] args, boolean bypass) throws FailedCommandException {
        super(sender, command, label, args, bypass, HELP);
    }
    
    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        if (args.length < 2) {
            for (String helpMessage : HELP) {
                sender.sendMessage(helpMessage);
            }
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
            if (UUIDUtils.isValidUUID(args[2])) {
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[2]));
            } else {
                offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
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
        if (sender instanceof Player) {
            plot.setAssignedBy(((Player) sender).getUniqueId().toString());
        } else if (sender instanceof ConsoleCommandSender) {
            plot.setAssignedBy("CONSOLE");
        } else {
            plot.setAssignedBy("SHOP_PURCHASE");
        }
        plot.setOwnedBy(offlinePlayer.getUniqueId());
        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().assignedToBy.replaceAll("%assigned_to%", offlinePlayer.getName()).replaceAll("%assigned_by%", sender.getName()));
        plot.update();
    }
    
}
