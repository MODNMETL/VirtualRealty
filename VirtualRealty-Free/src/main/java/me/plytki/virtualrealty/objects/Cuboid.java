package me.plytki.virtualrealty.objects;

import me.plytki.virtualrealty.objects.math.BlockVector2;
import me.plytki.virtualrealty.objects.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cuboid {

    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int zMin;
    private int zMax;
    private double xMinCentered;
    private double xMaxCentered;
    private double yMinCentered;
    private double yMaxCentered;
    private double zMinCentered;
    private double zMaxCentered;
    private World world;

    public Cuboid(Location point1, Location point2) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = point1.getWorld();
        this.xMinCentered = this.xMin + 0.5;
        this.xMaxCentered = this.xMax + 0.5;
        this.yMinCentered = this.yMin + 0.5;
        this.yMaxCentered = this.yMax + 0.5;
        this.zMinCentered = this.zMin + 0.5;
        this.zMaxCentered = this.zMax + 0.5;
    }

    public Cuboid(BlockVector3 point1, BlockVector3 point2, World world) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = world;
        this.xMinCentered = this.xMin + 0.5;
        this.xMaxCentered = this.xMax + 0.5;
        this.yMinCentered = this.yMin + 0.5;
        this.yMaxCentered = this.yMax + 0.5;
        this.zMinCentered = this.zMin + 0.5;
        this.zMaxCentered = this.zMax + 0.5;
    }


    public List<Block> blockList() {
        //long time = System.currentTimeMillis();
        ArrayList<Block> bL = new ArrayList<>((xMax - xMin) * (yMax - yMin) * (zMax - zMin));
        for(int x = this.xMin; x <= this.xMax; ++x) {
            for(int y = this.yMin; y <= this.yMax; ++y) {
                for(int z = this.zMin; z <= this.zMax; ++z) {
                    Block b = this.world.getBlockAt(x, y, z);
                    bL.add(b);
                }
            }
        }
        //System.out.println("blockList() time " + (System.currentTimeMillis() - time));
        return bL;
    }

    public Location getCenter() {
        return new Location(this.world, (this.xMax - this.xMin) / 2 + this.xMin, (this.yMax - this.yMin) / 2 + this.yMin, (this.zMax - this.zMin) / 2 + this.zMin);
    }

    public BlockVector3 getCenterVector() {
        return BlockVector3.at((this.xMax - this.xMin) / 2 + this.xMin, (this.yMax - this.yMin) / 2 + this.yMin, (this.zMax - this.zMin) / 2 + this.zMin);
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

    public Location getRandomLocation() {
        Random rand = new Random();
        int x = rand.nextInt(Math.abs(this.xMax - this.xMin) + 1) + this.xMin;
        int y = rand.nextInt(Math.abs(this.yMax - this.yMin) + 1) + this.yMin;
        int z = rand.nextInt(Math.abs(this.zMax - this.zMin) + 1) + this.zMin;
        return new Location(this.world, x, y, z);
    }

    public boolean contains(BlockVector3 vector3) {
        return vector3.getBlockX() >= this.xMin && vector3.getBlockX() <= this.xMax && vector3.getBlockY() >= this.yMin && vector3.getBlockY() <= this.yMax && vector3
                .getBlockZ() >= this.zMin && vector3.getBlockZ() <= this.zMax;
    }

    public boolean contains(BlockVector3 vector3, World world) {
        return world == this.world && vector3.getBlockX() >= this.xMin && vector3.getBlockX() <= this.xMax && vector3.getBlockY() >= this.yMin && vector3.getBlockY() <= this.yMax && vector3
                .getBlockZ() >= this.zMin && vector3.getBlockZ() <= this.zMax;
    }

    public int getTotalBlockSize() {
        return this.getHeight() * this.getXWidth() * this.getZWidth();
    }

    public int getXWidth() {
        return this.xMax - this.xMin + 1;
    }

    public int getZWidth() {
        return this.zMax - this.zMin + 1;
    }

    public boolean isIn(Location loc) {
        return loc.getWorld() == this.world && loc.getBlockX() >= this.xMin && loc.getBlockX() <= this.xMax && loc.getBlockY() >= this.yMin && loc.getBlockY() <= this.yMax && loc
                .getBlockZ() >= this.zMin && loc.getBlockZ() <= this.zMax;
    }

    public boolean isIn(Player player) {
        return this.isIn(player.getLocation());
    }

    public boolean isInWithMarge(Location loc, double marge) {
        return loc.getWorld() == this.world && loc.getX() >= this.xMinCentered - marge && loc.getX() <= this.xMaxCentered + marge && loc.getY() >= this.yMinCentered - marge && loc
                .getY() <= this.yMaxCentered + marge && loc.getZ() >= this.zMinCentered - marge && loc.getZ() <= this.zMaxCentered + marge;
    }

    public boolean isColliding(Cuboid region) {
        List<BlockVector2> flatRegion1 = new ArrayList<>(this.getFlatRegion());
        List<BlockVector2> flatRegion2 = new ArrayList<>(region.getFlatRegion());
        for (BlockVector2 vector1 : flatRegion1) {
            for (BlockVector2 vector2 : flatRegion2)
                if (vector1.getBlockX() == vector2.getBlockX() && vector1.getBlockZ() == vector2.getBlockZ()) return true;
        }
        return false;
    }

    public List<BlockVector2> getFlatRegion() {
        List<BlockVector2> blocksVector = new ArrayList<>();
        for (Block block : blockList()) {
            blocksVector.add(BlockVector2.at(block.getLocation().getBlockX(), block.getLocation().getBlockZ()));
        }
        return blocksVector;
    }

    public List<BlockVector2> getWalls() {
        List<BlockVector2> walls = new ArrayList<>();
        for (int z = zMin; z <= zMax; z++) {
            walls.add(BlockVector2.at(xMin, z));
            walls.add(BlockVector2.at(xMax, z));
        }
        for (int x = xMin; x < xMax; x++) {
            walls.add(BlockVector2.at(x, zMin));
            walls.add(BlockVector2.at(x, zMax));
        }
        return walls;
    }

}
