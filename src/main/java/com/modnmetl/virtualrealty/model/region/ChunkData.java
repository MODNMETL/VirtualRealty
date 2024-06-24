package com.modnmetl.virtualrealty.model.region;

public class ChunkData {

    private final int x;
    private final int z;

    public ChunkData(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

}
