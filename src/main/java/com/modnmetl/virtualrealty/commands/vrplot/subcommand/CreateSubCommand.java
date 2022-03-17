package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.RegionUtil;
import com.modnmetl.virtualrealty.utils.multiversion.Chat;
import com.modnmetl.virtualrealty.utils.multiversion.VMaterial;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CreateSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot create §8<§7small/medium/large§8> §8<§7floor (optional)§8> §8<§7border (optional)§8> §8<§7--natural(optional)§8>");
        HELP.add(" §a/vrplot create area §8<§7length§8> §8<§7height§8> §8<§7width§8>");
        HELP.add(" §a/vrplot create §8<§7length§8> §8<§7height§8> §8<§7width§8> §8<§7floor (optional)§8> §8<§7border (optional)§8> §8<§7--natural(optional)§8>");
    }

    public CreateSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPlayer();
        assertPermission();
        if (args.length < 2) {
            printHelp();
            return;
        }
        Player player = ((Player) sender);
        Location location = player.getLocation();
        location.add(0, -1, 0);
        if (Arrays.stream(PlotSize.values()).anyMatch(plotSize -> plotSize.name().equalsIgnoreCase(args[1])) && !args[1].equalsIgnoreCase(PlotSize.CUSTOM.name())) {
            PlotSize plotSize = null;
            try {
                plotSize = PlotSize.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            if (plotSize != null) {
                if (plotSize == PlotSize.AREA) {
                    int length = plotSize.getLength();
                    int height = plotSize.getHeight();
                    int width = plotSize.getWidth();
                    if (args.length > 2) {
                        try {
                            length = Integer.parseInt(args[2]);
                            height = Integer.parseInt(args[3]);
                            width = Integer.parseInt(args[4]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                            return;
                        }
                    }
                    if (length < 1 || width < 1 || height < 1) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().graterThenZero);
                        return;
                    }
                    Cuboid cuboid = RegionUtil.getRegion(location, Direction.byYaw(location.getYaw()), length, height, width);
                    if (RegionUtil.isCollidingWithAnotherPlot(cuboid)) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantCreateOnExisting);
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
                        long timeStart = System.currentTimeMillis();
                        Plot plot = PlotManager.createPlot(location, PlotSize.AREA, length, height, width, true);
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
                                new GridStructure(player, plot.getLength(), plot.getHeight(), plot.getWidth(), plot.getID(), ((Player) sender).getWorld(), 20 * 6, plot.getCreatedLocation()).preview(true, false);
                            }
                        }.runTaskLater(VirtualRealty.getInstance(), 20);
                    }
                } else {
                    Cuboid cuboid = RegionUtil.getRegion(location, Direction.byYaw(location.getYaw()), plotSize.getLength(), plotSize.getHeight(), plotSize.getWidth());
                    if (RegionUtil.isCollidingWithAnotherPlot(cuboid)) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantCreateOnExisting);
                    } else {
                        boolean natural = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--natural"));
                        Material floorMaterial = null;
                        byte floorData = 0;
                        Material borderMaterial = null;
                        byte borderData = 0;
                        if (args.length >= 3 && !natural) {
                            try {
                                floorMaterial = VMaterial.getMaterial(args[2].split(":")[0].toUpperCase());
                                if (floorMaterial == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                                    return;
                                }
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                                return;
                            }
                            if (args[2].split(":").length == 2) {
                                floorData = Byte.parseByte(args[2].split(":")[1]);
                            }
                        }
                        if (args.length >= 4) {
                            try {
                                borderMaterial = VMaterial.getMaterial(args[3].split(":")[0].toUpperCase());
                                if (borderMaterial == null) {
                                    sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                                    return;
                                }
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                                return;
                            }
                            if (args[3].split(":").length == 2) {
                                borderData = Byte.parseByte(args[3].split(":")[1]);
                            }
                        }
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
                        long timeStart = System.currentTimeMillis();
                        Plot plot = PlotManager.createPlot(location, plotSize, plotSize.getLength(), plotSize.getHeight(), plotSize.getWidth(), natural);
                        if (!natural) {
                            if (floorMaterial != null) {
                                plot.setFloorMaterial(floorMaterial, floorData);
                            }
                            if (borderMaterial != null) {
                                plot.setBorderMaterial(borderMaterial, borderData);
                            }
                        }
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
                                new GridStructure(player, plot.getPlotSize().getLength(), plot.getPlotSize().getHeight(), plot.getPlotSize().getWidth(), plot.getID(), ((Player) sender).getWorld(), 20 * 6, plot.getCreatedLocation()).preview(true, false);
                            }
                        }.runTaskLater(VirtualRealty.getInstance(), 20);
                    }
                }
            } else {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().sizeNotRecognised);
            }
        } else {
            int length;
            int height;
            int width;
            try {
                length = Integer.parseInt(args[1]);
                height = Integer.parseInt(args[2]);
                width = Integer.parseInt(args[3]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().useNaturalNumbersOnly);
                return;
            }
            if (length < 1 || width < 1 || height < 1) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().graterThenZero);
                return;
            }
            if (length > 500 || width > 500 || height > 500) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().hardLimit);
                return;
            }
            Cuboid cuboid = RegionUtil.getRegion(location, Direction.byYaw(location.getYaw()), length, height, width);
            if (RegionUtil.isCollidingWithAnotherPlot(cuboid) || RegionUtil.isCollidingWithBedrock(cuboid)) {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantCreateOnExisting);
            } else {
                boolean natural = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--natural"));
                Material floorMaterial = null;
                byte floorData = 0;
                if (args.length >= 5 && !natural) {
                    try {
                        floorMaterial = VMaterial.getMaterial(args[4].split(":")[0].toUpperCase());
                        if (floorMaterial == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetFloorMaterial);
                        return;
                    }
                    if (args[4].split(":").length == 2) {
                        floorData = Byte.parseByte(args[4].split(":")[1]);
                    }
                }
                Material borderMaterial = null;
                byte borderData = 0;
                if (args.length >= 6) {
                    try {
                        borderMaterial = VMaterial.getMaterial(args[5].split(":")[0].toUpperCase());
                        if (borderMaterial == null) {
                            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantGetBorderMaterial);
                        return;
                    }
                    if (args[5].split(":").length == 2) {
                        borderData = Byte.parseByte(args[5].split(":")[1]);
                    }
                }
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().notCollidingCreating);
                long timeStart = System.currentTimeMillis();
                Plot plot = PlotManager.createPlot(location, PlotSize.CUSTOM, length, height, width, natural);
                if (!natural) {
                    if (floorMaterial != null) {
                        plot.setFloorMaterial(floorMaterial, floorData);
                    }
                    if (borderMaterial != null) {
                        plot.setBorderMaterial(borderMaterial, borderData);
                    }
                }
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
                        new GridStructure(player, plot.getLength(), plot.getHeight(), plot.getWidth(), plot.getID(), ((Player) sender).getWorld(), 20 * 6, plot.getCreatedLocation()).preview(true, false);
                    }
                }.runTaskLater(VirtualRealty.getInstance(), 20);
            }
        }
    }


}
