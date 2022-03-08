package com.modnmetl.virtualrealty.enums;

import com.modnmetl.virtualrealty.VirtualRealty;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum PlotSize {

    SMALL(10, 10, 10, Material.matchMaterial(VirtualRealty.legacyVersion ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.legacyVersion ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.legacyVersion ? (byte)5 : (byte)0),
    MEDIUM(25, 25, 25, Material.matchMaterial(VirtualRealty.legacyVersion ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.legacyVersion ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.legacyVersion ? (byte)5 : (byte)0),
    LARGE(50, 50, 50, Material.matchMaterial(VirtualRealty.legacyVersion ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.legacyVersion ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.legacyVersion ? (byte)5 : (byte)0),
    CUSTOM(0, 0, 0, Material.matchMaterial(VirtualRealty.legacyVersion ? "GRASS" : "GRASS_BLOCK"), (byte)0, Material.matchMaterial(VirtualRealty.legacyVersion ? "STEP" : "STONE_BRICK_SLAB"), VirtualRealty.legacyVersion ? (byte)5 : (byte)0),
    AREA(0, 0, 0, Material.AIR, (byte)0, Material.AIR, (byte)0);

    private int length;
    private int height;
    private int width;

    private Material floorMaterial;
    private byte floorData;
    private Material borderMaterial;
    private byte borderData;

    PlotSize(int length, int height, int width, Material floorMaterial, byte floorData, Material borderMaterial, byte borderData) {
        this.length = length;
        this.height = height;
        this.width = width;
        this.floorMaterial = floorMaterial;
        this.floorData = floorData;
        this.borderMaterial = borderMaterial;
        this.borderData = borderData;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public void setFloorMaterial(Material floorMaterial) {
        this.floorMaterial = floorMaterial;
    }

    public void setFloorData(byte floorData) {
        this.floorData = floorData;
    }

    public void setBorderMaterial(Material borderMaterial) {
        this.borderMaterial = borderMaterial;
    }

    public void setBorderData(byte borderData) {
        this.borderData = borderData;
    }

}
