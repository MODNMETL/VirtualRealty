package com.modnmetl.virtualrealty.commands;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.commands.CommandType;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CommandRegistry {

    public static final HashMap<String, String> PLOT_PLACEHOLDERS = new HashMap<>();
    public static final HashMap<String, String> VRPLOT_PLACEHOLDERS = new HashMap<>();
    public static final List<SubCommand> PLOT_SUB_COMMAND_LIST = new ArrayList<>();
    public static final List<SubCommand> VRPLOT_SUB_COMMAND_LIST = new ArrayList<>();

    public static void setupPlaceholders() {
        for (SubCommand subCommand : PLOT_SUB_COMMAND_LIST) {
            PLOT_PLACEHOLDERS.put("%" + subCommand.getSubCommandClassName() + "_command%", subCommand.getSubCommandName());
        }
        for (SubCommand subCommand : VRPLOT_SUB_COMMAND_LIST) {
            VRPLOT_PLACEHOLDERS.put("%" + subCommand.getSubCommandClassName() + "_command%", subCommand.getSubCommandName());
        }
        if (VirtualRealty.getPremium() == null) {
            PLOT_PLACEHOLDERS.put("%panel_command%", "panel");
        } else {
            getSubCommand("panel", CommandType.PLOT).ifPresent(subCommand -> {
                PLOT_PLACEHOLDERS.put("%panel_command%", subCommand.getSubCommandName());
            });
        }
    }

    @SneakyThrows
    public static void addSubCommandToRegistry(Class<?> clazz, CommandType commandType) {
        if (clazz.getSimpleName().equalsIgnoreCase("SubCommand")) return;
        Object newInstance = clazz.newInstance();
        switch (commandType) {
            case PLOT: {
                PLOT_SUB_COMMAND_LIST.add((SubCommand) newInstance);
                break;
            }
            case VRPLOT: {
                VRPLOT_SUB_COMMAND_LIST.add((SubCommand) newInstance);
                break;
            }
        }
    }

    public static Optional<SubCommand> getSubCommand(String name, CommandType commandType) {
        switch (commandType) {
            case PLOT: {
                return PLOT_SUB_COMMAND_LIST.stream().filter(subCommand ->
                        subCommand.getSubCommandClassName().equalsIgnoreCase(name)
                                        ||
                                        (subCommand.getAlias() != null && subCommand.getAlias().equalsIgnoreCase(name))
                ).findAny();
            }
            case VRPLOT: {
                return VRPLOT_SUB_COMMAND_LIST.stream().filter(subCommand ->
                                subCommand.getSubCommandClassName().equalsIgnoreCase(name)
                                        ||
                                        (subCommand.getAlias() != null && subCommand.getAlias().equalsIgnoreCase(name))
                ).findAny();
            }
        }
        return Optional.empty();
    }

    public static List<SubCommand> getSubCommandList(CommandType commandType) {
        switch (commandType) {
            case VRPLOT:
                return VRPLOT_SUB_COMMAND_LIST;
            case PLOT:
                return PLOT_SUB_COMMAND_LIST;
        }
        return new ArrayList<>();
    }

    @SneakyThrows
    public static String getSubCommandName(String placeholder, CommandType commandType) {
        Optional<SubCommand> command = getSubCommand(placeholder.replaceAll("%", "").replaceAll("_command", ""), commandType);
        return command.map(SubCommand::getSubCommandName).orElse(null);
    }

    @SneakyThrows
    public static List<String> getSubCommandNames(SubCommand subCommand, CommandType commandType) {
        List<String> strings = new ArrayList<>();
        getSubCommand(subCommand.getSubCommandClassName(), commandType).ifPresent(subCommand1 -> {
            strings.add(subCommand1.getSubCommandClassName());
            strings.add(subCommand1.getSubCommandName());
        });
        return strings;
    }

}
