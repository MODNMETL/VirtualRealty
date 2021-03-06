package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.enums.items.VItem;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.objects.data.PlotItem;
import com.modnmetl.virtualrealty.utils.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ItemSubCommand extends SubCommand {

    public static final LinkedList<String> HELP = new LinkedList<>();

    static {
        HELP.add(" ");
        HELP.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP.add(" §a/vrplot item §8<§7small/medium/large§8> §8<§7floor§8> §8<§7border§8> §8<§7lease duration (in days)§8> §8<§7amount of items§8> §8<§7player§8> §8<§7--natural(optional)§8>");
        HELP.add(" §a/vrplot item §8<§7custom/area§8> §8<§7length§8> §8<§7height§8> §8<§7width§8> §8<§7floor§8> §8<§7border§8> §8<§7lease duration (in days)§8> §8<§7amount of items§8> §8<§7player§8> §8<§7--natural(optional)§8>");
    }

    public ItemSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, HELP);
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        boolean isNatural = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--natural"));
        args = Arrays.stream(args).filter(s1 -> !s1.equalsIgnoreCase("--natural")).toArray(String[]::new);
        if (args.length < 2) {
            printHelp();
            return;
        }
        PlotSize plotSize = PlotSize.valueOf(args[1].toUpperCase());
        if (!EnumUtils.isValidEnum(PlotSize.class, args[1].toUpperCase())) return;
        int length;
        int height;
        int width;
        int backwardArgs;
        if (plotSize == PlotSize.CUSTOM || plotSize == PlotSize.AREA) {
            backwardArgs = (isNatural ? 2 : 0);
            length = Integer.parseInt(args[2]);
            height = Integer.parseInt(args[3]);
            width = Integer.parseInt(args[4]);
        } else {
            backwardArgs = 3 + (isNatural ? 2 : 0);
            length = plotSize.getLength();
            height = plotSize.getHeight();
            width = plotSize.getWidth();
        }
        if (length < 1 || width < 1 || height < 1) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().graterThenZero);
            return;
        }
        Map.Entry<String, Byte> floorData;
        Map.Entry<String, Byte> borderData;
        if (VirtualRealty.legacyVersion) {
            if (isNatural) {
                floorData = new AbstractMap.SimpleEntry<>(Material.AIR.name(), (byte)0);
                borderData = new AbstractMap.SimpleEntry<>(Material.AIR.name(), (byte)0);
            } else {
                if (args[5 - backwardArgs].equalsIgnoreCase("default")) {
                    floorData = new AbstractMap.SimpleEntry<>(plotSize.getFloorMaterial().name(), plotSize.getFloorData());
                } else
                    floorData = new AbstractMap.SimpleEntry<>(Material.valueOf(args[5 - backwardArgs].split(":")[0].toUpperCase()).name(), Byte.valueOf(args[5 - backwardArgs].split(":")[1]));
                if (args[6 - backwardArgs].equalsIgnoreCase("default")) {
                    borderData = new AbstractMap.SimpleEntry<>(plotSize.getBorderMaterial().name(), plotSize.getBorderData());
                } else
                    borderData = new AbstractMap.SimpleEntry<>(Material.valueOf(args[6 - backwardArgs].split(":")[0].toUpperCase()).name(), Byte.valueOf(args[6 - backwardArgs].split(":")[1]));
            }
        } else {
            if (isNatural) {
                floorData = new AbstractMap.SimpleEntry<>(Material.AIR.createBlockData().getAsString(), (byte)0);
                borderData = new AbstractMap.SimpleEntry<>(Material.AIR.createBlockData().getAsString(), (byte)0);
            } else {
                if (args[5 - backwardArgs].equalsIgnoreCase("default")) {
                    floorData = new AbstractMap.SimpleEntry<>(plotSize.getFloorMaterial().createBlockData().getAsString(), (byte) 0);
                } else
                    floorData = new AbstractMap.SimpleEntry<>(Material.valueOf(args[5 - backwardArgs].toUpperCase()).createBlockData().getAsString(), (byte) 0);
                if (args[6 - backwardArgs].equalsIgnoreCase("default")) {
                    borderData = new AbstractMap.SimpleEntry<>(plotSize.getBorderMaterial().createBlockData().getAsString(), (byte) 0);
                } else
                    borderData = new AbstractMap.SimpleEntry<>(Material.valueOf(args[6 - backwardArgs].toUpperCase()).createBlockData().getAsString(), (byte) 0);
            }
        }
        int additionalDays = Integer.parseInt(args[7-backwardArgs]);
        int itemsAmount = Integer.parseInt(args[8-backwardArgs]);
        Player onlinePlayer = Bukkit.getPlayer(args[9-backwardArgs]);
        for (int i = 0; i < itemsAmount; i++) {
            PlotItem plotItem = new PlotItem(VItem.CLAIM, plotSize, length, height, width, floorData, borderData, isNatural, additionalDays, UUID.randomUUID());
            onlinePlayer.getInventory().addItem(plotItem.getItemStack());
        }
    }

}
