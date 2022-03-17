package com.modnmetl.virtualrealty.commands;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import com.modnmetl.virtualrealty.exceptions.InsufficientPermissionsException;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.LinkedList;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public abstract class SubCommand {

    private final String[] args;
    private final CommandSender commandSender;
    private final LinkedList<String> helpList;

    @SneakyThrows
    public SubCommand(CommandSender sender, Command command, String label, String[] args, LinkedList<String> helpList) throws FailedCommandException {
        this.args = args;
        this.helpList = helpList;
        this.commandSender = sender;
        exec(sender, command, label, args);
    }

    public abstract void exec(CommandSender sender, Command command, String label, String[] args) throws Exception;

    public void assertPlayer() throws FailedCommandException {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cmdOnlyPlayers);
            throw new FailedCommandException();
        }
    }

    public String getDefaultPermission() {
        return COMMAND_PERMISSION.getName() + "." + args[0].toLowerCase();
    }

    public void assertPermission() throws InsufficientPermissionsException {
        if (!commandSender.hasPermission(getDefaultPermission())) {
            if (commandSender.isOp()) {
                commandSender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", getDefaultPermission()));
            } else {
                commandSender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissionsShort.replaceAll("%permission%", getDefaultPermission()));
            }
            throw new InsufficientPermissionsException();
        }
    }

    public void assertPermission(String permission) throws InsufficientPermissionsException {
        if (!commandSender.hasPermission(permission)) {
            if (commandSender.isOp()) {
                commandSender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", permission));
            } else {
                commandSender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissionsShort.replaceAll("%permission%", permission));
            }
            throw new InsufficientPermissionsException();
        }
    }

    public void printHelp() throws FailedCommandException {
        for (String s : helpList) {
            commandSender.sendMessage(s);
        }
        throw new FailedCommandException();
    }

    public static void registerSubCommands(String[] subCommand, Class<?> mainCommandClass) {
        for (String s : subCommand) {
            try {
                Method method = CommandManager.class.getMethod("addSubCommand", String.class, Class.class);
                method.setAccessible(true);
                method.invoke(null, s, mainCommandClass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
