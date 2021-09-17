package me.plytki.virtualrealty.configs;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.HighlightType;
import org.bukkit.GameMode;

import java.awt.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Header("################################################################")
@Header("#                                                              #")
@Header("#                       Virtual Realty                         #")
@Header("#                                                              #")
@Header("################################################################")
@Names(strategy = NameStrategy.IDENTITY, modifier = NameModifier.TO_LOWER_CASE)
public class PluginConfiguration extends OkaeriConfig {

    @Comment(" ")
    @Comment("-------------------------")
    @Comment("Don't change this value!")
    @CustomKey("config-version")
    public String configVersion = VirtualRealty.getInstance().getDescription().getVersion();

    @Comment("-------------------------")
    @Comment("Debug mode (Dev only)")
    @CustomKey("debug-mode")
    public boolean debugMode = false;

    @Comment("Set player gamemode to change when they enter their plot")
    @CustomKey("enable-plot-gamemode")
    public boolean enablePlotGameMode = false;

    @Comment("Set which gamemode players change to when they enter their plot")
    @CustomKey("default-plot-gamemode")
    public String plotGameMode = "SURVIVAL";

    @Comment("Set forced change to plot gamemode when players enter their plot")
    @CustomKey("force-plot-gamemode")
    public boolean forcePlotGameMode = false;

    public GameMode getGameMode() {
        try {
            return GameMode.valueOf(plotGameMode);
        } catch (Exception e) {
            VirtualRealty.getInstance().getLogger().warning("Couldn't parse plot-gamemode from config.yml\nUsing default: SURVIVAL");
            return GameMode.SURVIVAL;
        }
    }

    @Comment("Allow players to build outside of their plots")
    @CustomKey("allow-outplot-build")
    public boolean allowOutPlotBuild = true;

    @Comment("Enables dynmap plots highlighting")
    @CustomKey("enable-dynmap-markers")
    public boolean dynmapMarkers = false;

    @Comment("Choose which type of plots should be highlighted on Dynmap page | Choose from: { ALL, AVAILABLE, OWNED }")
    @CustomKey("dynmap-type")
    public HighlightType dynmapType = HighlightType.ALL;

    @CustomKey("dynmap-markers")
    public MarkerColor dynmapMarkersColor = new MarkerColor(new MarkerColor.Available("#80eb34", .3), new MarkerColor.Owned("#ffbf00", .45));

    @Names(strategy = NameStrategy.IDENTITY)
    public static class MarkerColor extends OkaeriConfig {

        public Available available;

        public Owned owned;

        public MarkerColor(Available available, Owned owned) {
            this.available = available;
            this.owned = owned;
        }

        @Names(strategy = NameStrategy.IDENTITY)
        public static class Available extends OkaeriConfig {

            public String color;

            public double opacity;

            public Available(String color, double opacity) {
                this.color = color;
                this.opacity = opacity;
            }

            public int getHexColor() {
                return Integer.decode("0x" + color.replaceAll("#", ""));
            }

        }

        @Names(strategy = NameStrategy.IDENTITY)
        public static class Owned extends OkaeriConfig {

            public String color;

            public double opacity;

            public Owned(String color, double opacity) {
                this.color = color;
                this.opacity = opacity;
            }

            public int getHexColor() {
                return Integer.decode("0x" + color.replaceAll("#", ""));
            }

        }

    }

    @Comment("Enables plots enter/leave sounds")
    @CustomKey("plot-sounds")
    public boolean plotSound = true;

    @Comment("Type of data recording")
    @Comment("H2 - Local database (Automatically started with our plugin)")
    @Comment("MYSQL - External database")
    @CustomKey("data-model")
    public DataModel dataModel = DataModel.H2;

    @Comment("Data required to connect to the database")
    @Comment("The plotsTableName section is the name of the VR data table in the database")
    @Comment("It is best to change these names only if you really need to (e.g. there is a conflict with another plugin)")
    @Comment("To rename tables when you already have some VR data in the database:")
    @Comment("1. Turn off the server")
    @Comment("2. Change data in VR config")
    @Comment("3. Rename database tables using phpMyAdmin for example")
    @CustomKey("mysql")
    public MySQL mysql = new MySQL("localhost", 3306, "db", "root", "passwd", true, "vr_plots");

    public enum DataModel {
        H2,
        MYSQL
    }

    @Names(strategy = NameStrategy.IDENTITY)
    public static class MySQL extends OkaeriConfig {

        @Variable("VR_MYSQL_HOSTNAME")
        public String hostname;
        @Variable("VR_MYSQL_PORT")
        public int port;
        @Variable("VR_MYSQL_DATABASE")
        public String database;
        @Variable("VR_MYSQL_USER")
        public String user;
        @Variable("VR_MYSQL_PASSWORD")
        public String password;
        @Variable("VR_MYSQL_USE_SSL")
        public boolean useSSL;
        @Variable("VR_MYSQL_USERS_TABLE_NAME")
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
    }

}
