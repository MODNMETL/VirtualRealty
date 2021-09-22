package me.plytki.virtualrealty;

import me.plytki.virtualrealty.commands.PlotCommand;
import me.plytki.virtualrealty.commands.VirtualRealtyCommand;
import me.plytki.virtualrealty.configs.MessagesConfiguration;
import me.plytki.virtualrealty.configs.PluginConfiguration;
import me.plytki.virtualrealty.configs.SizesConfiguration;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.exceptions.MaterialMatchException;
import me.plytki.virtualrealty.listeners.plot.BorderListener;
import me.plytki.virtualrealty.listeners.plot.PlotListener;
import me.plytki.virtualrealty.listeners.plot.ProtectionListener;
import me.plytki.virtualrealty.listeners.world.WorldListener;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.sql.SQL;
import me.plytki.virtualrealty.tasks.PlotExpireTask;
import me.plytki.virtualrealty.utils.ConfigurationFactory;
import me.plytki.virtualrealty.utils.SchematicUtil;
import me.plytki.virtualrealty.utils.UpdateChecker;
import me.plytki.virtualrealty.utils.multiversion.VMaterial;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public final class VirtualRealty extends JavaPlugin {

    public final Locale locale = Locale.getDefault();
    public final List<Locale> availableLocales = new ArrayList<>(Arrays.asList(new Locale("en", "GB"), new Locale("es", "ES"), new Locale("pl", "PL")));

    //CORE
    private static VirtualRealty instance;
    public static final String PREFIX = "§a§lVR §8§l» §7";
    public static ArrayList<BukkitTask> tasks = new ArrayList<>();
    private static final ArrayList<String> postVersions = new ArrayList<>();
    public static boolean isLegacy = false;

    //FILES
    public static File plotsFolder;
    public static File plotsSchemaFolder;
    public PluginConfiguration pluginConfiguration;
    public SizesConfiguration sizesConfiguration;
    public MessagesConfiguration messagesConfiguration;
    private final File pluginConfigurationFile = new File(this.getDataFolder(), "config.yml");
    private final File sizesConfigurationFile = new File(this.getDataFolder(), "sizes.yml");
    private final File languagesDirectory = new File(this.getDataFolder(), "messages");

    //DYNMAP API
    public static boolean isDynmapPresent = false;
    public static DynmapAPI dapi = null;
    public static MarkerSet markerset = null;
    public static MarkerIcon markerIcon = null;

    @Override
    public void onEnable() {
        instance = this;
        if (!checkLegacyVersions()) {
            isLegacy = true;
        }
        String[] updateCheck = UpdateChecker.getUpdate();
        if (updateCheck != null) {
            if (!updateCheck[0].equals(this.getDescription().getVersion())) {
                this.getLogger().info("A newer version is available!");
                this.getLogger().info("The current version you use: " + this.getDescription().getVersion());
                this.getLogger().info("Latest version available: " + updateCheck[0]);
                this.getLogger().info("Download link: https://www.spigotmc.org/resources/virtual-realty.95599/");
            } else {
                this.getLogger().info("Plugin is up to date!");
            }
        }
        try {
            checkConfig();
            checkSizesConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        plotsFolder = new File(getInstance().getDataFolder().getAbsolutePath(), "plots");
        plotsFolder.mkdirs();
        plotsSchemaFolder = new File(plotsFolder.getAbsolutePath(), "primary-terrain");
        plotsSchemaFolder.mkdirs();
        spawnLocales();
        reloadConfigs();
        registerMetrics();
        loadSizesConfiguration();
        connectToDatabase();
        PlotManager.loadPlots();
        if (pluginConfiguration.dynmapMarkers) {
            registerDynmap();
        }
        registerCommands();
        registerListeners();
        registerTasks();
        checkForOldSchemas();
        debug("Server version: " + this.getServer().getBukkitVersion() + " | " + this.getServer().getVersion());
    }

    @Override
    public void onDisable() {
        PlotManager.plots.forEach(Plot::update);
        tasks.forEach(BukkitTask::cancel);
        SQL.closeConnection();
    }

    public static void debug(String debugMessage) {
        if (VirtualRealty.getPluginConfiguration().debugMode)
            VirtualRealty.getInstance().getLogger().warning("DEBUG-MODE > " + debugMessage);
    }

    public void spawnLocales() {
        for (Locale availableLocale : availableLocales) {
            if (availableLocale.toString().equalsIgnoreCase("en_GB")) {
                File messagesConfigurationFile = new File(languagesDirectory, "messages_en_GB.yml");
                ConfigurationFactory configFactory = new ConfigurationFactory();
                configFactory.createMessagesConfiguration(messagesConfigurationFile);
            } else {
                File languageConfigurationFile = new File(languagesDirectory, "messages_" + availableLocale + ".yml");
                if (!languageConfigurationFile.exists()) {
                    saveResource("messages_" + availableLocale + ".yml", true);
                    File file = new File(this.getDataFolder(), "messages_" + availableLocale + ".yml");
                    try {
                        FileUtils.moveFile(file, languageConfigurationFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void reloadConfigs() {
        try {
            ConfigurationFactory configFactory = new ConfigurationFactory();
            pluginConfiguration = configFactory.createPluginConfiguration(pluginConfigurationFile);
            File messagesConfigurationFile = new File(languagesDirectory, "messages_" + pluginConfiguration.locale + ".yml");
            sizesConfiguration = configFactory.createSizesConfiguration(sizesConfigurationFile);
            messagesConfiguration = configFactory.createMessagesConfiguration(messagesConfigurationFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void checkForOldSchemas() {
        for (Plot plot : PlotManager.plots) {
            File f = new File(VirtualRealty.plotsSchemaFolder, "plot" + plot.getID() + ".schem");
            if (f.exists()) {
                List<String> data = SchematicUtil.oldLoad(plot.getID());
                FileUtils.deleteQuietly(f);
                SchematicUtil.save(plot.getID(), data.toArray(new String[0]));
                debug("Converted Plot #" + plot.getID() + " | File: " + f.getName());
            }
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
                                if (in.available() > 0) {
                                    markerIcon = dapi.getMarkerAPI().createMarkerIcon("virtualrealty_main_icon", "Plots", in);
                                }
                            }
                            else {
                                 markerIcon = dapi.getMarkerAPI().getMarkerIcon("virtualrealty_main_icon");
                            }
                        }
                        catch (IOException ex) {}
                        VirtualRealty.debug("Registering plots markers..");
                        for (Plot plot : PlotManager.plots) {
                            PlotManager.resetPlotMarker(plot);
                        }
                        VirtualRealty.debug("Registered plots markers");
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(this, 20, 20*5);
    }


    private void registerCommands() {
        this.getCommand("plot").setExecutor(new PlotCommand());
        this.getCommand("virtualrealty").setExecutor(new VirtualRealtyCommand());
    }

    private void registerListeners() {
        new BorderListener(this).registerEvents();
        new PlotListener(this).registerEvents();
        new ProtectionListener(this).registerEvents();
        new WorldListener(this).registerEvents();
        debug("Registered listeners");
    }

    private void registerTasks() {
        tasks.add(new PlotExpireTask().runTaskTimer(this, 20 * 30, 20 * 30));
        debug("Registered tasks");
    }

    private void registerMetrics() {
        Metrics metrics = new Metrics(this, 12578);
        metrics.addCustomChart(new SimplePie("used_database", () -> pluginConfiguration.dataModel.name()));
        metrics.addCustomChart(new AdvancedPie("created_plots", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<String, Integer>();
                int smallPlots = 0;
                int mediumPlots = 0;
                int largePlots = 0;
                int customPlots = 0;
                for (Plot plot : PlotManager.plots) {
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
                        default:
                            throw new IllegalStateException("Unexpected value: " + plot.getPlotSize());
                    }
                }
                valueMap.put("SMALL", smallPlots);
                valueMap.put("MEDIUM", mediumPlots);
                valueMap.put("LARGE", largePlots);
                valueMap.put("CUSTOM", customPlots);
                return valueMap;
            }
        }));
        debug("Registered metrics");
    }

    private void connectToDatabase() {
        SQL.connect();
        SQL.createTables();
        debug("Connected to database");
    }

    public void loadSizesConfiguration() {
        for (PlotSize plotSize : PlotSize.values()) {
            if (plotSize == PlotSize.CUSTOM) return;
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
            }
            Material floorMaterial;
            try {
                floorMaterial = VMaterial.catchMaterial(classSize.floorMaterial.toUpperCase());
            } catch (MaterialMatchException e) {
                floorMaterial = VirtualRealty.isLegacy ? Material.GRASS : Material.GRASS_BLOCK;
                e.printStackTrace();
                //throw new MaterialMatchException("Couldn't parse floor-material from sizes.yml | Using default: " + (VirtualRealty.isLegacy ? Material.GRASS : Material.GRASS_BLOCK));
            }
            Material borderMaterial;
            try {
                borderMaterial = VMaterial.catchMaterial(classSize.borderMaterial.toUpperCase());
            } catch (MaterialMatchException e) {
                borderMaterial = VirtualRealty.isLegacy ? Material.getMaterial("STEP") : Material.STONE_BRICK_SLAB;
                e.printStackTrace();
                //throw new MaterialMatchException("Couldn't parse border-material from sizes.yml | Using default: " + (VirtualRealty.isLegacy ? Material.getMaterial("STEP") : Material.STONE_BRICK_SLAB));
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

    public boolean checkLegacyVersions() {
        setPostVersions();
        for (String postVersion : postVersions) {
            if (Bukkit.getBukkitVersion().toLowerCase().contains(postVersion.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static Locale getLocale() {
        return getInstance().locale;
    }

    public void setPostVersions() {
        postVersions.add("1.17");
        postVersions.add("1.16");
        postVersions.add("1.15");
        postVersions.add("1.14");
        postVersions.add("1.13");
    }

    public void checkConfig() throws IOException {
        File oldConfigFile = new File(this.getDataFolder(), "config.yml");
        if (!oldConfigFile.exists()) return;
        String version = null;
        boolean isOldVersion = true;
        boolean updateConfigVersion = false;
        FileReader fileReader = new FileReader(this.pluginConfigurationFile);
        BufferedReader reader = new BufferedReader(fileReader);
        String latestLine;
        while((latestLine = reader.readLine()) != null) {
            if (latestLine.contains("config-version")) {
                version = latestLine.replaceAll("config-version: ", "");
                isOldVersion = false;
            }
        }
        fileReader.close();
        reader.close();
        if (version == null) {
            System.err.println(" ");
            this.getLogger().warning("Config has been reset due to major config changes!");
            this.getLogger().warning("Old config has been renamed to config.yml.old");
            this.getLogger().warning("Please update your config file!");
            System.err.println(" ");
        } else if (!version.equalsIgnoreCase(VirtualRealty.getInstance().getDescription().getVersion())) {
            updateConfigVersion = true;
            this.getLogger().info("Config has been updated!");
        }

        // save old config file
        if (isOldVersion) {
            File newConfigFile = new File(this.getDataFolder().getAbsolutePath(), "config.yml.old");
            if (newConfigFile.exists()) {
                newConfigFile.delete();
            }
            FileUtils.copyFile(oldConfigFile, newConfigFile);
            oldConfigFile.delete();
        }

//         update config version
        if (updateConfigVersion) {
            List<String> lines = new ArrayList<>();
            LineIterator iterator = FileUtils.lineIterator(oldConfigFile);
            while (iterator.hasNext()) {
                String line = iterator.next();
                lines.add(line);
            }
            for (String line : new ArrayList<>(lines)) {
                if (line.contains("config-version")) {
                    int index = lines.indexOf(line);
                    lines.set(index, "config-version: " + VirtualRealty.getInstance().getDescription().getVersion());
                }
            }
            File newConfigFile = new File(this.getDataFolder().getAbsolutePath(), "config.yml");
            FileUtils.deleteQuietly(newConfigFile);
            FileUtils.writeLines(newConfigFile, lines);
            newConfigFile.createNewFile();
        }
    }

    public void checkSizesConfig() throws IOException {
        File oldConfigFile = new File(this.getDataFolder(), "sizes.yml");
        if (!oldConfigFile.exists()) return;
        String version = null;
        boolean isOldVersion = true;
        boolean updateConfigVersion = false;
        BufferedReader reader = new BufferedReader(new FileReader(this.sizesConfigurationFile));
        String latestLine;
        while((latestLine = reader.readLine()) != null) {
            if (latestLine.contains("config-version")) {
                version = latestLine.replaceAll("config-version: ", "");
                isOldVersion = false;
            }
        }
        reader.close();
        if (version == null) {
            System.err.println(" ");
            this.getLogger().warning("Config has been reset due to major config changes!");
            this.getLogger().warning("Old config has been renamed to sizes.yml.old");
            this.getLogger().warning("Please update your config file!");
            System.err.println(" ");
        } else if (!version.equalsIgnoreCase(VirtualRealty.getInstance().getDescription().getVersion())) {
            updateConfigVersion = true;
            this.getLogger().info("Plot sizes config has been updated!");
        }

        // save old config file
        if (isOldVersion) {
            File newConfigFile = new File(this.getDataFolder().getAbsolutePath(), "sizes.yml.old");
            if (newConfigFile.exists()) {
                newConfigFile.delete();
            }
            FileUtils.copyFile(oldConfigFile, newConfigFile);
            oldConfigFile.delete();
        }

        // update config version
        if (updateConfigVersion) {
            List<String> lines = new ArrayList<>();
            LineIterator iterator = FileUtils.lineIterator(oldConfigFile);
            while (iterator.hasNext()) {
                String line = iterator.next();
                lines.add(line);
            }
            for (String line : new ArrayList<>(lines)) {
                if (line.contains("config-version")) {
                    int index = lines.indexOf(line);
                    lines.set(index, "config-version: " + VirtualRealty.getInstance().getDescription().getVersion());
                }
            }
            File newConfigFile = new File(this.getDataFolder().getAbsolutePath(), "sizes.yml");
            FileUtils.deleteQuietly(newConfigFile);
            FileUtils.writeLines(newConfigFile, lines);
            newConfigFile.createNewFile();
        }
    }

}
