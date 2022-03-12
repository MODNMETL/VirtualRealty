package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.DynmapManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission(COMMAND_PERMISSION.getName() + "." + args[0].toLowerCase());
        try {
            VirtualRealty.getInstance().reloadConfigs();
            if (VirtualRealty.getPluginConfiguration().dynmapMarkers) {
                if (VirtualRealty.getDynmapManager().markerset != null) {
                    VirtualRealty.getDynmapManager().markerset.deleteMarkerSet();
                }
                VirtualRealty.getDynmapManager().registerDynmap();
                for (Plot plot : PlotManager.getPlots()) {
                    DynmapManager.resetPlotMarker(plot);
                }
            } else {
                if (VirtualRealty.getDynmapManager().markerset != null) {
                    VirtualRealty.getDynmapManager().markerset.deleteMarkerSet();
                }
            }
            PlotManager.loadPlots();
            VirtualRealty.getInstance().loadSizesConfiguration();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().reloadComplete);
    }

}
