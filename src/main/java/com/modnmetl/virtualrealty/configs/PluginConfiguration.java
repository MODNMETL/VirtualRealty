package com.modnmetl.virtualrealty.configs;

import com.modnmetl.virtualrealty.enums.dynmap.HighlightType;
import com.modnmetl.virtualrealty.enums.ServerVersion;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import com.modnmetl.virtualrealty.VirtualRealty;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;

@Header("-------------------------------------------------------------- #")
@Header("                                                               #")
@Header("                        Virtual Realty                         #")
@Header("                                                               #")
@Header("-------------------------------------------------------------- #")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PluginConfiguration extends OkaeriConfig {

    @Comment("Changing this value will break your plugin!")
    @CustomKey("initial-version")
    public String initServerVersion = VirtualRealty.legacyVersion ? ServerVersion.LEGACY.toString() : ServerVersion.MODERN.toString();

    @Comment("Debug mode")
    public boolean debugMode = false;

    @Comment("Here you put your license details")
    public License license = new License();

    @Names(strategy = NameStrategy.IDENTITY)
    public static class License extends OkaeriConfig {

        public String key = "";
        public String email = "";

    }

    @Comment("Set player gamemode to change when they enter their plot")
    public boolean enablePlotGamemode = false;

    @Comment("Set your wanted language (locale)")
    public String locale = "en_GB";

    @Comment("Set which gamemode players change to when they enter their plot")
    @CustomKey("default-plot-gamemode")
    public String plotGamemode = "SURVIVAL";

    @Comment("Lock gamemode to plot default when player enters their plot (disables '/plot gm' command)")
    public boolean lockPlotGamemode = false;

    public GameMode getDefaultPlotGamemode() {
        try {
            return GameMode.valueOf(plotGamemode);
        } catch (Exception e) {
            VirtualRealty.getInstance().getLogger().warning("Couldn't parse plot-gamemode from config.yml\nUsing default: SURVIVAL");
            return GameMode.SURVIVAL;
        }
    }

    @Comment("Enables dynmap plots highlighting")
    @CustomKey("enable-dynmap-markers")
    public boolean dynmapMarkers = false;

    @Comment("The minimum number of blocks required between plots")
    @CustomKey("enforce-plot-separation")
    public int plotSpacing = 1;

    @Comment("Choose which type of plots should be highlighted on Dynmap page | Choose from: { ALL, AVAILABLE, OWNED }")
    public HighlightType dynmapType = HighlightType.ALL;

    @CustomKey("dynmap-markers")
    public MarkerColor dynmapMarkersColor = new MarkerColor();

    @Names(strategy = NameStrategy.IDENTITY)
    public static class MarkerColor extends OkaeriConfig {

        public Available available = new Available();
        public Owned owned = new MarkerColor.Owned();

        @Names(strategy = NameStrategy.IDENTITY)
        public static class Available extends OkaeriConfig {

            public String color = "#80eb34";
            public double opacity = .3;

            public int getHexColor() {
                return Integer.decode("0x" + color.replaceAll("#", ""));
            }

        }

        @Names(strategy = NameStrategy.IDENTITY)
        public static class Owned extends OkaeriConfig {

            public String color = "#ffbf00";
            public double opacity = .45;

            public int getHexColor() {
                return Integer.decode("0x" + color.replaceAll("#", ""));
            }

        }

    }

    @Comment("Enables plots enter/leave sounds")
    @CustomKey("plot-sounds")
    public boolean plotSound = true;

    @Comment("Type of data recording")
    @Comment("SQLITE - Local database")
    @Comment("MYSQL - External database")
    @CustomKey("data-model")
    public DataModel dataModel = DataModel.SQLITE;

    @Comment("Data required to connect to the database")
    @Comment("The plotsTableName section is the name of the VR data table in the database")
    @Comment("It is best to change these names only if you really need to (e.g. there is a conflict with another plugin)")
    @Comment("To rename tables when you already have some VR data in the database:")
    @Comment("1. Turn off the server")
    @Comment("2. Change data in VR config")
    @Comment("3. Rename database tables using phpMyAdmin for example")
    @CustomKey("mysql")
    public MySQL mysql = new MySQL();

    public enum DataModel {
        SQLITE,
        MYSQL
    }

    @Names(strategy = NameStrategy.IDENTITY)
    @NoArgsConstructor
    public static class MySQL extends OkaeriConfig {

        public String hostname = "localhost";
        public int port = 3306;
        public String database = "db";
        public String user = "root";
        public String password = "passwd";
        public String plotsTableName = "vr_plots";
        public String plotMembersTableName = "vr_plot_members";

    }

}
