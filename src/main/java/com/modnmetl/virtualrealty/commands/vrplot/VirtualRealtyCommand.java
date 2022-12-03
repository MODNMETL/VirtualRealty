package com.modnmetl.virtualrealty.commands.vrplot;

import com.modnmetl.virtualrealty.commands.CommandRegistry;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.model.other.CommandType;
import com.modnmetl.virtualrealty.exception.FailedCommandException;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.exception.InsufficientPermissionsException;
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
        HELP_LIST.add(" §a/vrplot %create_command% §8- §7Creates a plot");
        HELP_LIST.add(" §a/vrplot %remove_command% §8- §7Removes a plot");
        HELP_LIST.add(" §a/vrplot %set_command% §8- §7Sets a variable for the plot");
        HELP_LIST.add(" §a/vrplot %assign_command% §8- §7Assigns a plot to player");
        HELP_LIST.add(" §a/vrplot %unassign_command% §8- §7Sets assigned to and assigned by to null");
        HELP_LIST.add(" §a/vrplot %info_command% §8- §7Prints info about plot");
        HELP_LIST.add(" §a/vrplot %list_command% §8- §7Prints all plots");
        HELP_LIST.add(" §a/vrplot %item_command% §8- §7Creates plot item");
        HELP_LIST.add(" §a/vrplot %visual_command% §8- §7Displays visual grid of the plot");
        HELP_LIST.add(" §a/vrplot %tp_command% §8- §7Teleports to the plot");
        HELP_LIST.add(" §a/vrplot %reload_command% §8- §7Reloads plugin");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        boolean displayError = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--error") || s.equalsIgnoreCase("-e"));
        boolean bypass = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--yes") || s.equalsIgnoreCase("-y"));
        args = Arrays.stream(args).filter(s1 ->
                        !s1.equalsIgnoreCase("--error") &&
                                !s1.equalsIgnoreCase("--yes") &&
                                !s1.equalsIgnoreCase("-e") &&
                                !s1.equalsIgnoreCase("-y"))
                .toArray(String[]::new);
        if (!sender.hasPermission(COMMAND_PERMISSION)) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", COMMAND_PERMISSION.getName()));
            return false;
        }
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        try {
            String subcommandName = String.valueOf(args[0].toCharArray()[0]).toUpperCase() + args[0].substring(1);
            Class<? extends SubCommand> aClass = CommandRegistry.getSubCommand(subcommandName.toLowerCase(), CommandType.VRPLOT).get().getClass();
            if (bypass) {
                aClass.getConstructors()[2].newInstance(sender, command, label, args, true);
            } else {
                aClass.getConstructors()[1].newInstance(sender, command, label, args);
            }
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
            final String[] finalMessage = {message};
            CommandRegistry.VRPLOT_PLACEHOLDERS.forEach((s, s2) -> {
                finalMessage[0] = finalMessage[0].replaceAll(s, s2);
            });
            if (!finalMessage[0].contains("_command%"))
                sender.sendMessage(
                        finalMessage[0]
                );
        }
    }
    
}