package me.plytki.virtualrealty.commands;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class PlotCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = ((Player) sender);
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        if (args.length >= 2) {
            switch (args[0].toUpperCase()) {
                case "GAMEMODE": {
                    GameMode gameMode;
                    int gameModeID;
                    try {
                        gameModeID = Integer.parseInt(args[1]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cUse only natural numbers!");
                        return false;
                    }
                    if (!(gameModeID != 1 && gameModeID != 2)) {
                        gameMode = GameMode.getByValue(Integer.parseInt(args[1]));
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cUse only numbers from 1 to 2!");
                        return false;
                    }

                    Plot plot = PlotManager.getBorderedPlot(p.getLocation());
                    if (plot != null) {
                        if (plot.getOwnedBy() == null || (plot.getOwnedBy() != null && !plot.getOwnedBy().equals(p.getUniqueId()))) {
                            if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                                return false;
                            }
                            sender.sendMessage(VirtualRealty.PREFIX + "§cYou can't switch gamemode here!");
                        } else {
                            if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                                sender.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                            } else {
                                if (plot.getSelectedGameMode().equals(gameMode)) {
                                    if (plot.getSelectedGameMode().equals(GameMode.CREATIVE)) {
                                        sender.sendMessage(VirtualRealty.PREFIX + "§cPlot Creative-Mode is already enabled!");
                                    } else {
                                        sender.sendMessage(VirtualRealty.PREFIX + "§cPlot Creative-Mode is already disabled!");
                                    }
                                    return false;
                                }
                                plot.setSelectedGameMode(gameMode);
                                p.setGameMode(plot.getSelectedGameMode());
                                if (plot.getSelectedGameMode().equals(GameMode.CREATIVE)) {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§aPlot Creative-Mode has been enabled!");
                                } else {
                                    sender.sendMessage(VirtualRealty.PREFIX + "§ePlot Creative-Mode has been disabled!");
                                }

                            }
                        }
                    } else {
                        sender.sendMessage(VirtualRealty.PREFIX + "§cYou can't switch gamemode here!");
                    }
                    break;
                }
                default: {
                    printHelp(sender);
                }
            }
        }
        return false;
    }


    private static void printHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aPlots§8]§m                    §8§l»");
        sender.sendMessage(" §a/plot gamemode §8<§71/2§8> §8- §7Changes gamemode");
    }

}
