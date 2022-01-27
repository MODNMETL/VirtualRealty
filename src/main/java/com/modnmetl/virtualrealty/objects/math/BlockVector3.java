package com.modnmetl.virtualrealty.objects.math;

import org.bukkit.Location;

public class BlockVector3 extends BlockVector2 {

    private int x;
    private int y;
    private int z;

    private BlockVector3(int x, int y, int z) {
        super(x, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockVector3 at(int x, int y, int z) {
        return new BlockVector3(x, y, z);
    }

    public static BlockVector3 locationToVector(Location location) {
        return new BlockVector3(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public int getBlockX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getBlockY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getBlockZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public String toSimpleString() {
        return x + ", " + y + ", " + z;
    }

}
