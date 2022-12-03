package com.modnmetl.virtualrealty.configs;

import com.modnmetl.virtualrealty.commands.CommandManager;
import com.modnmetl.virtualrealty.commands.CommandRegistry;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.commands.plot.PlotCommand;
import com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand;
import com.modnmetl.virtualrealty.model.other.CommandType;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

@Header("-------------------------------------------------------------- #")
@Header("                                                               #")
@Header("                           Commands                            #")
@Header("                                                               #")
@Header("-------------------------------------------------------------- #")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class CommandsConfiguration extends OkaeriConfig {

    public PlotAliases plotAliases = new PlotAliases();
    public Map<String, String> vrplotAliases = getVRPlotAliasesHashMap();

    public Map<String, LinkedList<String>> plotCommandsHelp = getCommandsHelp(CommandType.PLOT);
    public Map<String, LinkedList<String>> vrplotCommandsHelp = getCommandsHelp(CommandType.VRPLOT);



    @Names(strategy = NameStrategy.HYPHEN_CASE)
    public static class PlotAliases extends OkaeriConfig  {

        public String panel = "panel";
        public String add = "add";
        public String gm = "gm";
        public String info = "info";
        public String kick = "kick";
        public String list = "list";
        public String tp = "tp";

        public PlotAliases() {
            super.setRemoveOrphans(true);
        }

        @SneakyThrows
        public Map<String, String> getAliasesMap() {
            Map<String, String> aliasesMap = new HashMap<>();
            for (Field field : this.getClass().getDeclaredFields()) {
                String name = field.getName();
                String alias = (String) field.get(this);
                aliasesMap.put(name, alias);
            }
            return aliasesMap;
        }

    }

    public static LinkedHashMap<String, String> getVRPlotAliasesHashMap() {
        LinkedHashMap<String, String> aliasesHashMap = new LinkedHashMap<>();
        for (String s : CommandManager.SUBCOMMANDS.get(VirtualRealtyCommand.class)) {
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
        plotAliases.getAliasesMap().forEach((original, alias) -> {
            Optional<SubCommand> any = CommandRegistry.PLOT_SUB_COMMAND_LIST.stream().filter(subCommand -> subCommand.getSubCommandClassName().equalsIgnoreCase(original)).findAny();
            any.ifPresent(subCommand -> subCommand.setAlias(alias));
        });
    }

}
