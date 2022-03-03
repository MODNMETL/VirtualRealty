package com.modnmetl.virtualrealty;

import com.modnmetl.virtualrealty.commands.CommandManager;
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
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.managers.PlotMemberManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.registry.VirtualPlaceholders;
import com.modnmetl.virtualrealty.sql.Database;
import com.modnmetl.virtualrealty.utils.configuration.ConfigurationFactory;
import com.modnmetl.virtualrealty.utils.loader.CustomClassLoader;
import com.modnmetl.virtualrealty.utils.multiversion.VMaterial;
import com.modnmetl.virtualrealty.utils.UpdateChecker;
import com.zaxxer.hikari.HikariDataSource;
import de.tr7zw.nbtapi.utils.VersionChecker;
import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public final class VirtualRealty extends JavaPlugin {

    public Locale locale = Locale.getDefault();

    //CORE
    private static VirtualRealty instance;
    private static ClassLoader loader;
    public static final String PREFIX = "§a§lVR §8§l» §7";
    public static LinkedList<BukkitTask> tasks = new LinkedList<>();
    private static final LinkedList<String> preVersions = new LinkedList<>();
    public static boolean legacyVersion = false;
    public static ServerVersion currentServerVersion = ServerVersion.MODERN;
    public static final Permission GLOBAL_PERMISSION = new Permission("virtualrealty");
    private static Object premium;
    public static boolean upToDate;
    public static String latestVersion;

    //FILES
    public static File plotsFolder;
    public static File plotsSchemaFolder;
    public PluginConfiguration pluginConfiguration;
    public SizesConfiguration sizesConfiguration;
    public MessagesConfiguration messagesConfiguration;
    public PermissionsConfiguration permissionsConfiguration;
    private final File pluginConfigurationFile = new File(this.getDataFolder(), "config.yml");
    private final File sizesConfigurationFile = new File(this.getDataFolder(), "sizes.yml");
    private final File permissionsConfigurationFile = new File(this.getDataFolder(), "permissions.yml");
    private final File languagesDirectory = new File(this.getDataFolder(), "messages");
    private final File databaseFolder = new File(this.getDataFolder().getAbsolutePath(), File.separator + "data" + File.separator);
    private final File databaseFile = new File(databaseFolder, "data.db");
    private File loaderFile;

    //DYNMAP API
    public static boolean isDynmapPresent = false;
    public static DynmapAPI dapi = null;
    public static MarkerSet markerset = null;
    public static MarkerIcon markerIcon = null;

    @Override
    public void onEnable() {
        instance = this;
        loader = getClassLoader();
        VersionChecker.hideOk = true;
        if (checkLegacyVersions()) {
            legacyVersion = true;
            currentServerVersion = ServerVersion.LEGACY;
        }
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
                runLoader(new URL("http://license.mineproxy.com/virtualrealty/premium"), pluginConfiguration.license.key, pluginConfiguration.license.email, this.getDescription().getVersion());
            } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                getLogger().log(Level.WARNING, "Loading of premium features failed.");
            }
        }
        registerMetrics();
        loadSizesConfiguration();
        try {
            connectToDatabase();
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to connect to MySQL database.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        PlotManager.loadPlots();
        PlotMemberManager.loadMembers();
        if (pluginConfiguration.dynmapMarkers) {
            registerDynmap();
        }
        registerCommands();
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
            Method method = Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, getCustomClassLoader()).getMethod("onDisable");
            method.setAccessible(true);
            method.invoke(premium);
        } catch (Exception ignored) {
        }
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
        } catch (SQLException ignored) {
        }
        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.updatePluginConfiguration(pluginConfigurationFile);
        FileUtils.deleteQuietly(loaderFile);
    }


    public static void debug(String debugMessage) {
        if (VirtualRealty.getPluginConfiguration().debugMode)
            VirtualRealty.getInstance().getLogger().warning("DEBUG > " + debugMessage);
    }

    private void runLoader(URL url, String licenseKey, String licenseEmail, String pluginVersion) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        url = new URL(url.toString() + "?license=" + licenseKey + "&email=" + licenseEmail + "&version=" + pluginVersion);
        debug("Injecting premium..");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setConnectTimeout(1200);
        httpConn.setReadTimeout(5000);
        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            debug("Authentication error");
            return;
        }
        try (InputStream in = httpConn.getInputStream()) {
            loaderFile = File.createTempFile(String.valueOf(Arrays.asList(new Random().nextInt(9), new Random().nextInt(9), new Random().nextInt(9))), ".tmp");
            FileUtils.deleteQuietly(loaderFile);
            Files.copy(in, Paths.get(loaderFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        }
        URL jarUrl = loaderFile.toURI().toURL();
        loader = new CustomClassLoader(new URL[]{jarUrl}, getClassLoader());
        try {
            Class<?> clazz = Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, loader);
            premium = clazz.newInstance();
            Class.forName("com.modnmetl.virtualrealty.utils.PanelUtil", true, loader);
        } catch (Exception ignored) {
            debug("Premium injection failed");
            return;
        }
        debug("Premium injected");
    }

    public void configureMessages() {
        File messagesConfigurationFile = new File(languagesDirectory, "messages_" + locale.toString() + ".yml");
        ConfigurationFactory configFactory = new ConfigurationFactory();
        configFactory.loadMessagesConfiguration(messagesConfigurationFile);
    }

    public void reloadConfigs() {
        try {
            ConfigurationFactory configFactory = new ConfigurationFactory();
            pluginConfiguration = configFactory.loadPluginConfiguration(pluginConfigurationFile);
            File messagesConfigurationFile = new File(languagesDirectory, "messages_" + pluginConfiguration.locale + ".yml");
            sizesConfiguration = configFactory.loadSizesConfiguration(sizesConfigurationFile);
            permissionsConfiguration = configFactory.loadPermissionsConfiguration(permissionsConfigurationFile);
            messagesConfiguration = configFactory.loadMessagesConfiguration(messagesConfigurationFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void registerDynmap() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
                if (plugin != null) {
                    isDynmapPresent = true;
                }
                if (plugin != null && plugin.isEnabled()) {
                    dapi = (DynmapAPI) plugin;
                    if (dapi.markerAPIInitialized()) {
                        markerset = dapi.getMarkerAPI().getMarkerSet("virtualrealty.plots");
                        if (markerset == null)
                            markerset = dapi.getMarkerAPI().createMarkerSet("virutalrealty.plots", "Plots", dapi.getMarkerAPI().getMarkerIcons(), false);
                        for (MarkerSet markerSet : dapi.getMarkerAPI().getMarkerSets()) {
                            if (markerSet.getMarkerSetLabel().equalsIgnoreCase("Plots")) {
                                markerset = markerSet;
                            }
                        }
                        try {
                            if (dapi.getMarkerAPI().getMarkerIcon("virtualrealty_main_icon") == null) {
                                InputStream in = this.getClass().getResourceAsStream("/ploticon.png");
                                if (in != null && in.available() > 0) {
                                    markerIcon = dapi.getMarkerAPI().createMarkerIcon("virtualrealty_main_icon", "Plots", in);
                                }
                            } else {
                                markerIcon = dapi.getMarkerAPI().getMarkerIcon("virtualrealty_main_icon");
                            }
                        } catch (IOException ignored) {
                        }
                        VirtualRealty.debug("Registering plots markers..");
                        for (Plot plot : PlotManager.getPlots()) {
                            PlotManager.resetPlotMarker(plot);
                        }
                        VirtualRealty.debug("Registered plots markers");
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(this, 20, 20 * 5);
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("plot")).setExecutor(new PlotCommand());
        Objects.requireNonNull(this.getCommand("virtualrealty")).setExecutor(new VirtualRealtyCommand());
        Objects.requireNonNull(this.getCommand("plot")).setTabCompleter(new CommandManager());
        Objects.requireNonNull(this.getCommand("virtualrealty")).setTabCompleter(new CommandManager());
        SubCommand.registerSubCommands(new String[]{"visual", "item"}, VirtualRealtyCommand.class);
        SubCommand.registerSubCommands(new String[]{"panel", "draft", "stake"}, PlotCommand.class);
        SubCommand.registerSubCommands(new String[]{"assign", "create", "info", "list", "reload", "remove", "set", "tp", "unassign"}, VirtualRealtyCommand.class);
        SubCommand.registerSubCommands(new String[]{"add", "gm", "info", "kick", "list", "tp"}, PlotCommand.class);
    }

    private void registerListeners() {
        new BorderProtectionListener(this);
        new PlotProtectionListener(this);
        new WorldProtectionListener(this);
        new PlotEntranceListener(this);
        new PlayerActionListener(this);
        try {
            Class<?> panelListener = Class.forName("com.modnmetl.virtualrealty.listeners.premium.PanelListener", true, loader);
            Class<?> draftListener = Class.forName("com.modnmetl.virtualrealty.listeners.premium.DraftListener", true, loader);
            panelListener.getConstructors()[0].newInstance(this);
            draftListener.getConstructors()[0].newInstance(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
        }
        debug("Registered listeners");
    }

    private void registerMetrics() {
        Metrics metrics = new Metrics(this, 14066);
        metrics.addCustomChart(new SimplePie("used_database", () -> pluginConfiguration.dataModel.name()));
        metrics.addCustomChart(new AdvancedPie("created_plots", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            int smallPlots = 0;
            int mediumPlots = 0;
            int largePlots = 0;
            int customPlots = 0;
            int areas = 0;
            for (Plot plot : PlotManager.getPlots()) {
                switch (plot.getPlotSize()) {
                    case SMALL: {
                        smallPlots++;
                        break;
                    }
                    case MEDIUM: {
                        mediumPlots++;
                        break;
                    }
                    case LARGE: {
                        largePlots++;
                        break;
                    }
                    case CUSTOM: {
                        customPlots++;
                        break;
                    }
                    case AREA: {
                        areas++;
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + plot.getPlotSize());
                }
            }
            valueMap.put("SMALL", smallPlots);
            valueMap.put("MEDIUM", mediumPlots);
            valueMap.put("LARGE", largePlots);
            valueMap.put("CUSTOM", customPlots);
            valueMap.put("AREA", areas);
            return valueMap;
        }));
        debug("Registered metrics");
    }

    private void connectToDatabase() throws SQLException {
        Database database = null;
        if (pluginConfiguration.dataModel == PluginConfiguration.DataModel.SQLITE) {
            database = new Database(databaseFile);
        }
        if (pluginConfiguration.dataModel == PluginConfiguration.DataModel.MYSQL) {
            database = new Database(
                    VirtualRealty.getPluginConfiguration().mysql.hostname,
                    VirtualRealty.getPluginConfiguration().mysql.port,
                    VirtualRealty.getPluginConfiguration().mysql.user,
                    VirtualRealty.getPluginConfiguration().mysql.password,
                    VirtualRealty.getPluginConfiguration().mysql.database
            );
        }
        Database.setInstance(database);
        debug("Connected to database");
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

    public static VirtualRealty getInstance() {
        return instance;
    }

    public static ClassLoader getCustomClassLoader() {
        return loader;
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

    public boolean checkLegacyVersions() {
        setPostVersions();
        for (String preVersion : preVersions) {
            if (Bukkit.getBukkitVersion().toLowerCase().contains(preVersion.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static Locale getLocale() {
        return getInstance().locale;
    }

    public static Database getDatabase() {
        return Database.getInstance();
    }

    private void setPostVersions() {
        preVersions.add("1.12");
        preVersions.add("1.11");
        preVersions.add("1.10");
        preVersions.add("1.9");
        preVersions.add("1.8");
    }

}
