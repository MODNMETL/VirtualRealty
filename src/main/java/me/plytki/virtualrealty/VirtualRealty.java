package me.plytki.virtualrealty;

import me.plytki.virtualrealty.commands.PlotCommand;
import me.plytki.virtualrealty.commands.VirtualRealtyCommand;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.listeners.PlotListener;
import me.plytki.virtualrealty.listeners.PlotProtectionListener;
import me.plytki.virtualrealty.listeners.WorldListener;
import me.plytki.virtualrealty.loaders.PluginConfiguration;
import me.plytki.virtualrealty.loaders.SizesConfiguration;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.sql.SQL;
import me.plytki.virtualrealty.tasks.PlotExpireTask;
import me.plytki.virtualrealty.utils.ConfigUtil;
import me.plytki.virtualrealty.utils.UpdateChecker;
import me.plytki.virtualrealty.utils.multiversion.VMaterial;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public final class VirtualRealty extends JavaPlugin {

    private static VirtualRealty instance;
    public static final String PREFIX = "§a§lVR §8§l» §7";
    public static ArrayList<BukkitTask> tasks = new ArrayList<>();

    public static File plotsFolder;
    public static File plotsSchemaFolder;

    private PluginConfiguration pluginConfiguration;
    private SizesConfiguration sizesConfiguration;
    private final File pluginConfigurationFile  = new File(this.getDataFolder(), "config.yml");
    private final File sizesConfigurationFile  = new File(this.getDataFolder(), "sizes.yml");

    private static final ArrayList<String> postVersions = new ArrayList<>();
    private boolean configError = false;

    public static boolean isLegacy = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        if (!checkLegacyVersions()) {
            isLegacy = true;
        }
        String[] updateCheck = UpdateChecker.getUpdate();
        if (updateCheck != null) {
            if (!updateCheck[0].equals(this.getDescription().getVersion())) {
                this.getLogger().info("A new version is available!");
                this.getLogger().info("Current version you're using: " + this.getDescription().getVersion());
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
        registerMetrics();
        plotsFolder = new File(getInstance().getDataFolder().getAbsolutePath(), "plots");
        plotsFolder.mkdirs();
        plotsSchemaFolder = new File(plotsFolder.getAbsolutePath(), "primary-terrain");
        plotsSchemaFolder.mkdirs();
        try {
            this.pluginConfiguration = ConfigUtil.loadConfig(this.pluginConfigurationFile, PluginConfiguration.class);
            this.sizesConfiguration = ConfigUtil.loadConfig(this.sizesConfigurationFile, SizesConfiguration.class);
        } catch (Exception exception) {
            exception.printStackTrace();
            configError = true;
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //createSizesConfig();
        loadSizesConfiguration();
        connectToDatabase();
        registerCommands();
        registerListeners();
        registerTasks();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (configError) {
            return;
        }
        tasks.forEach(BukkitTask::cancel);
        SQL.closeConnection();
    }

    private void registerCommands() {
        this.getCommand("plot").setExecutor(new PlotCommand());
        this.getCommand("virtualrealty").setExecutor(new VirtualRealtyCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlotListener(), this);
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
    }

    private void registerTasks() {
        tasks.add(new PlotExpireTask().runTaskTimer(this, 20 * 30, 20 * 30));
    }

    private void registerMetrics() {
        Metrics metrics = new Metrics(this, 12578);
        metrics.addCustomChart(new SimplePie("used_database", () -> this.getConfig().getString("data-storage")));
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
    }

    private void connectToDatabase() {
        SQL.connect();
        SQL.createTables();
        PlotManager.loadPlots();
    }

    public static void loadSizesConfiguration() {
        for (PlotSize plotSize : PlotSize.values()) {
            if (plotSize == PlotSize.CUSTOM) return;
            SizesConfiguration.Size classSize = null;
            switch (plotSize) {
                case SMALL: {
                    classSize = getInstance().sizesConfiguration.plotSizes.SMALL;
                    break;
                }
                case MEDIUM: {
                    classSize = getInstance().sizesConfiguration.plotSizes.MEDIUM;
                    break;
                }
                case LARGE: {
                    classSize = getInstance().sizesConfiguration.plotSizes.LARGE;
                    break;
                }
            }
            Material floorMaterial;
            try {
                floorMaterial = VMaterial.getMaterial(classSize.floorMaterial.toUpperCase());
                floorMaterial.name();
            } catch (Exception e) {
                floorMaterial = VirtualRealty.isLegacy ? Material.GRASS : Material.GRASS_BLOCK;
                VirtualRealty.getInstance().getLogger().warning("Couldn't parse floor-material from sizes.yml | Using default: " + (VirtualRealty.isLegacy ? Material.GRASS : Material.GRASS_BLOCK));
            }
            Material borderMaterial;
            try {
                borderMaterial = VMaterial.getMaterial(classSize.borderMaterial.toUpperCase());
                borderMaterial.name();
            } catch (Exception e) {
                borderMaterial = VirtualRealty.isLegacy ? Material.getMaterial("STEP") : Material.STONE_BRICK_SLAB;
                VirtualRealty.getInstance().getLogger().warning("Couldn't parse border-material from sizes.yml | Using default: " + (VirtualRealty.isLegacy ? Material.getMaterial("STEP") : Material.STONE_BRICK_SLAB));
            }
            plotSize.setFloorMaterial(floorMaterial);
            plotSize.setFloorData(classSize.floorData);
            plotSize.setBorderMaterial(borderMaterial);
            plotSize.setBorderData(classSize.borderData);
            plotSize.setLength(classSize.length);
            plotSize.setWidth(classSize.width);
            plotSize.setHeight(classSize.height);
        }
    }

    public static VirtualRealty getInstance() {
        return instance;
    }

    public static PluginConfiguration getPluginConfiguration() {
        return getInstance().pluginConfiguration;
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

    public void setPostVersions() {
        postVersions.add("1.17");
        postVersions.add("1.16");
        postVersions.add("1.15");
        postVersions.add("1.14");
        postVersions.add("1.13");
    }

    public void checkConfig() throws IOException {
        File oldConfigFile = new File(this.getDataFolder().getAbsolutePath(), "config.yml");
        if (!oldConfigFile.exists()) return;
        String version = null;
        boolean isOldVersion = true;
        boolean updateConfigVersion = false;
        BufferedReader reader = new BufferedReader(new FileReader(this.pluginConfigurationFile));
        String latestLine;
        while((latestLine = reader.readLine()) != null) {
            if (latestLine.contains("config-version")) {
                version = latestLine.replaceAll("config-version: ", "");
                isOldVersion = false;
            }
        }
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
            File newConfigFile = new File(this.getDataFolder().getAbsolutePath(), "config.yml");
            FileUtils.deleteQuietly(newConfigFile);
            FileUtils.writeLines(newConfigFile, lines);
            newConfigFile.createNewFile();
        }
    }

    public void checkSizesConfig() throws IOException {
        File oldConfigFile = new File(this.getDataFolder().getAbsolutePath(), "sizes.yml");
        if (!oldConfigFile.exists()) return;
        String version = null;
        boolean isOldVersion = true;
        boolean updateConfigVersion = false;
        BufferedReader reader = new BufferedReader(new FileReader(this.pluginConfigurationFile));
        String latestLine;
        while((latestLine = reader.readLine()) != null) {
            if (latestLine.contains("config-version")) {
                version = latestLine.replaceAll("config-version: ", "");
                isOldVersion = false;
            }
        }
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
