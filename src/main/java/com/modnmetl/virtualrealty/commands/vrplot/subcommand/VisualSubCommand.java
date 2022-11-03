package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.multiversion.ChatMessage;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class VisualSubCommand extends SubCommand {

    public VisualSubCommand() {}

    public VisualSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPlayer();
        assertPermission();
        Player player = ((Player) sender);
        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if (plot == null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notStandingOnPlot);
            return;
        }
        if (GridStructure.isCuboidGridDisplaying(player, plot.getID())) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().visualBoundaryActive);
            return;
        }
        GridStructure previewStructure = new GridStructure(
                ((Player) sender),
                plot.getLength(),
                plot.getHeight(),
                plot.getWidth(),
                plot.getID(),
                ((Player) sender).getWorld(),
                GridStructure.DISPLAY_TICKS,
                plot.getCreatedLocation()
        );
        previewStructure.preview(player.getLocation(), true, false);
        ChatMessage.of(VirtualRealty.getMessages().visualBoundaryDisplayed).sendWithPrefix(sender);
    }


}
