package me.plytki.virtualrealty.enums;

import org.bukkit.Material;

public enum PlotSize {

    SMALL(10, 10, 10, Material.matchMaterial("GRASS_BLOCK"), (byte)0),
    MEDIUM(25, 25, 25, Material.matchMaterial("GRASS_BLOCK"), (byte)0),
    LARGE(50, 50, 50, Material.matchMaterial("GRASS_BLOCK"), (byte)0),
    CUSTOM(0, 0, 0, Material.matchMaterial("GRASS_BLOCK"), (byte)0);

    private int length;
    private int width;
    private int height;
    private Material floorMaterial;
    private byte floorMaterialData;

    PlotSize(int length, int width, int height, Material floorMaterial, byte floorMaterialData) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.floorMaterial = floorMaterial;
        this.floorMaterialData = floorMaterialData;
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

    public byte getFloorMaterialData() {
        return floorMaterialData;
    }

    public void setFloorMaterialData(byte floorMaterialData) {
        this.floorMaterialData = floorMaterialData;
    }
}