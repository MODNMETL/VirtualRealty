package com.modnmetl.virtualrealty.configs;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import com.modnmetl.virtualrealty.model.plot.PlotSize;
import lombok.NoArgsConstructor;

@Header("--------------------------------------------------------------#")
@Header("                                                              #")
@Header("                         Plot Sizes                           #")
@Header("                                                              #")
@Header("--------------------------------------------------------------#")
public class SizesConfiguration extends OkaeriConfig {

    @Comment(" ")
    @Comment("(<1.13) Legacy Materials: https://helpch.at/docs/1.8/org/bukkit/Material.html")
    @Comment("(>1.12) Post-Legacy Materials: https://helpch.at/docs/1.13/org/bukkit/Material.html")
    @Comment(" ")
    @Comment("floor-data and border-data are only for legacy versions * <1.13 *")
    @CustomKey("plot-sizes")
    public PlotSizes plotSizes = new PlotSizes();

    @Names(strategy = NameStrategy.IDENTITY)
    @NoArgsConstructor
    public static class PlotSizes extends OkaeriConfig {

        public Size SMALL = new Size(PlotSize.SMALL);
        public Size MEDIUM = new Size(PlotSize.MEDIUM);
        public Size LARGE = new Size(PlotSize.LARGE);
        public CustomSize AREA = new CustomSize();


        @NoArgsConstructor
        public static class Size extends OkaeriConfig {

            @CustomKey("floor-material")
            public String floorMaterial;
            @CustomKey("floor-data")
            public byte floorData;
            @CustomKey("border-material")
            public String borderMaterial;
            @CustomKey("border-data")
            public byte borderData;
            public int length;
            public int height;
            public int width;

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

        @NoArgsConstructor
        public static class CustomSize extends OkaeriConfig {

            public int length = PlotSize.AREA.getLength();
            public int height = PlotSize.AREA.getHeight();
            public int width = PlotSize.AREA.getWidth();

        }

    }

}
