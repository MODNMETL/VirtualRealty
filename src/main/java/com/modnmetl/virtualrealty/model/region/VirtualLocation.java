package com.modnmetl.virtualrealty.model.region;

import lombok.Data;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.Serializable;

@Data
public class VirtualLocation implements Serializable {

    private int x;
    private int y;
    private int z;

    public VirtualLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block getBlock(World world) {
        return world.getBlockAt(x,y,z);
    }


}
