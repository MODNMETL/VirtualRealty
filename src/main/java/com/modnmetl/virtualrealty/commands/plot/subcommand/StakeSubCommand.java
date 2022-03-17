package com.modnmetl.virtualrealty.commands.plot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.ConfirmationType;
import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.listeners.stake.DraftListener;
import com.modnmetl.virtualrealty.managers.ConfirmationManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.Confirmation;
import com.modnmetl.virtualrealty.objects.data.PlotItem;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.RegionUtil;
import com.modnmetl.virtualrealty.utils.multiversion.Chat;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.*;

public class StakeSubCommand extends SubCommand {

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
        for (String s : VirtualRealty.getMessages().stakeConfirmation) {
            sender.sendMessage(VirtualRealty.PREFIX + s);
        }
        Confirmation confirmation = new Confirmation(ConfirmationType.STAKE, (Player) sender, "YES") {
            @Override
            public void success() {
                GridStructure gridStructure = DraftListener.DRAFT_MAP.get(this.getSender()).getKey();
                PlotItem plotItem = DraftListener.DRAFT_MAP.get(this.getSender()).getValue().getKey();
                PlotSize plotSize = plotItem.getPlotSize();
                ItemStack plotItemStack = DraftListener.DRAFT_MAP.get(this.getSender()).getValue().getValue().getItemStack();
                NBTItem item = new NBTItem(plotItemStack);
                gridStructure.removeGrid();
                this.getSender().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
                long timeStart = System.currentTimeMillis();
                Plot plot = PlotManager.createPlot(gridStructure.getPreviewLocation().subtract(0, 1, 0), plotSize, plotItem.getLength(), plotItem.getHeight(), plotItem.getWidth(), plotItem.isNatural());
                AbstractMap.SimpleEntry<String, Byte> floorData = new AbstractMap.SimpleEntry<>(item.getString("vrplot_floor_material"), item.getByte("vrplot_floor_data"));
                AbstractMap.SimpleEntry<String, Byte> borderData = new AbstractMap.SimpleEntry<>(item.getString("vrplot_border_material"), item.getByte("vrplot_border_data"));
                if (!plotItem.isNatural()) {
                    if (VirtualRealty.legacyVersion) {
                        plot.setFloorMaterial(Material.valueOf(floorData.getKey()), floorData.getValue());
                        plot.setBorderMaterial(Material.valueOf(borderData.getKey()), borderData.getValue());
                    } else {
                        plot.setFloorMaterial(Bukkit.createBlockData(floorData.getKey()).getMaterial(), floorData.getValue());
                        plot.setBorderMaterial(Bukkit.createBlockData(borderData.getKey()).getMaterial(), borderData.getValue());
                    }
                }
                plot.setOwnedBy(this.getSender().getUniqueId());
                if (plotItem.getAdditionalDays() == 0) {
                    plot.setOwnedUntilDate(Plot.MAX_DATE);
                } else {
                    plot.setOwnedUntilDate(LocalDateTime.now().plusDays(plotItem.getAdditionalDays()));
                }
                this.getSender().getInventory().remove(plotItemStack);
                long timeEnd = System.currentTimeMillis();
                BaseComponent textComponent = new TextComponent(VirtualRealty.PREFIX + VirtualRealty.getMessages().creationPlotComponent1);
                BaseComponent textComponent2 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent2.replaceAll("%plot_id%", String.valueOf(plot.getID())));
                BaseComponent textComponent3 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent3.replaceAll("%creation_time%", String.valueOf(timeEnd - timeStart)));
                textComponent2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(VirtualRealty.getMessages().clickToShowDetailedInfo.replaceAll("%plot_id%", String.valueOf(plot.getID())))}));
                textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vrplot info " + plot.getID()));
                textComponent.addExtra(textComponent2);
                textComponent.addExtra(textComponent3);
                new Chat(textComponent).sendTo(this.getSender());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        gridStructure.setCuboidId(plot.getID());
                        gridStructure.setDisplayTicks(20 * 6);
                        gridStructure.preview(true, false);
                    }
                }.runTaskLater(VirtualRealty.getInstance(), 20);
                DraftListener.DRAFT_MAP.remove(this.getSender());
                ConfirmationManager.removeStakeConfirmations(this.getConfirmationType(), this.getSender().getUniqueId());
            }

            @Override
            public void failed() {
                this.getSender().getInventory().removeItem(DraftListener.DRAFT_MAP.get(this.getSender()).getValue().getValue().getItemStack());
                this.getSender().getInventory().remove(DraftListener.DRAFT_MAP.get(this.getSender()).getValue().getValue().getItemStack());
                this.getSender().getInventory().addItem(DraftListener.DRAFT_MAP.get(this.getSender()).getValue().getKey().getItemStack());
                DraftListener.DRAFT_MAP.get(this.getSender()).getKey().removeGrid();
                DraftListener.DRAFT_MAP.remove(this.getSender());
                this.getSender().sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().stakeCancelled);
                DraftListener.DRAFT_MAP.remove(this.getSender());
                ConfirmationManager.removeStakeConfirmations(this.getConfirmationType(), this.getSender().getUniqueId());
            }

            @Override
            public void expiry() {
                ConfirmationManager.removeStakeConfirmations(this.getConfirmationType(), this.getSender().getUniqueId());
            }
        };
        ConfirmationManager.confirmations.add(confirmation);
    }

}
