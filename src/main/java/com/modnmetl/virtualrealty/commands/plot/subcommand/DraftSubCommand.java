package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.ConfirmationType;
import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.enums.items.VItem;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.listeners.stake.DraftListener;
import com.modnmetl.virtualrealty.managers.ConfirmationManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.PlotItem;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.RegionUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.AbstractMap;
import java.util.LinkedList;

public class DraftSubCommand extends SubCommand {

    public DraftSubCommand() {}

    public DraftSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        assertPlayer();
        Player player = ((Player) sender);
        if (DraftListener.DRAFT_MAP.containsKey(player)) {
            player.getInventory().remove(DraftListener.DRAFT_MAP.get(player).getValue().getValue().getItemStack());
            player.getInventory().addItem(DraftListener.DRAFT_MAP.get(player).getValue().getKey().getItemStack());
            DraftListener.DRAFT_MAP.get(player).getKey().removeGrid();
            DraftListener.DRAFT_MAP.remove(player);
            ConfirmationManager.removeStakeConfirmations(ConfirmationType.STAKE, player.getUniqueId());
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().draftModeDisabled);
            return;
        }
        PlayerInventory inv;
        ItemStack claimItem;
        if (VirtualRealty.legacyVersion) {
            inv = player.getInventory();
            claimItem = player.getItemInHand();
        } else {
            inv = player.getInventory();
            claimItem = inv.getItemInMainHand();
        }
        NBTItem claimNbtItem;
        if (!(claimItem.getType() == (VirtualRealty.legacyVersion ? Material.valueOf("SKULL_ITEM") : Material.PLAYER_HEAD)
                &&
                (claimNbtItem = new NBTItem(claimItem)).getString("vrplot_item") != null && claimNbtItem.getString("vrplot_item").equals("CLAIM"))) {
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notHoldingPlotClaim);
            return;
        }
        PlotItem plotItem = PlotItem.fromItemStack(claimItem);
        Plot plot = PlotManager.getPlot(player.getLocation());
        String replacement = null;
        if (plot == null) {
            replacement = VirtualRealty.getMessages().createFeature;
        } else {
            if (plotItem.getPlotSize().equals(plot.getPlotSize())) {
                if (((plot.isOwnershipExpired() && plot.getPlotOwner() != null && !plot.getPlotOwner().getUniqueId().equals(player.getUniqueId())) || plot.getPlotOwner() == null)) {
                    replacement = VirtualRealty.getMessages().claimFeature;
                } else if (plot.getPlotOwner() != null && plot.getPlotOwner().getUniqueId().equals(player.getUniqueId())) {
                    replacement = VirtualRealty.getMessages().extendFeature;
                }
            } else {
                replacement = VirtualRealty.getMessages().createFeature;
            }
        }
        String finalReplacement = replacement;
        if (plot != null && plotItem.getPlotSize().equals(plot.getPlotSize())) {
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().standingOnPlot);
            GridStructure previewStructure = new GridStructure(((Player) sender), plot.getLength(), plot.getHeight(), plot.getWidth(), plot.getID(), ((Player) sender).getWorld(), 0, plot.getCreatedLocation());
            previewStructure.preview(player.getLocation(), true, false);
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().visualBoundaryDisplayed);
            PlotItem draftItem = PlotItem.fromItemStack(claimItem, VItem.DRAFT);
            DraftListener.DRAFT_MAP.put(player, new AbstractMap.SimpleEntry<>(previewStructure, new AbstractMap.SimpleEntry<>(plotItem, draftItem)));
            inv.remove(claimItem);
            if (VirtualRealty.legacyVersion) {
                player.setItemInHand(draftItem.getItemStack());
            } else {
                inv.setItemInMainHand(draftItem.getItemStack());
            }
            VirtualRealty.getMessages().draftEnabled.forEach((message) -> player.sendMessage(message.replaceAll("&", "§")
                    .replaceAll("%feature%", finalReplacement)
            ));
            return;
        }
        PlotSize plotSize = PlotSize.valueOf(claimNbtItem.getString("vrplot_size"));
        Cuboid cuboid = RegionUtil.getRegion(player.getLocation(), Direction.byYaw(player.getLocation().getYaw()), plotSize.getLength(), plotSize.getHeight(), plotSize.getWidth());
        if (RegionUtil.isCollidingWithAnotherPlot(cuboid)) {
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().draftModeCancelledCollision);
            if (!GridStructure.isCuboidGridDisplaying(player, 0)) {
                new GridStructure(player, plotSize.getLength(), plotSize.getHeight(), plotSize.getWidth(), 0, ((Player) sender).getWorld(), 20 * 6, player.getLocation()).preview(player.getLocation(),true, true);
            }
            return;
        }
        if (RegionUtil.isCollidingWithBedrock(cuboid)) {
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().draftModeCancelledBedrock);
            GridStructure.isCuboidGridDisplaying(player, 0);
            if (!GridStructure.isCuboidGridDisplaying(player, 0)) {
                new GridStructure(player, plotSize.getLength(), plotSize.getHeight(), plotSize.getWidth(), 0, ((Player) sender).getWorld(), 20 * 6, player.getLocation()).preview(player.getLocation(),true, true);
            }
            return;
        }
        PlotItem draftItem = PlotItem.fromItemStack(claimItem, VItem.DRAFT);
        GridStructure draftStructure = new GridStructure(player, plotItem.getLength(), plotItem.getHeight(), plotItem.getWidth(), 0, ((Player) sender).getWorld(), 0, player.getLocation());
        DraftListener.DRAFT_MAP.put(player, new AbstractMap.SimpleEntry<>(draftStructure, new AbstractMap.SimpleEntry<>(plotItem, draftItem)));
        inv.remove(claimItem);
        if (VirtualRealty.legacyVersion) {
            player.setItemInHand(draftItem.getItemStack());
        } else {
            inv.setItemInMainHand(draftItem.getItemStack());
        }
        draftStructure.preview(player.getLocation(), true, false);
        VirtualRealty.getMessages().draftEnabled.forEach((message) -> player.sendMessage(message.replaceAll("&", "§")
                .replaceAll("%feature%", finalReplacement)
        ));
    }

}
