package com.modnmetl.virtualrealty.model.region;

import com.modnmetl.virtualrealty.model.math.BlockVector2;
import com.modnmetl.virtualrealty.model.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public class Cuboid {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final World world;
    private BlockVector3 centerVector; // Lazy initialization
    private List<BlockVector2> walls; // Lazy initialization

    public Cuboid(Location point1, Location point2) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = point1.getWorld();
    }

    public Cuboid(BlockVector3 point1, BlockVector3 point2, World world) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = world;
    }

    public List<BlockVector2> getWalls() {
        if (walls == null) {
            walls = new ArrayList<>();
            for (int z = zMin; z <= zMax; z++) {
                walls.add(BlockVector2.at(xMin, z));
                walls.add(BlockVector2.at(xMax, z));
            }
            for (int x = xMin; x < xMax; x++) {
                walls.add(BlockVector2.at(x, zMin));
                walls.add(BlockVector2.at(x, zMax));
            }
        }
        return walls;
    }

    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (int x = this.xMin; x <= this.xMax; ++x) {
            for (int y = this.yMin; y <= this.yMax; ++y) {
                for (int z = this.zMin; z <= this.zMax; ++z) {
                    blocks.add(this.world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public List<Block> getFlatBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (int x = this.xMin; x <= this.xMax; ++x) {
            for (int z = this.zMin; z <= this.zMax; ++z) {
                blocks.add(this.world.getBlockAt(x, 0, z));
            }
        }
        return blocks;
    }

    public List<BlockVector2> getFlatRegion() {
        List<BlockVector2> blocksVector = new ArrayList<>();
        for (Block block : getFlatBlocks()) {
            blocksVector.add(BlockVector2.at(block.getLocation().getBlockX(), block.getLocation().getBlockZ()));
        }
        return blocksVector;
    }


    public Location getCenter() {
        return new Location(this.world, (this.xMax - this.xMin) / 2 + this.xMin + 1, (this.yMax - this.yMin) / 2 + this.yMin, (this.zMax - this.zMin) / 2 + this.zMin + 1);
    }

    // Suggestion 3 - Lazy calculation and cached results
    public BlockVector3 getCenterVector() {
        if (centerVector == null) {
            centerVector = BlockVector3.at(
                    (xMax - xMin) / 2 + xMin,
                    (yMax - yMin) / 2 + yMin,
                    (zMax - zMin) / 2 + zMin
            );
        }
        return centerVector;
    }

    public double getDistance() {
        return this.getPoint1().distance(this.getPoint2());
    }

    public double getDistanceSquared() {
        return this.getPoint1().distanceSquared(this.getPoint2());
    }

    public int getHeight() {
        return this.yMax - this.yMin + 1;
    }

    public BlockVector3 getMinimumPoint() {
        return BlockVector3.at(this.xMin, this.yMin, this.zMin);
    }

    public BlockVector3 getMaximumPoint() {
        return BlockVector3.at(this.xMax, this.yMax, this.zMax);
    }

    public Location getPoint1() {
        return new Location(this.world, this.xMin, this.yMin, this.zMin);
    }

    public Location getPoint2() {
        return new Location(this.world, this.xMax, this.yMax, this.zMax);
    }

    public boolean isIn(BlockVector3 vector3, String world) {
        return world.equals(this.world.getName())
                && vector3.getBlockX() >= this.xMin && vector3.getBlockX() <= this.xMax
                && vector3.getBlockY() >= this.yMin && vector3.getBlockY() <= this.yMax
                && vector3.getBlockZ() >= this.zMin && vector3.getBlockZ() <= this.zMax;
    }

    public int getCuboidSize() {
        return this.getHeight() * this.getXWidth() * this.getZWidth();
    }

    public int getXWidth() {
        return this.xMax - this.xMin + 1;
    }

    public int getZWidth() {
        return this.zMax - this.zMin + 1;
    }

    public boolean isIn(Location loc) {
        return loc.getWorld() == this.world
                && loc.getBlockX() >= this.xMin && loc.getBlockX() <= this.xMax
                && loc.getBlockY() >= this.yMin && loc.getBlockY() <= this.yMax
                && loc.getBlockZ() >= this.zMin && loc.getBlockZ() <= this.zMax;
    }

    public boolean isIn(BlockVector2 vector2, World world, int spacing) {
        return world == this.world && vector2.getBlockX() >= this.xMin - spacing && vector2.getBlockX() <= this.xMax + spacing &&
                vector2.getBlockZ() >= this.zMin - spacing && vector2.getBlockZ() <= this.zMax + spacing;
    }

    public boolean isIn(BlockVector2 vector2, World world) {
        return this.isIn(vector2, world, 0);
    }

    public boolean isColliding(Cuboid other) {
        return this.world == other.world &&
                this.xMax >= other.xMin && this.xMin <= other.xMax &&
                this.yMax >= other.yMin && this.yMin <= other.yMax &&
                this.zMax >= other.zMin && this.zMin <= other.zMax;
    }

    public boolean isColliding2D(Cuboid other) {
        return this.world.equals(other.world) &&
                this.xMax >= other.xMin && this.xMin <= other.xMax &&
                this.zMax >= other.zMin && this.zMin <= other.zMax;
    }

    public boolean isWithin2D(Cuboid other) {
        return this.world.equals(other.world) &&
                this.xMin >= other.xMin && this.xMax <= other.xMax &&
                this.zMin >= other.zMin && this.zMax <= other.zMax;
    }

    public boolean containsBlock(Block block) {
        Location blockLocation = block.getLocation();
        if (block.getWorld() != world) return false;
        for (BlockVector2 vector2 : getFlatRegion()) {
            if (vector2.getBlockX() == blockLocation.getBlockX() && vector2.getBlockZ() == blockLocation.getBlockZ()) return true;
        }
        return false;
    }

    public World getWorld() {
        return world;
    }

}
