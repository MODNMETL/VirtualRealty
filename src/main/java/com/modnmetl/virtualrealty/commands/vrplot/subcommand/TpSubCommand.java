package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class TpSubCommand extends SubCommand {

    public static LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot %command% §8<§7plot§8>");
    }

    public TpSubCommand() {}

    public TpSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPlayer();
        assertPermission();
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
        Plot plot = PlotManager.getInstance().getPlot(plotID);
        if (plot == null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
            return;
        }
        plot.teleportPlayer(player);
        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().teleportedToPlot);
    }

}
