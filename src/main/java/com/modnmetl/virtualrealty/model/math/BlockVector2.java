package com.modnmetl.virtualrealty.model.math;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Vector;

public class BlockVector2 extends Vector {

    private int x;
    private int z;

    public BlockVector2(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public static BlockVector2 at(int x, int z) {
        return new BlockVector2(x, z);
    }

    public int getBlockX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getBlockZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public boolean containedWithin(BlockVector2 min, BlockVector2 max) {
        return this.x >= min.x && this.x <= max.x && this.z >= min.z && this.z <= max.z;
    }

    public Location toLocation(World world, int y) {
        return new Location(world, x, y, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

}
