package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.ConfirmationType;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.ConfirmationManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.Confirmation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class RemoveSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot remove §8<§7plot§8>");
    }

    public RemoveSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }

    public RemoveSubCommand(CommandSender sender, Command command, String label, String[] args, boolean bypass) throws FailedCommandException {
        super(sender, command, label, args, bypass, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        if (args.length < 2) {
            printHelp();
            return;
        }
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
        if (sender instanceof Player) {
            if (this.isBypass()) {
                plot.remove(sender);
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().removedPlot);
            } else {
                for (String s : VirtualRealty.getMessages().removeConfirmation) {
                    sender.sendMessage(VirtualRealty.PREFIX + s.replaceAll("%plot_id%", String.valueOf(plot.getID())));
                }
                Confirmation confirmation = new Confirmation(ConfirmationType.REMOVE, (Player) sender, plot.getID(), "YES") {
                    @Override
                    public void success() {
                        plot.remove(((Player) sender).getKiller());
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().removedPlot);
                        ConfirmationManager.removeConfirmations(plot.getID(), this.getConfirmationType());
                    }

                    @Override
                    public void failed() {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().removalCancelled);
                        ConfirmationManager.removeConfirmations(plot.getID(), this.getConfirmationType());
                    }

                    @Override
                    public void expiry() {
                        ConfirmationManager.removeConfirmations(plot.getID(), this.getConfirmationType());
                    }
                };
                ConfirmationManager.confirmations.add(confirmation);
            }
        } else {
            plot.remove(sender);
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().removedPlot);
        }
    }

}
