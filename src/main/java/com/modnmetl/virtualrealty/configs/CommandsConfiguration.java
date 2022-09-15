package com.modnmetl.virtualrealty.configs;

import com.modnmetl.virtualrealty.commands.CommandManager;
import com.modnmetl.virtualrealty.commands.CommandRegistry;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.commands.plot.PlotCommand;
import com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand;
import com.modnmetl.virtualrealty.enums.commands.CommandType;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.lang.reflect.Field;
import java.util.*;

@Header("-------------------------------------------------------------- #")
@Header("                                                               #")
@Header("                           Commands                            #")
@Header("                                                               #")
@Header("-------------------------------------------------------------- #")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class CommandsConfiguration extends OkaeriConfig {

    public LinkedHashMap<String, String> plotAliases = getPlotAliasesHashMap();
    public LinkedHashMap<String, String> vrplotAliases = getVRPlotAliasesHashMap();

    public LinkedHashMap<String, LinkedList<String>> plotCommandsHelp = getCommandsHelp(CommandType.PLOT);
    public LinkedHashMap<String, LinkedList<String>> vrplotCommandsHelp = getCommandsHelp(CommandType.VRPLOT);

    public static LinkedHashMap<String, String> getVRPlotAliasesHashMap() {
        LinkedHashMap<String, String> aliasesHashMap = new LinkedHashMap<>();
        for (String s : CommandManager.SUBCOMMANDS.get(VirtualRealtyCommand.class)) {
            if (s == null || s.isEmpty()) continue;
            aliasesHashMap.put(s, s);
        }
        return aliasesHashMap;
    }

    public static LinkedHashMap<String, String> getPlotAliasesHashMap() {
        LinkedHashMap<String, String> aliasesHashMap = new LinkedHashMap<>();
        for (String s : CommandManager.SUBCOMMANDS.get(PlotCommand.class)) {
            if (s == null || s.isEmpty()) continue;
            aliasesHashMap.put(s, s);
        }
        return aliasesHashMap;
    }

    public static LinkedHashMap<String, LinkedList<String>> getCommandsHelp(CommandType commandType) {
        LinkedHashMap<String, LinkedList<String>> commandsHelpMap = new LinkedHashMap<>();
        switch (commandType) {
            case VRPLOT:
                commandsHelpMap.put("vrplot", VirtualRealtyCommand.HELP_LIST);
                break;
            case PLOT:
                commandsHelpMap.put("plot", PlotCommand.HELP_LIST);
                break;
        }
        for (SubCommand subCommand : CommandRegistry.getSubCommandList(commandType)) {
            Class<? extends SubCommand> aClass = subCommand.getClass();
            String simpleName = aClass.getSimpleName();
            String subCommand1 = simpleName.replaceAll("SubCommand", "").toLowerCase();
            if (Arrays.stream(subCommand.getClass().getFields()).noneMatch(field -> field.getName().equalsIgnoreCase("HELP"))) continue;
            Field helpList;
            try {
                helpList = subCommand.getClass().getField("HELP");
            } catch (NoSuchFieldException e) {
                continue;
            }
            helpList.setAccessible(true);
            Object o;
            try {
                o = helpList.get(null);
            } catch (IllegalAccessException e) {
                continue;
            }
            LinkedList<String> o1 = (LinkedList<String>) o;
            if (o1 == null || o1.isEmpty()) continue;
            commandsHelpMap.put(subCommand1, o1);
        }
        return commandsHelpMap;
    }

    public void refreshHelpMessages() {
        for (SubCommand subCommand : CommandRegistry.getSubCommandList(CommandType.PLOT)) {
            if (plotCommandsHelp.containsKey(subCommand.getSubCommandClassName())) {
                LinkedList<String> strings = plotCommandsHelp.get(subCommand.getSubCommandClassName());
                Field helpList;
                try {
                    helpList = subCommand.getClass().getField("HELP");
                } catch (NoSuchFieldException e) {
                    continue;
                }
                helpList.setAccessible(true);
                try {
                    helpList.set(null, strings);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        PlotCommand.HELP_LIST.clear();
        PlotCommand.HELP_LIST.addAll(plotCommandsHelp.get("plot"));

        for (SubCommand subCommand : CommandRegistry.getSubCommandList(CommandType.VRPLOT)) {
            if (vrplotCommandsHelp.containsKey(subCommand.getSubCommandClassName())) {
                LinkedList<String> strings = vrplotCommandsHelp.get(subCommand.getSubCommandClassName());
                Field helpList;
                try {
                    helpList = subCommand.getClass().getField("HELP");
                } catch (NoSuchFieldException e) {
                    continue;
                }
                helpList.setAccessible(true);
                try {
                    helpList.set(null, strings);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        VirtualRealtyCommand.HELP_LIST.clear();
        VirtualRealtyCommand.HELP_LIST.addAll(vrplotCommandsHelp.get("vrplot"));
    }

    public void assignAliases() {
        vrplotAliases.forEach((original, alias) -> {
            Optional<SubCommand> any = CommandRegistry.VRPLOT_SUB_COMMAND_LIST.stream().filter(subCommand -> subCommand.getSubCommandClassName().equalsIgnoreCase(original)).findAny();
            any.ifPresent(subCommand -> subCommand.setAlias(alias));
        });
        plotAliases.forEach((original, alias) -> {
            Optional<SubCommand> any = CommandRegistry.PLOT_SUB_COMMAND_LIST.stream().filter(subCommand -> subCommand.getSubCommandClassName().equalsIgnoreCase(original)).findAny();
            any.ifPresent(subCommand -> subCommand.setAlias(alias));
        });
    }

}
