package me.plytki.virtualrealty.loaders;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.PlotSize;
import org.bukkit.Material;
import org.diorite.cfg.annotations.CfgClass;
import org.diorite.cfg.annotations.CfgComment;
import org.diorite.cfg.annotations.CfgName;
import org.diorite.cfg.annotations.CfgStringStyle;
import org.diorite.cfg.annotations.CfgStringStyle.StringStyle;
import org.diorite.cfg.annotations.defaults.CfgDelegateDefault;

@CfgClass(name = "SizesConfiguration")
@CfgDelegateDefault("{new}")
@CfgComment("~-~-~-~-~-~-~-~-~-~-~-~~-~-~-~~ #")
@CfgComment("                                #")
@CfgComment("           Plot Sizes           #")
@CfgComment("                                #")
@CfgComment("~-~-~-~-~-~-~-~-~-~-~-~~-~-~-~~ #")
public class SizesConfiguration {

    @CfgComment(" ")
    @CfgComment("-------------------------")
    @CfgComment("Don't change this value!")
    @CfgName("config-version")
    public final String configVersion = VirtualRealty.getInstance().getDescription().getVersion();
    @CfgComment("-------------------------")

    @CfgComment(" ")
    @CfgComment("(<1.13) Legacy Materials: https://helpch.at/docs/1.8/org/bukkit/Material.html")
    @CfgComment("(>1.12) Post-Legacy Materials: https://helpch.at/docs/1.13/org/bukkit/Material.html")
    @CfgComment(" ")
    @CfgComment("floor-data and border-data are only for legacy versions * <1.13 *")
    @CfgName("plot-sizes")
    public PlotSizes plotSizes = new PlotSizes(new Small(PlotSize.SMALL), new Medium(PlotSize.MEDIUM), new Large(PlotSize.LARGE));

    public static class PlotSizes {

        public Small SMALL;

        public Medium MEDIUM;

        public Large LARGE;

        public PlotSizes(Small small, Medium medium, Large large) {
            this.SMALL = small;
            this.MEDIUM = medium;
            this.LARGE = large;
        }

        private PlotSizes() {}

    }

    public static class Small extends Size {

        public Small(PlotSize plotSize) {
            super(plotSize.getFloorMaterial(), plotSize.getFloorData(), plotSize.getBorderMaterial(), plotSize.getBorderData(), plotSize.getLength(), plotSize.getWidth(), plotSize.getHeight());
        }

        private Small() {}

    }

    public static class Medium extends Size {

        public Medium(PlotSize plotSize) {
            super(plotSize.getFloorMaterial(), plotSize.getFloorData(), plotSize.getBorderMaterial(), plotSize.getBorderData(), plotSize.getLength(), plotSize.getWidth(), plotSize.getHeight());

        }

        private Medium() {}

    }

    public static class Large extends Size {

        public Large(PlotSize plotSize) {
            super(plotSize.getFloorMaterial(), plotSize.getFloorData(), plotSize.getBorderMaterial(), plotSize.getBorderData(), plotSize.getLength(), plotSize.getWidth(), plotSize.getHeight());
        }

        private Large() {}

    }

    public abstract static class Size {

        @CfgName("floor-material")
        public String floorMaterial;
        @CfgName("floor-data")
        public byte floorData;
        @CfgName("border-material")
        public String borderMaterial;
        @CfgName("border-data")
        public byte borderData;
        public int length;
        public int width;
        public int height;

        public Size(Material floorMaterial, byte floorData, Material borderMaterial, byte borderData, int length, int width, int height) {
            this.floorMaterial = floorMaterial.name();
            this.floorData = floorData;
            this.borderMaterial = borderMaterial.name();
            this.borderData = borderData;
            this.length = length;
            this.width = width;
            this.height = height;
        }

        private Size() {}

    }

}
