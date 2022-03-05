package com.modnmetl.virtualrealty.commands.plot;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.exceptions.FailedCommandExecution;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class PlotCommand implements CommandExecutor {

    public static final LinkedList<String> HELP_LIST = new LinkedList<>();

    static {
        HELP_LIST.add(" ");
        HELP_LIST.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP_LIST.add(" §a/plot panel §8- §7Opens your plots panel");
        HELP_LIST.add(" §a/plot draft §8- §7Enables draft mode");
        HELP_LIST.add(" §a/plot stake §8- §7Creates a plot when not colliding");
        HELP_LIST.add(" §a/plot info §8- §7Shows plot info");
        HELP_LIST.add(" §a/plot gm §8- §7Changes gamemode");
        HELP_LIST.add(" §a/plot add §8- §7Adds a member");
        HELP_LIST.add(" §a/plot kick §8- §7Kicks a member");
        HELP_LIST.add(" §a/plot list §8- §7Shows your plots");
        HELP_LIST.add(" §a/plot tp §8- §7Teleports to the plot");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean displayError = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--error"));
        args = Arrays.stream(args).filter(s1 -> !s1.equalsIgnoreCase("--error")).toArray(String[]::new);
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        if (args[0].equalsIgnoreCase("panel") || args[0].equalsIgnoreCase("draft") || args[0].equalsIgnoreCase("stake")) {
            try {
                Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, VirtualRealty.getCustomClassLoader());
            } catch (Exception e) {
                sender.sendMessage("§aThis function is available with a valid license key");
                sender.sendMessage("§aPlease visit §fhttps://modnmetl.com/ §ato purchase one");
                return false;
            }
        }
        try {
            Class<?> clazz = Class.forName("com.modnmetl.virtualrealty.commands.plot.subcommand." + String.valueOf(args[0].toCharArray()[0]).toUpperCase(Locale.ROOT) + args[0].substring(1) + "SubCommand", true, VirtualRealty.getCustomClassLoader());
            clazz.getConstructors()[0].newInstance(sender, command, label, args);
        } catch (Exception e) {
            if(!(e instanceof InvocationTargetException)) {
                printHelp(sender);
                return false;
            }

            if (displayError) {
                e.printStackTrace();
            } else {
                if (e.getCause() instanceof FailedCommandExecution) return false;
                sender.sendMessage("§cAn error occurred while executing the command.");
                sender.sendMessage("§cCheck console for details.");
                VirtualRealty.getInstance().getLogger().log(Level.SEVERE, "Failed command execution | Command Sender: " + sender.getName());
                VirtualRealty.getInstance().getLogger().log(Level.SEVERE, "To print more details add \"--error\" argument at the end of the command.");
            }
        }
        return false;
    }

    private static void printHelp(CommandSender sender) {
        for (String message : HELP_LIST) {
            sender.sendMessage(message);
        }
    }

}