package com.modnmetl.virtualrealty.util.configuration;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.configs.*;
import com.modnmetl.virtualrealty.configs.migration.C0001_Remove_Old_Plot_Sub_Commands;
import eu.okaeri.configs.ConfigManager;
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
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public SizesConfiguration loadSizesConfiguration(File sizesConfigurationFile) {
        return ConfigManager.create(SizesConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(sizesConfigurationFile);
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public MessagesConfiguration loadMessagesConfiguration(File messagesConfigurationFile) {
        return ConfigManager.create(MessagesConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(messagesConfigurationFile);
            it.withRemoveOrphans(true);
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
            it.withLogger(VirtualRealty.getInstance().getLogger());
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
            it.migrate(
                    new C0001_Remove_Old_Plot_Sub_Commands()
            );
        });
    }

    public PermissionsConfiguration updatePluginConfiguration(File pluginConfigurationFile) {
        return ConfigManager.create(PermissionsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(pluginConfigurationFile);
            it.withRemoveOrphans(true);
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

    public CommandsConfiguration updateCommandsConfiguration(File commandsConfigurationFile) {
        return ConfigManager.create(CommandsConfiguration.class, (it) -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit());
            it.withBindFile(commandsConfigurationFile);
            it.withRemoveOrphans(true);
            it.update();
        });
    }

}
