package com.modnmetl.virtualrealty.listeners.stake;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.plot.subcommand.StakeSubCommand;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.PlotItem;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.multiversion.Chat;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.AbstractMap;

public class StakeConfirmationListener extends VirtualListener {

    public StakeConfirmationListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (StakeSubCommand.stakeConfirmations.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            Player player = e.getPlayer();
            GridStructure gridStructure = DraftListener.DRAFT_MAP.get(player).getKey();
            PlotItem plotItem =  DraftListener.DRAFT_MAP.get(player).getValue().getKey();
            PlotSize plotSize = plotItem.getPlotSize();
            ItemStack plotItemStack =  DraftListener.DRAFT_MAP.get(player).getValue().getValue().getItemStack();
            NBTItem item = new NBTItem(plotItemStack);
            gridStructure.removeGrid();
            if (e.getMessage().equalsIgnoreCase("YES")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
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
                        plot.setOwnedBy(player.getUniqueId());
                        if (plotItem.getAdditionalDays() == 0) {
                            plot.setOwnedUntilDate(Plot.MAX_DATE);
                        } else {
                            plot.setOwnedUntilDate(LocalDateTime.now().plusDays(plotItem.getAdditionalDays()));
                        }
                        player.getInventory().remove(plotItemStack);
                        long timeEnd = System.currentTimeMillis();
                        BaseComponent textComponent = new TextComponent(VirtualRealty.PREFIX + VirtualRealty.getMessages().creationPlotComponent1);
                        BaseComponent textComponent2 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent2.replaceAll("%plot_id%", String.valueOf(plot.getID())));
                        BaseComponent textComponent3 = new TextComponent(VirtualRealty.getMessages().creationPlotComponent3.replaceAll("%creation_time%", String.valueOf(timeEnd - timeStart)));
                        textComponent2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(VirtualRealty.getMessages().clickToShowDetailedInfo.replaceAll("%plot_id%", String.valueOf(plot.getID())))}));
                        textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vrplot info " + plot.getID()));
                        textComponent.addExtra(textComponent2);
                        textComponent.addExtra(textComponent3);
                        new Chat(textComponent).sendTo(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                gridStructure.setCuboidId(plot.getID());
                                gridStructure.setDisplayTicks(20 * 6);
                                gridStructure.preview(true, false);
                            }
                        }.runTaskLater(VirtualRealty.getInstance(), 20);
                    }
                }.runTaskLater(VirtualRealty.getInstance(), 10);
            } else {
                player.getInventory().removeItem(DraftListener.DRAFT_MAP.get(player).getValue().getValue().getItemStack());
                player.getInventory().remove(DraftListener.DRAFT_MAP.get(player).getValue().getValue().getItemStack());
                player.getInventory().addItem(DraftListener.DRAFT_MAP.get(player).getValue().getKey().getItemStack());
                DraftListener.DRAFT_MAP.get(player).getKey().removeGrid();
                DraftListener.DRAFT_MAP.remove(player);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().stakeCancelled);
            }
            DraftListener.DRAFT_MAP.remove(player);
        }
    }


}
