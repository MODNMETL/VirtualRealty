package me.plytki.virtualrealty.enums;

import me.plytki.virtualrealty.VirtualRealty;
import org.bukkit.Material;

public enum PlotSize {

    SMALL(10, 10, 10, Material.matchMaterial(VirtualRealty.isLegacy ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.isLegacy ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.isLegacy ? (byte)5 : (byte)0),
    MEDIUM(25, 25, 25, Material.matchMaterial(VirtualRealty.isLegacy ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.isLegacy ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.isLegacy ? (byte)5 : (byte)0),
    LARGE(50, 50, 50, Material.matchMaterial(VirtualRealty.isLegacy ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.isLegacy ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.isLegacy ? (byte)5 : (byte)0),
    CUSTOM(0, 0, 0, Material.matchMaterial(VirtualRealty.isLegacy ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.isLegacy ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.isLegacy ? (byte)5 : (byte)0);

    private int length;
    private int width;
    private int height;
    private Material floorMaterial;
    private byte floorData;
    private Material borderMaterial;
    private byte borderData;

    PlotSize(int length, int width, int height, Material floorMaterial, byte floorData, Material borderMaterial, byte borderData) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.floorMaterial = floorMaterial;
        this.floorData = floorData;
        this.borderMaterial = borderMaterial;
        this.borderData = borderData;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Material getFloorMaterial() {
        return floorMaterial;
    }

    public void setFloorMaterial(Material floorMaterial) {
        this.floorMaterial = floorMaterial;
    }

    public byte getFloorData() {
        return floorData;
    }

    public void setFloorData(byte floorData) {
        this.floorData = floorData;
    }

    public Material getBorderMaterial() {
        return borderMaterial;
    }

    public void setBorderMaterial(Material borderMaterial) {
        this.borderMaterial = borderMaterial;
    }

    public byte getBorderData() {
        return borderData;
    }

    public void setBorderData(byte borderData) {
        this.borderData = borderData;
    }
}
