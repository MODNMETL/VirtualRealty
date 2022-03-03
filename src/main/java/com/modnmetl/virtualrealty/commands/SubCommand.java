package com.modnmetl.virtualrealty.commands;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.vrplot.subcommand.AssignSubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandExecution;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.LinkedList;

public abstract class SubCommand {

    private final CommandSender commandSender;
    private final LinkedList<String> helpList;

    @SneakyThrows
    public SubCommand(CommandSender sender, Command command, String label, String[] args, LinkedList<String> helpList) throws FailedCommandExecution {
        this.helpList = helpList;
        this.commandSender = sender;
        exec(sender, command, label, args);
    }

    public abstract void exec(CommandSender sender, Command command, String label, String[] args) throws FailedCommandExecution;

    public void assertPlayer() throws FailedCommandExecution {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cmdOnlyPlayers);
            throw new FailedCommandExecution();
        }
    }

    public void printHelp() throws FailedCommandExecution {
        for (String s : helpList) {
            commandSender.sendMessage(s);
        }
        throw new FailedCommandExecution();
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
