package me.plytki.virtualrealty.loaders;

import me.plytki.virtualrealty.VirtualRealty;;
import org.bukkit.GameMode;
import org.diorite.cfg.annotations.*;
import org.diorite.cfg.annotations.CfgStringStyle.StringStyle;
import org.diorite.cfg.annotations.defaults.CfgDelegateDefault;

@CfgClass(name = "PluginConfiguration")
@CfgDelegateDefault("{new}")
@CfgComment("~-~-~-~-~-~-~-~-~-~-~-~~-~-~-~~ #")
@CfgComment("                                #")
@CfgComment("         Virtual Realty         #")
@CfgComment("                                #")
@CfgComment("~-~-~-~-~-~-~-~-~-~-~-~~-~-~-~~ #")
public class PluginConfiguration {

    @CfgComment(" ")
    @CfgComment("-------------------------")
    @CfgComment("Don't change this value!")
    @CfgName("config-version")
    public final String configVersion = VirtualRealty.getInstance().getDescription().getVersion();

    @CfgComment("-------------------------")
    @CfgComment("Set player gamemode to change when they enter their plot")
    @CfgName("enable-plot-gamemode")
    public boolean enablePlotGameMode = false;

    @CfgComment("Set which gamemode players change to when they enter their plot")
    @CfgName("default-plot-gamemode")
    public String plotGameMode = "SURVIVAL";

    @CfgComment("Set forced change to plot gamemode when players enter their plot")
    @CfgName("force-plot-gamemode")
    public boolean forcePlotGameMode = false;

    public GameMode getGameMode() {
        try {
            return GameMode.valueOf(plotGameMode);
        } catch (Exception e) {
            VirtualRealty.getInstance().getLogger().warning("Couldn't parse plot-gamemode from config.yml\nUsing default: SURVIVAL");
            return GameMode.SURVIVAL;
        }
    }

    @CfgComment("Allow players to build outside of their plots")
    @CfgName("allow-outplot-build")
    public boolean allowOutPlotBuild = true;

    @CfgComment("Type of data recording")
    @CfgComment("H2 - Local database (Automatically started with our plugin)")
    @CfgComment("MYSQL - External database")
    @CfgName("data-model")
    public DataModel dataModel = DataModel.H2;

    @CfgComment("Data required to connect to the database")
    @CfgComment("The plotsTableName section is the name of the VR data table in the database")
    @CfgComment("It is best to change these names only if you really need to (e.g. there is a conflict with another plugin)")
    @CfgComment("To rename tables when you already have some VR data in the database:")
    @CfgComment("1. Turn off the server")
    @CfgComment("2. Change data in VR config")
    @CfgComment("3. Rename database tables using phpMyAdmin for example")
    @CfgName("mysql")
    public MySQL mysql = new MySQL("localhost", 3306, "db", "root", "passwd", true, "vr_plots");

    public enum DataModel {
        H2,
        MYSQL
    }

    public static class MySQL {
        @CfgStringStyle(StringStyle.ALWAYS_QUOTED)
        public String hostname;

        public int port;

        @CfgStringStyle(StringStyle.ALWAYS_QUOTED)
        public String database;

        @CfgStringStyle(StringStyle.ALWAYS_QUOTED)
        public String user;

        @CfgStringStyle(StringStyle.ALWAYS_QUOTED)
        public String password;

        public boolean useSSL;

        @CfgStringStyle(StringStyle.ALWAYS_QUOTED)
        public String plotsTableName;

        public MySQL(String hostname, int port, String database, String user, String password, boolean useSSL, String plotsTableName) {
            this.hostname = hostname;
            this.port = port;
            this.database = database;
            this.user = user;
            this.password = password;
            this.useSSL = useSSL;
            this.plotsTableName = plotsTableName;
        }

        private MySQL() {}

    }

}
