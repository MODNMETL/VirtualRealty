package com.modnmetl.virtualrealty.model.region;

import java.util.HashMap;
import java.util.Map;

public class WorldPlotData {

    private final Map<Long, ChunkPlotData> chunkPlotDataMap;

    public WorldPlotData() {
        this.chunkPlotDataMap = new HashMap<>();
    }

    public ChunkPlotData getChunkPlotData(long chunkKey) {
        chunkPlotDataMap.putIfAbsent(chunkKey, new ChunkPlotData());
        return chunkPlotDataMap.get(chunkKey);
    }

}
