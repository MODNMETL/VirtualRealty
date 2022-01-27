package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.configs.SizesConfiguration;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import com.modnmetl.virtualrealty.configs.MessagesConfiguration;
import com.modnmetl.virtualrealty.configs.PluginConfiguration;

import java.io.File;

public class ConfigurationFactory {

    public ConfigurationFactory() {

    }

    public PluginConfiguration createPluginConfiguration(File pluginConfigurationFile) {
        return ConfigManager.create(PluginConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesCommons());
            it.withBindFile(pluginConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public SizesConfiguration createSizesConfiguration(File sizesConfigurationFile) {
        return ConfigManager.create(SizesConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesCommons());
            it.withBindFile(sizesConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

    public MessagesConfiguration createMessagesConfiguration(File messagesConfigurationFile) {
        return ConfigManager.create(MessagesConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesCommons());
            it.withBindFile(messagesConfigurationFile);
            it.saveDefaults();
            it.load(true);
        });
    }

}
