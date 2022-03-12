package com.modnmetl.virtualrealty.managers;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;

import java.util.HashMap;
import java.util.Map;

public class MetricsManager {

    public static void registerMetrics(VirtualRealty instance, int serviceID) {
        Metrics metrics = new Metrics(instance, serviceID);
        metrics.addCustomChart(new SimplePie("used_database", () -> VirtualRealty.getPluginConfiguration().dataModel.name()));
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
        VirtualRealty.debug("Registered metrics");
    }

}
