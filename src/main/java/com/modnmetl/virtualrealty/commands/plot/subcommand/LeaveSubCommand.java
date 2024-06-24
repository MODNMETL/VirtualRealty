package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exception.FailedCommandException;
import com.modnmetl.virtualrealty.manager.ConfirmationManager;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.other.ChatMessage;
import com.modnmetl.virtualrealty.model.other.CommandType;
import com.modnmetl.virtualrealty.model.other.Confirmation;
import com.modnmetl.virtualrealty.model.other.ConfirmationType;
import com.modnmetl.virtualrealty.model.permission.ManagementPermission;
import com.modnmetl.virtualrealty.model.plot.Plot;
import com.modnmetl.virtualrealty.model.plot.PlotMember;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class LeaveSubCommand extends SubCommand {

    public static LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/plot %command% §8<§7plot§8>");
    }

    public LeaveSubCommand() {}

    public LeaveSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        if (args.length < 2) {
            printHelp(CommandType.PLOT);
            return;
        }
        int plotID;
        try {
            plotID = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            ChatMessage.of(VirtualRealty.getMessages().useNaturalNumbersOnly).sendWithPrefix(sender);
            return;
        }
        Plot plot = PlotManager.getInstance().getPlot(plotID);
        if (plot == null) {
            ChatMessage.of(VirtualRealty.getMessages().noPlotFound).sendWithPrefix(sender);
            return;
        }
        if (!plot.hasMembershipAccess(player.getUniqueId())) {
            ChatMessage.of(VirtualRealty.getMessages().notYourPlot).sendWithPrefix(sender);
            return;
        }
        PlotMember plotMember = plot.getMember(player.getUniqueId());
        if (plot.getOwnedBy().equals(player.getUniqueId())) {
            for (String s : VirtualRealty.getMessages().leaveConfirmation) {
                player.sendMessage(VirtualRealty.PREFIX + s.replaceAll("%plot_id%", String.valueOf(plotID)));
            }
            Confirmation confirmation = new Confirmation(ConfirmationType.PLOT_OWNER_LEAVE, player, "YES") {
                @Override
                public void success() {
                    ChatMessage.of(VirtualRealty.getMessages().plotLeave.replaceAll("%plot_id%", String.valueOf(plotID)))
                            .sendWithPrefix(sender);
                    plot.removeAllMembers();
                    plot.setAssignedBy(null);
                    plot.setOwnedBy(null);
                    plot.update();
                    ConfirmationManager.removeConfirmations(this.getConfirmationType());
                }
                @Override
                public void failed() {
                    ChatMessage.of(VirtualRealty.getMessages().leaveConfirmationCancelled.replaceAll("%plot_id%", String.valueOf(plotID)))
                            .sendWithPrefix(sender);
                    ConfirmationManager.removeConfirmations(this.getConfirmationType());
                }
                @Override
                public void expiry() {
                    ChatMessage.of(VirtualRealty.getMessages().confirmationExpired.replaceAll("%plot_id%", String.valueOf(plotID)))
                            .sendWithPrefix(sender);
                    ConfirmationManager.removeConfirmations(this.getConfirmationType());
                }
            };
            ConfirmationManager.addConfirmation(confirmation);
        } else {
            plot.removeMember(plotMember);
            ChatMessage.of(VirtualRealty.getMessages().plotLeave.replaceAll("%plot_id%", String.valueOf(plot.getID()))).sendWithPrefix(sender);
        }
    }

}
