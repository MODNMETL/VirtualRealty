package com.modnmetl.virtualrealty.commands.plot;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.CommandRegistry;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.enums.commands.CommandType;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

import static com.modnmetl.virtualrealty.commands.CommandRegistry.PLOT_SUB_COMMAND_LIST;

public class PlotCommand implements CommandExecutor {

    public static final LinkedList<String> HELP_LIST = new LinkedList<>();

    static {
        HELP_LIST.add(" ");
        HELP_LIST.add(" §8§l«§8§m                    §8[§aVirtualRealty§8]§m                    §8§l»");
        HELP_LIST.add(" §a/plot %panel_command% §8- §7Opens your plots panel");
        HELP_LIST.add(" §a/plot %draft_command% §8- §7Shows layout of potential new plot");
        HELP_LIST.add(" §a/plot %stake_command% §8- §7Creates the plot shown with draft");
        HELP_LIST.add(" §a/plot %info_command% §8- §7Shows plot info");
        HELP_LIST.add(" §a/plot %gm_command% §8- §7Changes gamemode");
        HELP_LIST.add(" §a/plot %add_command% §8- §7Adds a member");
        HELP_LIST.add(" §a/plot %kick_command% §8- §7Kicks a member");
        HELP_LIST.add(" §a/plot %list_command% §8- §7Shows your plots");
        HELP_LIST.add(" §a/plot %tp_command% §8- §7Teleports to the plot");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean displayError = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--error") || s.equalsIgnoreCase("-e"));
        boolean bypass = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--yes") || s.equalsIgnoreCase("-y"));
        args = Arrays.stream(args).filter(s1 ->
                !s1.equalsIgnoreCase("--error") &&
                !s1.equalsIgnoreCase("--yes") &&
                !s1.equalsIgnoreCase("-e") &&
                !s1.equalsIgnoreCase("-y"))
                .toArray(String[]::new);
        if ((args.length > 0 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            printHelp(sender);
            return false;
        }
        if (args[0].equalsIgnoreCase("panel")) {
            try {
                Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, VirtualRealty.getLoader());
            } catch (Exception e) {
                sender.sendMessage("§aThis function is available with a valid license key");
                TextComponent linkComponent = new TextComponent("§fhttps://modnmetl.com/");
                linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modnmetl.com/category/virtual-realty-plugin-61eef16fe2eeab00116a3e64"));
                sender.spigot().sendMessage(new TextComponent("§aPlease visit "), linkComponent, new TextComponent(" §ato purchase one"));
                return false;
            }
        }
        try {
            String subcommandName = String.valueOf(args[0].toCharArray()[0]).toUpperCase() + args[0].substring(1);
            Optional<SubCommand> subCommand1 = CommandRegistry.getSubCommand(subcommandName.toLowerCase(), CommandType.PLOT);
            subCommand1.get().getClass().getConstructors()[1].newInstance(sender, command, label, args);
        } catch (Exception e) {
            if (!(e instanceof InvocationTargetException)) {
                printHelp(sender);
                return false;
            }
            if (displayError) {
                e.printStackTrace();
            } else {
                if (e.getCause() instanceof FailedCommandException) return false;
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
            CommandRegistry.PLOT_PLACEHOLDERS.forEach((s, s2) -> {
                finalMessage[0] = finalMessage[0].replaceAll(s, s2);
            });
            sender.sendMessage(
                    finalMessage[0]
            );
        }
    }

}