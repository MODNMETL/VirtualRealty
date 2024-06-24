package com.modnmetl.virtualrealty.utils.data;

import com.modnmetl.virtualrealty.model.region.IVirtualBlock;
import lombok.Data;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.*;

// DO NOT TOUCH
@Data
@Deprecated
public class VirtualBlock implements Serializable, IVirtualBlock {

    private static final long serialVersionUID = -4262694990318416275L;

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
