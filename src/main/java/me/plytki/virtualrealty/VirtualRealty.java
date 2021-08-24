package me.plytki.virtualrealty;

import me.plytki.virtualrealty.commands.PlotCommand;
import me.plytki.virtualrealty.commands.VirtualRealtyCommand;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.exceptions.IncompatibleVersionException;
import me.plytki.virtualrealty.listeners.PlotListener;
import me.plytki.virtualrealty.listeners.PlotProtectionListener;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.sql.SQL;
import me.plytki.virtualrealty.tasks.PlotExpireTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class VirtualRealty extends JavaPlugin {

    private static final String PLUGIN_VERSION = "V1.2.1_SNAPSHOT";
    private static VirtualRealty instance;
    public static final String PREFIX = "§a§lVR §8§l» §7";
    public static ArrayList<BukkitTask> tasks = new ArrayList<>();

    private File sizesFile = new File(this.getDataFolder().getAbsolutePath() + "/sizes.yml");
    private FileConfiguration sizesConfiguration;
    public static File plotsFolder;
    public static File plotsSchemaFolder;
    
    private static ArrayList<String> compatibleVersions = new ArrayList<>();
    private boolean closedDueToIncompatibleVersion = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        //this.getServer().getConsoleSender().sendMessage("");
        setCompatibleVersions();
        if (!checkCompatibleVersion(Bukkit.getBukkitVersion())) {
            closedDueToIncompatibleVersion = true;
            try {
                throw new IncompatibleVersionException("You have installed the wrong VirtualRealty plugin for your server version - The correct plugin to use is §a§ohttps://www.spigotmc.org/resources/virtual-realty.95599/");
            } catch (IncompatibleVersionException e) {
                e.printStackTrace();
            }
            Bukkit.getServer().getPluginManager().disablePlugin(instance);
            return;
        }
        plotsFolder = new File(getInstance().getDataFolder().getAbsolutePath(), "plots");
        plotsFolder.mkdirs();
        plotsSchemaFolder = new File(plotsFolder.getAbsolutePath(), "primary-terrain");
        plotsSchemaFolder.mkdirs();
        saveDefaultConfig();
        createSizesConfig();
        loadSizesConfiguration();
        connectToDatabase();
        registerCommands();
        registerListeners();
        registerTasks();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (closedDueToIncompatibleVersion) {
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
    }

    private void registerTasks() {
        tasks.add(new PlotExpireTask().runTaskTimer(this, 20 * 30, 20 * 30));
    }

    private void connectToDatabase() {
        SQL.connect();
        SQL.createTables();
        PlotManager.loadPlots();
    }

    private void createSizesConfig() {
        sizesFile = new File(getDataFolder(), "sizes.yml");
        if (!sizesFile.exists()) {
            sizesFile.getParentFile().mkdirs();
            saveResource("sizes.yml", false);
        }
        sizesConfiguration = new YamlConfiguration();
        try {
            sizesConfiguration.load(sizesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void loadSizesConfiguration() {
        for (PlotSize plotSize : PlotSize.values()) {
            if (plotSize == PlotSize.CUSTOM) return;
            plotSize.setLength(getInstance().sizesConfiguration.getInt("plots-sizes." + plotSize.name() + ".size.length"));
            plotSize.setWidth(getInstance().sizesConfiguration.getInt("plots-sizes." + plotSize.name() + ".size.width"));
            plotSize.setHeight(getInstance().sizesConfiguration.getInt("plots-sizes." + plotSize.name() + ".size.height"));
            plotSize.setFloorMaterial(Material.valueOf(getInstance().sizesConfiguration.getString("plots-sizes." + plotSize.name() + ".floorMaterial")));
        }
    }

    public FileConfiguration getSizesConfiguration() {
        return this.sizesConfiguration;
    }

    public static VirtualRealty getInstance() {
        return instance;
    }

    public boolean checkCompatibleVersion(String version) {
        for (String compatibleVersion : compatibleVersions) {
            if (version.toLowerCase().contains(compatibleVersion.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    public void setCompatibleVersions() {
        compatibleVersions.add("1.17");
        compatibleVersions.add("1.16");
        compatibleVersions.add("1.15");
        compatibleVersions.add("1.14");
        compatibleVersions.add("1.13");
    }

}
