package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.listeners.stake.DraftListener;
import com.modnmetl.virtualrealty.objects.data.PlotItem;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.RegionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class StakeSubCommand extends SubCommand {

    public static Set<UUID> stakeConfirmations = new HashSet<>();

    public StakeSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        if (!DraftListener.DRAFT_MAP.containsKey(player)) {
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noDraftClaimEnabled);
            return;
        }
        GridStructure gridStructure = DraftListener.DRAFT_MAP.get(player).getKey();
        PlotItem plotItem =  DraftListener.DRAFT_MAP.get(player).getValue().getKey();
        Cuboid cuboid = RegionUtil.getRegion(gridStructure.getPreviewLocation(), Direction.byYaw(gridStructure.getPreviewLocation().getYaw()), plotItem.getLength(), plotItem.getHeight(), plotItem.getWidth());
        if (RegionUtil.isCollidingWithAnotherPlot(cuboid)) {
            player.getInventory().remove(DraftListener.DRAFT_MAP.get(player).getValue().getValue().getItemStack());
            player.getInventory().addItem(DraftListener.DRAFT_MAP.get(player).getValue().getKey().getItemStack());
            DraftListener.DRAFT_MAP.get(player).getKey().removeGrid();
            DraftListener.DRAFT_MAP.remove(player);
            gridStructure.removeGrid();
            gridStructure.setDisplayTicks(20L * 6);
            gridStructure.preview(true, true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().draftModeCancelledCollision);
            return;
        }
        stakeConfirmations.add(player.getUniqueId());
        for (String s : VirtualRealty.getMessages().stakeConfirmation) {
            sender.sendMessage(VirtualRealty.PREFIX + s);
        }
    }

}
