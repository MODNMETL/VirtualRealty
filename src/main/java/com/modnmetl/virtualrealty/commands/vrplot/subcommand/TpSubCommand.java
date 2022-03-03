package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandExecution;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.PermissionUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public class TpSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot tp §8<§7plot§8>");
    }

    public TpSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        assertPlayer();
        if (!PermissionUtil.hasPermission(sender, COMMAND_PERMISSION.getName() + args[0].toLowerCase())) return;
        if (args.length < 2) {
            printHelp();
            return;
        }
        Player player = ((Player) sender);
        int plotID;
        try {
            plotID = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
            return;
        }
        Plot plot = PlotManager.getPlot(plotID);
        if (plot == null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
            return;
        }
        Location loc = new Location(plot.getCreatedLocation().getWorld(), plot.getCenter().getBlockX(), plot.getCenter().getBlockY() + 1, plot.getCenter().getBlockZ());
        loc.setY(loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getY() + 1);
        player.teleport(loc);
        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().teleportedToPlot);
    }

}
