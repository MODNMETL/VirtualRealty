package com.modnmetl.virtualrealty.model.region;

import com.modnmetl.virtualrealty.model.plot.Plot;

import java.util.*;

public class ChunkPlotData {

    private final Map<Integer, Plot> plots = new HashMap<>();

    public ChunkPlotData(Plot... plots) {
        for (Plot plot : plots) {
            this.plots.put(plot.getID(), plot);
        }
    }

    public void addPlot(Plot plot) {
        this.plots.put(plot.getID(), plot);
    }

    public Plot getPlot(int id) {
        return plots.get(id);
    }

    public Collection<Plot> getPlots() {
        return plots.values();
    }

}
