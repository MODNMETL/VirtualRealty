package me.plytki.virtualrealty.utils;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import me.plytki.virtualrealty.configs.PluginConfiguration;
import me.plytki.virtualrealty.configs.SizesConfiguration;

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


}
