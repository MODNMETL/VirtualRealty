package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.permissions.ManagementPermission;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.PlotMember;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class AddSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/plot add §8<§7plot§8> §8<§7player§8>");
    }

    public AddSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        if (args.length < 3) {
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
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        if (offlinePlayer.getFirstPlayed() == 0) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerNotFoundWithUsername);
            return;
        }
        Plot plot = PlotManager.getPlot(plotID);
        if (plot == null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noPlotFound);
            return;
        }
        if (!plot.hasMembershipAccess(player.getUniqueId())) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notYourPlot);
            return;
        }
        PlotMember plotMember = plot.getMember(player.getUniqueId());
        if (plotMember != null) {
            if (!plotMember.hasManagementPermission(ManagementPermission.ADD_MEMBER)) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noAccess);
                return;
            }
        } else {
            if (plot.getPlotOwner().getUniqueId() != player.getUniqueId()) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noAccess);
                return;
            }
        }
        if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
            return;
        }
        if (plot.getOwnedBy().equals(offlinePlayer.getUniqueId())) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantAddYourself);
            return;
        }
        if (plot.getMember(offlinePlayer.getUniqueId()) != null) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().alreadyInMembers);
            return;
        }
        plot.addMember(offlinePlayer.getUniqueId());
        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().playerAdd.replaceAll("%player%", offlinePlayer.getName()));
    }
    
}
