package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exception.FailedCommandException;
import com.modnmetl.virtualrealty.manager.DynmapManager;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.plot.Plot;
import com.modnmetl.virtualrealty.model.other.ChatMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand() {}

    public ReloadSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        try {
            VirtualRealty.getInstance().reloadConfigs();
            if (VirtualRealty.getDynmapManager() != null) {
                if (VirtualRealty.getPluginConfiguration().dynmapMarkers) {
                    if (VirtualRealty.getDynmapManager().markerset != null) {
                        VirtualRealty.getDynmapManager().markerset.deleteMarkerSet();
                    }
                    VirtualRealty.getDynmapManager().registerDynmap();
                    for (Plot plot : PlotManager.getInstance().getPlots()) {
                        DynmapManager.resetPlotMarker(plot);
                    }
                } else {
                    if (VirtualRealty.getDynmapManager().markerset != null) {
                        VirtualRealty.getDynmapManager().markerset.deleteMarkerSet();
                    }
                }
            }
            PlotManager.getInstance().loadPlots();
            VirtualRealty.getInstance().loadSizesConfiguration();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        ChatMessage.of(VirtualRealty.getMessages().reloadComplete).sendWithPrefix(sender);
    }

}
