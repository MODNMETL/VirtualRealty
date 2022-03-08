package com.modnmetl.virtualrealty.utils.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Data
public class VirtualBlock implements Serializable {

    private int x;
    private int y;
    private int z;

    private int material;
    private byte data;

    private String blockData;

    public VirtualBlock(int x, int y, int z, int material, byte data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
        this.data = data;
    }

    public VirtualBlock(int x, int y, int z, String blockData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockData = blockData;
    }

    public Block getBlock(World world) {
        return world.getBlockAt(x,y,z);
    }


}
