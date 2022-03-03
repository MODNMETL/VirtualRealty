package com.modnmetl.virtualrealty.commands;

import com.modnmetl.virtualrealty.commands.plot.PlotCommand;
import com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.EnumUtils;
import lombok.SneakyThrows;
import net.minecraft.server.v1_13_R2.ItemNetherStar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandManager implements TabCompleter {

    public static final HashMap<Class<?>, SortedSet<String>> SUBCOMMANDS = new HashMap<>();

    public static void addSubCommand(String subCommand, Class<?> mainCommandClass) {
        if (!SUBCOMMANDS.containsKey(mainCommandClass)) {
            SUBCOMMANDS.put(mainCommandClass, new TreeSet<>());
        }
        SUBCOMMANDS.get(mainCommandClass).add(subCommand);
    }

    @SneakyThrows
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        LinkedList<String> tabCompleter = new LinkedList<>();
        if (command.getName().equalsIgnoreCase("virtualrealty")) {
            if (!sender.isOp()) return null;
            if (args.length <= 1) {
                for (String subcommand : SUBCOMMANDS.get(VirtualRealtyCommand.class)) {
                    if (args[0].isEmpty()) {
                        tabCompleter.add(subcommand);
                    } else if (subcommand.startsWith(args[0])) {
                        tabCompleter.add(subcommand);
                    }
                }
            }
            switch (args[0].toUpperCase(Locale.ROOT)) {
                case "CREATE": {
                    if (args.length > 1) {
                        if (args.length == 2) {
                            for (PlotSize value : PlotSize.values()) {
                                if (value == PlotSize.CUSTOM) continue;
                                if (args[1].isEmpty()) {
                                    tabCompleter.add(value.name().toLowerCase());
                                } else if (value.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                                    tabCompleter.add(value.name().toLowerCase());
                                }
                            }
                        }
                        if (args.length > 2) {
                            boolean predefinedValue = EnumUtils.isValidEnum(PlotSize.class, args[1].toUpperCase());
                            if ((predefinedValue && args.length < 5) || (!predefinedValue && args.length > 4 && args.length < 7)) {
                                for (Material value : Material.values()) {
                                    if (!value.isSolid()) continue;
                                    if (args[args.length - 1].isEmpty()) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    } else if (value.name().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    }
                                }
                            }
                        }
                    }
                    return tabCompleter;
                }
                case "SET": {
                    if (args.length == 2) {
                        for (Plot plot : PlotManager.getPlots()) {
                            if (args[1].isEmpty()) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            } else if (String.valueOf(plot.getID()).toLowerCase().startsWith(args[1].toLowerCase())) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            }
                        }
                        return tabCompleter;
                    }
                    if (args.length == 3) {
                        List<String> arguments = Arrays.asList("owner", "floor", "border", "expiry");
                        for (String argument : arguments) {
                            if (args[2].isEmpty()) {
                                tabCompleter.add(argument.toLowerCase());
                            } else if (argument.toLowerCase().startsWith(args[2].toLowerCase())) {
                                tabCompleter.add(argument.toLowerCase());
                            }
                        }
                        return tabCompleter;
                    }
                    if (args.length == 4) {
                        switch (args[2].toUpperCase()) {
                            case "FLOOR":
                            case "BORDER": {
                                for (Material value : Material.values()) {
                                    if (!value.isSolid()) continue;
                                    if (args[3].isEmpty()) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    } else if (value.name().toLowerCase().startsWith(args[3].toLowerCase())) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    }
                                }
                                return tabCompleter;
                            }
                            case "OWNER": {
                                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                    if (args[3].isEmpty()) {
                                        tabCompleter.add(onlinePlayer.getName());
                                    } else if (onlinePlayer.getName().toLowerCase().startsWith(args[3].toLowerCase())) {
                                        tabCompleter.add(onlinePlayer.getName());
                                    }
                                }
                                return tabCompleter;
                            }
                            case "EXPIRY": {
                                String validFormat = "31/12/2999";
                                String argument = args[3].toLowerCase();
                                if (argument.isEmpty()) {
                                    tabCompleter.add(validFormat);
                                } else {
                                    String display = null;
                                    String[] providedStrings = argument.split("/");
                                    if (providedStrings.length == 1) {
                                        display = validFormat.substring(2);
                                    }
                                    if (providedStrings.length == 2) {
                                        display = validFormat.substring(5);
                                    }
                                    if (providedStrings.length == 3 && providedStrings[2].length() < 4) {
                                        display = validFormat.substring(6 + providedStrings[2].length());
                                    }
                                    if (display != null) {
                                        if (argument.length() <= validFormat.length()) {
                                            tabCompleter.add(display);
                                        }
                                    }
                                }
                                return tabCompleter;
                            }
                        }
                    }
                    if (args.length == 5) {
                        if (args[2].equalsIgnoreCase("expiry")) {
                            String validFormat = "00:00";
                            String argument = args[4].toLowerCase();
                            if (argument.isEmpty()) {
                                tabCompleter.add(validFormat);
                            } else {
                                String display = null;
                                String[] providedStrings = argument.split(":");
                                if (providedStrings.length == 1) {
                                    display = validFormat.substring(2);
                                }
                                if (display != null) {
                                    if (argument.length() <= validFormat.length()) {
                                        tabCompleter.add(display);
                                    }
                                }
                            }
                            return tabCompleter;
                        }
                    }
                    return tabCompleter;
                }
                case "ASSIGN":
                case "UNASSIGN": {
                    if (args.length == 2) {
                        for (Plot plot : PlotManager.getPlots()) {
                            if (args[1].isEmpty()) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            } else if (String.valueOf(plot.getID()).toLowerCase().startsWith(args[1].toLowerCase())) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            }
                        }
                        return tabCompleter;
                    }
                    if (args.length == 3) {
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (args[2].isEmpty()) {
                                tabCompleter.add(onlinePlayer.getName());
                            } else if (onlinePlayer.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                tabCompleter.add(onlinePlayer.getName());
                            }
                        }
                        return tabCompleter;
                    }
                    return tabCompleter;
                }
                case "INFO":
                case "REMOVE":
                case "TP": {
                    if (args.length == 2) {
                        for (Plot plot : PlotManager.getPlots()) {
                            if (args[1].isEmpty()) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            } else if (String.valueOf(plot.getID()).toLowerCase().startsWith(args[1].toLowerCase())) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            }
                        }
                        return tabCompleter;
                    }
                }
                case "ITEM": {
                    if (args.length > 1) {
                        int backwardsArgs = 3;
                        if (args.length == 2) {
                            for (PlotSize value : PlotSize.values()) {
                                if (args[1].isEmpty()) {
                                    tabCompleter.add(value.name().toLowerCase());
                                } else if (value.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                                    tabCompleter.add(value.name().toLowerCase());
                                }
                            }
                        }
                        if (args.length > 2) {
                            if (!EnumUtils.isValidEnum(PlotSize.class, args[1].toUpperCase())) return null;
                            PlotSize plotSize = PlotSize.valueOf(args[1].toUpperCase());
                            int length;
                            int height;
                            int width;
                            switch (plotSize) {
                                case AREA:
                                case CUSTOM: {
                                    backwardsArgs = 0;
                                    if (args.length == 3 && args[2].isEmpty()) {
                                        tabCompleter.add(String.valueOf(plotSize.getLength()));
                                        return tabCompleter;
                                    }
                                    if (args.length == 4 && args[3].isEmpty()) {
                                        tabCompleter.add(String.valueOf(plotSize.getHeight()));
                                        return tabCompleter;
                                    }
                                    if (args.length == 5 && args[4].isEmpty()) {
                                        tabCompleter.add(String.valueOf(plotSize.getWidth()));
                                        return tabCompleter;
                                    }
                                    if (args.length > 5) {
                                        length = Integer.parseInt(args[2]);
                                        height = Integer.parseInt(args[3]);
                                        width = Integer.parseInt(args[4]);
                                    }
                                }
                            }
                            if (args.length == 6-backwardsArgs) {
                                for (Material value : Material.values()) {
                                    if (!value.isSolid()) continue;
                                    if (args[5 - backwardsArgs].isEmpty()) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    } else if (value.name().toLowerCase().startsWith(args[5 - backwardsArgs].toLowerCase())) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    }
                                }
                            }
                            if (args.length == 7-backwardsArgs) {
                                for (Material value : Material.values()) {
                                    if (!value.isSolid()) continue;
                                    if (args[6 - backwardsArgs].isEmpty()) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    } else if (value.name().toLowerCase().startsWith(args[6 - backwardsArgs].toLowerCase())) {
                                        tabCompleter.add(value.name().toLowerCase());
                                    }
                                }
                            }
                            if (args.length == 8-backwardsArgs) {
                                if (args[7-backwardsArgs].isEmpty()) {
                                    tabCompleter.add("180");
                                    return tabCompleter;
                                }
                            }
                            if (args.length == 9-backwardsArgs) {
                                if (args[8-backwardsArgs].isEmpty()) {
                                    tabCompleter.add("1");
                                    return tabCompleter;
                                }
                            }
                            if (args.length == 10-backwardsArgs) {
                                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                    if (args[9-backwardsArgs].isEmpty()) {
                                        tabCompleter.add(onlinePlayer.getName());
                                    } else if (onlinePlayer.getName().toLowerCase().startsWith(args[9-backwardsArgs].toLowerCase())) {
                                        tabCompleter.add(onlinePlayer.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return tabCompleter;
        } else if (command.getName().equalsIgnoreCase("plot")) {
            if (!sender.isOp()) return null;
            if (!(sender instanceof Player)) return null;
            Player player = ((Player) sender);
            if (args.length <= 1) {
                for (String subcommand : SUBCOMMANDS.get(PlotCommand.class)) {
                    if (args[0].isEmpty()) {
                        tabCompleter.add(subcommand);
                    } else if (subcommand.startsWith(args[0])) {
                        tabCompleter.add(subcommand);
                    }
                }
            }
            switch (args[0].toUpperCase(Locale.ROOT)) {
                case "KICK":
                case "ADD": {
                    if (args.length == 2) {
                        PlotManager.getAccessPlots(player.getUniqueId()).forEach((integer, plot) -> {
                            if (args[1].isEmpty()) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            } else if (String.valueOf(plot.getID()).toLowerCase().startsWith(args[0].toLowerCase())) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            }
                        });
                        return tabCompleter;
                    }
                    if (args.length == 3) {
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (args[2].isEmpty()) {
                                tabCompleter.add(onlinePlayer.getName());
                            } else if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                tabCompleter.add(onlinePlayer.getName());
                            }
                        }
                        return tabCompleter;
                    }
                }
                case "TP": {
                    if (args.length == 2) {
                        PlotManager.getAccessPlots(player.getUniqueId()).forEach((integer, plot) -> {
                            if (args[1].isEmpty()) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            } else if (String.valueOf(plot.getID()).toLowerCase().startsWith(args[0].toLowerCase())) {
                                tabCompleter.add(String.valueOf(plot.getID()));
                            }
                        });
                        return tabCompleter;
                    }
                }
            }
            return tabCompleter;
        }
        return null;
    }

}
