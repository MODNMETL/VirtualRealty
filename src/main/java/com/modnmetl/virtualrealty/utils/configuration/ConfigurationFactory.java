package com.modnmetl.virtualrealty.utils.configuration;

import com.modnmetl.virtualrealty.configs.*;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.postprocessor.SectionSeparator;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor
public class ConfigurationFactory {

    public PluginConfiguration loadPluginConfiguration(File pluginConfigurationFile) {
        return ConfigManager.create(PluginConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(pluginConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public SizesConfiguration loadSizesConfiguration(File sizesConfigurationFile) {
        return ConfigManager.create(SizesConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(sizesConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public MessagesConfiguration loadMessagesConfiguration(File messagesConfigurationFile) {
        return ConfigManager.create(MessagesConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(messagesConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public PermissionsConfiguration loadPermissionsConfiguration(File permissionsConfigurationFile) {
        return ConfigManager.create(PermissionsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(permissionsConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public CommandsConfiguration loadCommandsConfiguration(File commandsConfigurationFile) {
        return ConfigManager.create(CommandsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(commandsConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public PermissionsConfiguration updatePluginConfiguration(File pluginConfigurationFile) {
        return ConfigManager.create(PermissionsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(pluginConfigurationFile);
            it.saveDefaults();
            it.update();
        });
    }

    public PermissionsConfiguration updateSizesConfiguration(File sizesConfigurationFile) {
        return ConfigManager.create(PermissionsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(sizesConfigurationFile);
            it.saveDefaults();
            it.update();
        });
    }

    public PermissionsConfiguration updateMessagesConfiguration(File messagesConfigurationFile) {
        return ConfigManager.create(PermissionsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(messagesConfigurationFile);
            it.saveDefaults();
            it.update();
        });
    }

    public PermissionsConfiguration updatePermissionsConfiguration(File permissionsConfigurationFile) {
        return ConfigManager.create(PermissionsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(permissionsConfigurationFile);
            it.saveDefaults();
            it.update();
        });
    }

}
