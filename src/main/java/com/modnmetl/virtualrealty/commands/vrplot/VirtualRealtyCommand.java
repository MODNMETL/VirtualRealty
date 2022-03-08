package com.modnmetl.virtualrealty.commands.vrplot;

import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.exceptions.InsufficientPermissionsException;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class VirtualRealtyCommand implements CommandExecutor {

    public static final Permission COMMAND_PERMISSION = new Permission(VirtualRealty.GLOBAL_PERMISSION.getName() + ".vrplot");

    public static final LinkedList<String> HELP_LIST = new LinkedList<>();

    static {
        HELP_LIST.add(" ");
        HELP_LIST.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP_LIST.add(" §a/vrplot create §8- §7Creates a plot");
        HELP_LIST.add(" §a/vrplot remove §8- §7Removes a plot");
        HELP_LIST.add(" §a/vrplot set §8- §7Sets a variable for the plot");
        HELP_LIST.add(" §a/vrplot assign §8- §7Assigns a plot to player");
        HELP_LIST.add(" §a/vrplot unassign §8- §7Sets assigned to and assigned by to null");
        HELP_LIST.add(" §a/vrplot info §8- §7Prints info about plot");
        HELP_LIST.add(" §a/vrplot list §8- §7Prints all plots");
        HELP_LIST.add(" §a/vrplot item §8- §7Creates plot item");
        HELP_LIST.add(" §a/vrplot visual §8- §7Displays visual grid of the plot");
        HELP_LIST.add(" §a/vrplot tp §8- §7Teleports to the plot");
        HELP_LIST.add(" §a/vrplot reload §8- §7Reloads plugin");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        boolean displayError = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--error"));
        args = Arrays.stream(args).filter(s1 -> !s1.equalsIgnoreCase("--error")).toArray(String[]::new);
        if (!sender.hasPermission(COMMAND_PERMISSION)) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", COMMAND_PERMISSION.getName()));
            return false;
        }
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        if (args[0].equalsIgnoreCase("visual") || args[0].equalsIgnoreCase("item")) {
            try {
                Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, VirtualRealty.getCustomClassLoader());
            } catch (Exception e) {
                sender.sendMessage("§aThis function is available with a valid license key");
                sender.sendMessage("§aPlease visit §fhttps://modnmetl.com/ §ato purchase one");
                return false;
            }
        }
        try {
            Class<?> clazz = Class.forName("com.modnmetl.virtualrealty.commands.vrplot.subcommand." + String.valueOf(args[0].toCharArray()[0]).toUpperCase(Locale.ROOT) + args[0].substring(1) + "SubCommand", true, VirtualRealty.getCustomClassLoader());
            clazz.getConstructors()[0].newInstance(sender, command, label, args);
        } catch (Exception e) {
            if(!(e instanceof InvocationTargetException)) {
                printHelp(sender);
                return false;
            }
            if (displayError) {
                e.printStackTrace();
            } else {
                if (e.getCause() instanceof FailedCommandException) return false;
                if (e.getCause() instanceof InsufficientPermissionsException) return false;
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