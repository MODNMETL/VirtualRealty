package com.modnmetl.virtualrealty.configs;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.PlotSize;

@Header("################################################################")
@Header("#                                                              #")
@Header("#                         Plot Sizes                           #")
@Header("#                                                              #")
@Header("################################################################")
public class SizesConfiguration extends OkaeriConfig {

    @Comment(" ")
    @Comment("-------------------------")
    @Comment("Don't change this value!")
    @CustomKey("config-version")
    public final String configVersion = VirtualRealty.getInstance().getDescription().getVersion();
    @Comment("-------------------------")

    @Comment(" ")
    @Comment("(<1.13) Legacy Materials: https://helpch.at/docs/1.8/org/bukkit/Material.html")
    @Comment("(>1.12) Post-Legacy Materials: https://helpch.at/docs/1.13/org/bukkit/Material.html")
    @Comment(" ")
    @Comment("floor-data and border-data are only for legacy versions * <1.13 *")
    @CustomKey("plot-sizes")
    public PlotSizes plotSizes = new PlotSizes();

    @Names(strategy = NameStrategy.IDENTITY)
    public static class PlotSizes extends OkaeriConfig {

        @Variable("VR_SIZE_SMALL")
        public Size SMALL = new Size(PlotSize.SMALL);
        @Variable("VR_SIZE_MEDIUM")
        public Size MEDIUM = new Size(PlotSize.MEDIUM);
        @Variable("VR_SIZE_LARGE")
        public Size LARGE = new Size(PlotSize.LARGE);

        public PlotSizes() {
        }

        public PlotSizes(Size small, Size medium, Size large) {
            this.SMALL = small;
            this.MEDIUM = medium;
            this.LARGE = large;
        }

        public static class Size extends OkaeriConfig {

            @Variable("VR_SIZE_FLOORMATERIAL")
            @CustomKey("floor-material")
            public String floorMaterial;
            @Variable("VR_SIZE_FLOORDATA")
            @CustomKey("floor-data")
            public byte floorData;
            @Variable("VR_SIZE_BORDERMATERIAL")
            @CustomKey("border-material")
            public String borderMaterial;
            @Variable("VR_SIZE_BORDERDATA")
            @CustomKey("border-data")
            public byte borderData;
            @Variable("VR_SIZE_LENGTH")
            public int length;
            @Variable("VR_SIZE_WIDTH")
            public int width;
            @Variable("VR_SIZE_HEIGHT")
            public int height;

            public Size() {
            }

            public Size(PlotSize plotSize) {
                this.floorMaterial = plotSize.getFloorMaterial().name();
                this.floorData = plotSize.getFloorData();
                this.borderMaterial = plotSize.getBorderMaterial().name();
                this.borderData = plotSize.getBorderData();
                this.length = plotSize.getLength();
                this.width = plotSize.getWidth();
                this.height = plotSize.getHeight();
            }

        }
    }

}
