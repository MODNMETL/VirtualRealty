package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandExecution;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution {
        if (!PermissionUtil.hasPermission(sender, COMMAND_PERMISSION.getName() + args[0].toLowerCase())) return;
        try {
            VirtualRealty.getInstance().reloadConfigs();
            if (VirtualRealty.getPluginConfiguration().dynmapMarkers) {
                if (VirtualRealty.markerset != null) {
                    VirtualRealty.markerset.deleteMarkerSet();
                }
                VirtualRealty.getInstance().registerDynmap();
                for (Plot plot : PlotManager.getPlots()) {
                    PlotManager.resetPlotMarker(plot);
                }
            } else {
                if (VirtualRealty.markerset != null) {
                    VirtualRealty.markerset.deleteMarkerSet();
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
