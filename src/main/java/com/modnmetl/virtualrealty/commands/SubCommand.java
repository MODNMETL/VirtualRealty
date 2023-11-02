package com.modnmetl.virtualrealty.commands;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand;
import com.modnmetl.virtualrealty.model.other.CommandType;
import com.modnmetl.virtualrealty.exception.FailedCommandException;
import com.modnmetl.virtualrealty.exception.InsufficientPermissionsException;
import com.modnmetl.virtualrealty.model.other.ChatMessage;
import com.modnmetl.virtualrealty.util.MapUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand.COMMAND_PERMISSION;

public abstract class SubCommand {

    private final String[] args;
    private final CommandSender commandSender;
    @Getter
    public LinkedList<String> HELP_LIST;
    private final boolean bypass;
    @Getter
    @Setter
    private String alias;
    private String commandName;

    public SubCommand() {
        this.args = null;
        this.commandSender = null;
        this.HELP_LIST = null;
        this.bypass = false;
        this.alias = null;
    }

    @SneakyThrows
    public SubCommand(CommandSender sender, Command command, String label, String[] args, boolean bypass, LinkedList<String> HELP_LIST) throws FailedCommandException {
        this.args = args;
        this.HELP_LIST = HELP_LIST;
        this.commandSender = sender;
        this.bypass = bypass;
        this.alias = null;
        exec(sender, command, label, args);
    }

    @SneakyThrows
    public SubCommand(CommandSender sender, Command command, String label, String[] args, LinkedList<String> HELP_LIST) throws FailedCommandException {
        this.args = args;
        this.HELP_LIST = HELP_LIST;
        this.commandSender = sender;
        this.bypass = false;
        this.alias = null;
        exec(sender, command, label, args);
    }

    public abstract void exec(CommandSender sender, Command command, String label, String[] args) throws Exception;

    public void assertPlayer() throws FailedCommandException {
        if (!(commandSender instanceof Player)) {
            ChatMessage.of(VirtualRealty.getMessages().cmdOnlyPlayers).sendWithPrefix(commandSender);
            throw new FailedCommandException();
        }
    }

    public String getSubCommandClassName() {
        return this.getClass().getSimpleName().replaceAll("SubCommand", "").toLowerCase();
    }

    public String getSubCommandName() {
        if (this.commandName != null) return this.commandName;
        Optional<SubCommand> subCommand = CommandRegistry.getSubCommand(this.getSubCommandClassName(), getCommandType());
        if (subCommand.isPresent()) {
            if (subCommand.get().getAlias() != null) {
                this.commandName = subCommand.get().getAlias();
                return this.commandName;
            }
        }
        this.commandName = this.getSubCommandClassName();
        return this.commandName;
    }

    public CommandType getCommandType() {
        String name = this.getClass().getPackage().getName().replaceAll("com.modnmetl.virtualrealty.commands.", "").replaceAll(".subcommand", "").toUpperCase();
        return CommandType.valueOf(name);
    }

    public String getDefaultPermission() {
        return COMMAND_PERMISSION.getName() + "." + args[0].toLowerCase();
    }

    public void assertPermission() throws InsufficientPermissionsException {
        if (!commandSender.hasPermission(getDefaultPermission())) {
            if (commandSender.isOp()) {
                ChatMessage.of(VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", getDefaultPermission())).sendWithPrefix(commandSender);
            } else {
                ChatMessage.of(VirtualRealty.getMessages().insufficientPermissionsShort.replaceAll("%permission%", getDefaultPermission())).sendWithPrefix(commandSender);
            }
            throw new InsufficientPermissionsException();
        }
    }

    public void assertPermission(String permission) throws InsufficientPermissionsException {
        if (!commandSender.hasPermission(permission)) {
            if (commandSender.isOp()) {
                ChatMessage.of(VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", permission)).sendWithPrefix(commandSender);
            } else {
                ChatMessage.of(VirtualRealty.getMessages().insufficientPermissionsShort.replaceAll("%permission%", permission)).sendWithPrefix(commandSender);
            }
            throw new InsufficientPermissionsException();
        }
    }

    public boolean isBypass() {
        return this.bypass;
    }

    public void printHelp(CommandType commandType) throws FailedCommandException {
        Map<String, LinkedList<String>> commandsHelp = null;
        Map<String, String> commandsAliases = null;
        switch (commandType) {
            case VRPLOT: {
                commandsHelp = VirtualRealty.getCommands().vrplotCommandsHelp;
                commandsAliases = VirtualRealty.getCommands().vrplotAliases;
                break;
            }
            case PLOT: {
                commandsHelp = VirtualRealty.getCommands().plotCommandsHelp;
                commandsAliases = VirtualRealty.getCommands().plotAliases.getAliasesMap();
                break;
            }
        }
        String subCommandName = getSubCommandName();
        String keyByValue = MapUtils.getKeyByValue(commandsAliases, subCommandName);
        for (String s : commandsHelp.get(keyByValue)) {
            String message = s.replaceAll("%command%", subCommandName);
            ChatMessage.of(message).send(commandSender);
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
