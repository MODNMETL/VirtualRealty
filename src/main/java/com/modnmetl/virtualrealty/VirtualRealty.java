package com.modnmetl.virtualrealty;

import com.modnmetl.virtualrealty.commands.CommandManager;
import com.modnmetl.virtualrealty.commands.CommandRegistry;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.commands.plot.PlotCommand;
import com.modnmetl.virtualrealty.commands.vrplot.VirtualRealtyCommand;
import com.modnmetl.virtualrealty.configs.*;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.enums.ServerVersion;
import com.modnmetl.virtualrealty.exceptions.MaterialMatchException;
import com.modnmetl.virtualrealty.listeners.player.PlayerActionListener;
import com.modnmetl.virtualrealty.listeners.protection.BorderProtectionListener;
import com.modnmetl.virtualrealty.listeners.PlotEntranceListener;
import com.modnmetl.virtualrealty.listeners.protection.PlotProtectionListener;
import com.modnmetl.virtualrealty.listeners.protection.WorldProtectionListener;
import com.modnmetl.virtualrealty.listeners.stake.DraftListener;
import com.modnmetl.virtualrealty.listeners.stake.ConfirmationListener;
import com.modnmetl.virtualrealty.managers.DynmapManager;
import com.modnmetl.virtualrealty.managers.MetricsManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.managers.PlotMemberManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.registry.VirtualPlaceholders;
import com.modnmetl.virtualrealty.sql.Database;
import com.modnmetl.virtualrealty.utils.Loader;
import com.modnmetl.virtualrealty.utils.configuration.ConfigurationFactory;
import com.modnmetl.virtualrealty.utils.multiversion.VMaterial;
import com.modnmetl.virtualrealty.utils.UpdateChecker;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public final class VirtualRealty extends JavaPlugin {

    //CORE
    public List<JarFile> jarFiles = new ArrayList<>();
    public DynmapManager dynmapManager;
    public Locale locale;
    @Getter
    private static VirtualRealty instance;
    @Getter
    public MetricsManager metricsManager;

    @Getter
    public ConfigurationFactory configFactory = new ConfigurationFactory();
    public PluginConfiguration pluginConfiguration;
    public SizesConfiguration sizesConfiguration;
    public MessagesConfiguration messagesConfiguration;
    public PermissionsConfiguration permissionsConfiguration;
    public CommandsConfiguration commandsConfiguration;
    private static ClassLoader classLoader;
    public static final String PREFIX = "§a§lVR §8§l» §7";
    public static List<BukkitTask> tasks = new ArrayList<>();
    private final List<String> preVersions = Arrays.asList("1.12", "1.11", "1.10", "1.9", "1.8");
    public static boolean legacyVersion = false;
    public static ServerVersion currentServerVersion = ServerVersion.MODERN;
    public static final Permission GLOBAL_PERMISSION = new Permission("virtualrealty");
    @Getter
    @Setter
    private static Object premium;
    public static boolean upToDate;
    public static String latestVersion;

    //FILES
    @Getter
    @Setter
    public static File loaderFile;
    public static File plotsFolder;
    public static File plotsSchemaFolder;
    private final File pluginConfigurationFile = new File(this.getDataFolder(), "config.yml");
    private final File sizesConfigurationFile = new File(this.getDataFolder(), "sizes.yml");
    private final File permissionsConfigurationFile = new File(this.getDataFolder(), "permissions.yml");
    private final File commandsConfigurationFile = new File(this.getDataFolder(), "commands.yml");
    private final File languagesDirectory = new File(this.getDataFolder(), "messages");
    private final File databaseFolder = new File(this.getDataFolder().getAbsolutePath(), File.separator + "data" + File.separator);
    private final File databaseFile = new File(databaseFolder, "data.db");

    @Override
    public void onEnable() {
        classLoader = getClassLoader();
        instance = this;
        try {
            jarFiles.add(new JarFile(getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkLegacyVersions();
        databaseFolder.mkdirs();
        plotsFolder = new File(getInstance().getDataFolder().getAbsolutePath(), "plots");
        plotsFolder.mkdirs();
        plotsSchemaFolder = new File(plotsFolder.getAbsolutePath(), "primary-terrain");
        plotsSchemaFolder.mkdirs();
        reloadConfigs();
        this.locale = new Locale(pluginConfiguration.locale.split("_")[0], pluginConfiguration.locale.split("_")[1]);
        configureMessages();
        if (ServerVersion.valueOf(pluginConfiguration.initServerVersion) == ServerVersion.LEGACY && currentServerVersion == ServerVersion.MODERN) {
            this.getLogger().severe(" » ------------------------------------------------------------------------------------------------ « ");
            this.getLogger().severe(" You cannot migrate existing legacy plots (1.8-1.12) to a non-legacy server version (1.13 and higher)");
            this.getLogger().severe(" » ------------------------------------------------------------------------------------------------ « ");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        if (ServerVersion.valueOf(pluginConfiguration.initServerVersion) == ServerVersion.MODERN && currentServerVersion == ServerVersion.LEGACY) {
            this.getLogger().severe(" » ------------------------------------------------------------------------------------------------ « ");
            this.getLogger().severe(" You cannot migrate existing non-legacy plots (1.13 and higher) to a legacy server version (1.8-1.12)");
            this.getLogger().severe(" » ------------------------------------------------------------------------------------------------ « ");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        String[] updateCheck = UpdateChecker.getUpdate();
        if (updateCheck != null) {
            if (!updateCheck[0].equals(this.getDescription().getVersion())) {
                upToDate = false;
                latestVersion = updateCheck[0];
                this.getLogger().info("A newer version is available!");
                this.getLogger().info("The current version you use: " + this.getDescription().getVersion());
                this.getLogger().info("Latest version available: " + updateCheck[0]);
                this.getLogger().info("Download link: https://www.spigotmc.org/resources/virtual-realty.95599/");
            } else {
                upToDate = true;
                latestVersion = this.getDescription().getVersion();
                this.getLogger().info("Plugin is up to date!");
            }
        }
        if (!pluginConfiguration.license.key.isEmpty() && !pluginConfiguration.license.email.isEmpty()) {
            try {
                new Loader(pluginConfiguration.license.key, pluginConfiguration.license.email, this.getDescription().getVersion(), getLoader(), VirtualRealty.getPluginConfiguration().loaderDebugMode);
            } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                getLogger().log(Level.WARNING, "Load of premium features failed.");
            }
        }
        metricsManager = new MetricsManager(this, 14066);
        metricsManager.registerMetrics();
        loadSizesConfiguration();
        try {
            Database.connectToDatabase(databaseFile);
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to connect to the database.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        PlotManager.loadPlots();
        PlotMemberManager.loadMembers();
        if (pluginConfiguration.dynmapMarkers) {
            dynmapManager = new DynmapManager(this);
            dynmapManager.registerDynmap();
        }
        registerCommands();
        configureCommands();
        registerListeners();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new VirtualPlaceholders(this).register();
            debug("Registered new placeholders");
        }
        debug("Server version: " + this.getServer().getBukkitVersion() + " | " + this.getServer().getVersion());
    }

    @Override
    public void onDisable() {
        try {
            Method method = Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, getLoader()).getMethod("onDisable");
            method.setAccessible(true);
            method.invoke(premium);
        } catch (Exception ignored) {}
        DraftListener.DRAFT_MAP.forEach((player, gridStructureEntryEntry) -> {
            player.getInventory().remove(gridStructureEntryEntry.getValue().getValue().getItemStack());
            player.getInventory().addItem(gridStructureEntryEntry.getValue().getKey().getItemStack());
            gridStructureEntryEntry.getKey().removeGrid();
        });
        DraftListener.DRAFT_MAP.clear();
        PlotManager.getPlots().forEach(Plot::update);
        tasks.forEach(BukkitTask::cancel);
        try {
            DataSource dataSource;
            if (getDatabase() != null && (dataSource = getDatabase().getDataSource()) != null && dataSource.getConnection() != null) {
                if (VirtualRealty.getPluginConfiguration().dataModel == PluginConfiguration.DataModel.MYSQL) {
                    ((HikariDataSource) dataSource).close();
                } else {
                    dataSource.getConnection().close();
                }
            }
        } catch (SQLException ignored) {}
        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.updatePluginConfiguration(pluginConfigurationFile);
        FileUtils.deleteQuietly(loaderFile);
    }

    public static void debug(String message) {
        if (VirtualRealty.getPluginConfiguration().debugMode) VirtualRealty.getInstance().getLogger().warning("DEBUG > " + message);
    }

    public void configureMessages() {
        File messagesConfigurationFile = new File(languagesDirectory, "messages_" + locale.toString() + ".yml");
        ConfigurationFactory configFactory = new ConfigurationFactory();
        configFactory.loadMessagesConfiguration(messagesConfigurationFile);
    }

    public void configureCommands() {
        commandsConfiguration = configFactory.loadCommandsConfiguration(commandsConfigurationFile);
        commandsConfiguration.refreshHelpMessages();
        commandsConfiguration.assignAliases();
        CommandRegistry.setupPlaceholders();
    }

    public void reloadConfigs() {
        try {
            pluginConfiguration = configFactory.loadPluginConfiguration(pluginConfigurationFile);
            File messagesConfigurationFile = new File(languagesDirectory, "messages_" + pluginConfiguration.locale + ".yml");
            sizesConfiguration = configFactory.loadSizesConfiguration(sizesConfigurationFile);
            permissionsConfiguration = configFactory.loadPermissionsConfiguration(permissionsConfigurationFile);
            messagesConfiguration = configFactory.loadMessagesConfiguration(messagesConfigurationFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void registerCommands() {
        PluginCommand plotCommand = this.getCommand("plot");
        assert plotCommand != null;
        plotCommand.setExecutor(new PlotCommand());
        plotCommand.setTabCompleter( new CommandManager());
        registerSubCommands(PlotCommand.class, "panel");

        PluginCommand vrCommand = this.getCommand("virtualrealty");
        assert vrCommand != null;
        vrCommand.setExecutor(new VirtualRealtyCommand());
        vrCommand.setTabCompleter(new CommandManager());
        registerSubCommands(VirtualRealtyCommand.class);
    }

    public void registerSubCommands(Class<?> mainCommandClass, String... names) {
        if (names.length > 0)
            SubCommand.registerSubCommands(names, mainCommandClass);
        for (JarFile jarFile : jarFiles) {
            for (Enumeration<JarEntry> entry = jarFile.entries(); entry.hasMoreElements();) {
                JarEntry jarEntry = entry.nextElement();
                String name = jarEntry.getName().replace("/", ".");
                if (name.endsWith(".class") && name.startsWith(mainCommandClass.getPackage().getName() + ".subcommand.")) {
                    try {
                        Class<?> clazz = Class.forName(name.replaceAll("[.]class", ""), true, getClassLoader());
                        String subcommand = clazz.getSimpleName().toLowerCase().replaceAll("subcommand", "");
                        if (subcommand.isEmpty()) continue;
                        SubCommand.registerSubCommands(new String[]{subcommand}, mainCommandClass);
                    } catch (ClassNotFoundException ignored) {}
                }
            }
        }
    }

    private void registerListeners() {
        new BorderProtectionListener(this);
        new PlotProtectionListener(this);
        new WorldProtectionListener(this);
        new PlotEntranceListener(this);
        new PlayerActionListener(this);
        new DraftListener(this);
        new ConfirmationListener(this);
        try {
            List<Class<?>> classes = new ArrayList<>();
            classes.add(Class.forName("com.modnmetl.virtualrealty.listeners.premium.PanelListener", true, getLoader()));
            for (Class<?> aClass : classes) {
                aClass.getConstructors()[0].newInstance(this);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
        debug("Registered listeners");
    }

    public void loadSizesConfiguration() {
        for (PlotSize plotSize : PlotSize.values()) {
            if (plotSize == PlotSize.CUSTOM) continue;
            SizesConfiguration.PlotSizes.Size classSize = null;
            switch (plotSize) {
                case SMALL: {
                    classSize = sizesConfiguration.plotSizes.SMALL;
                    break;
                }
                case MEDIUM: {
                    classSize = sizesConfiguration.plotSizes.MEDIUM;
                    break;
                }
                case LARGE: {
                    classSize = sizesConfiguration.plotSizes.LARGE;
                    break;
                }
                case AREA: {
                    plotSize.setLength(sizesConfiguration.plotSizes.AREA.length);
                    plotSize.setWidth(sizesConfiguration.plotSizes.AREA.width);
                    plotSize.setHeight(sizesConfiguration.plotSizes.AREA.height);
                    return;
                }
            }
            Material floorMaterial;
            try {
                floorMaterial = VMaterial.catchMaterial(classSize.floorMaterial.toUpperCase());
            } catch (MaterialMatchException e) {
                floorMaterial = VirtualRealty.legacyVersion ? Material.GRASS : Material.GRASS_BLOCK;
                e.printStackTrace();
            }
            Material borderMaterial;
            try {
                borderMaterial = VMaterial.catchMaterial(classSize.borderMaterial.toUpperCase());
            } catch (MaterialMatchException e) {
                borderMaterial = VirtualRealty.legacyVersion ? Material.getMaterial("STEP") : Material.STONE_BRICK_SLAB;
                e.printStackTrace();
            }
            plotSize.setFloorMaterial(floorMaterial);
            plotSize.setFloorData(classSize.floorData);
            plotSize.setBorderMaterial(borderMaterial);
            plotSize.setBorderData(classSize.borderData);
            plotSize.setLength(classSize.length);
            plotSize.setWidth(classSize.width);
            plotSize.setHeight(classSize.height);
        }
        debug("Loaded sizes config");
    }

    public void checkLegacyVersions() {
        for (String preVersion : preVersions) {
            if (Bukkit.getBukkitVersion().toLowerCase().contains(preVersion.toLowerCase())) {
                legacyVersion = true;
                currentServerVersion = ServerVersion.LEGACY;
                return;
            }
        }
    }

    public static PluginConfiguration getPluginConfiguration() {
        return VirtualRealty.getInstance().pluginConfiguration;
    }

    public static File getPluginConfigurationFile() {
        return VirtualRealty.getInstance().pluginConfigurationFile;
    }

    public static SizesConfiguration getSizesConfiguration() {
        return VirtualRealty.getInstance().sizesConfiguration;
    }

    public static File getSizesConfigurationFile() {
        return VirtualRealty.getInstance().sizesConfigurationFile;
    }

    public static MessagesConfiguration getMessages() {
        return getInstance().messagesConfiguration;
    }

    public static PermissionsConfiguration getPermissions() {
        return getInstance().permissionsConfiguration;
    }

    public static CommandsConfiguration getCommands() {
        return getInstance().commandsConfiguration;
    }

    public static DynmapManager getDynmapManager() {
        return getInstance().dynmapManager;
    }

    public static Database getDatabase() {
        return Database.getInstance();
    }

    public static ClassLoader getLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader newClassLoader) {
        classLoader = newClassLoader;
    }

}
