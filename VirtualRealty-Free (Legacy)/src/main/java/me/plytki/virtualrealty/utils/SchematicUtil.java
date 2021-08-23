package me.plytki.virtualrealty.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class SchematicUtil {

    Plugin plugin;

    public SchematicUtil(Plugin pl){
        this.plugin = pl;
    }

    public List<String> getStructure(Block block, Block block2) {
        int minX = (block.getX() < block2.getX()) ? block.getX() : block2.getX();
        int minZ = (block.getZ() < block2.getZ()) ? block.getZ() : block2.getZ();
        int minY = (block.getY() < block2.getY()) ? block.getY() : block2.getY();
        int maxX = (block.getX() > block2.getX()) ? block.getX() : block2.getX();
        int maxZ = (block.getZ() > block2.getZ()) ? block.getZ() : block2.getZ();
        int maxY = (block.getY() > block2.getY()) ? block.getY() : block2.getY();
        List<String> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block b = block.getWorld().getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR)
                        blocks.add(x - minX + ";" + (y - minY) + ";" + (z - minZ) + ";" + b.getType().getId() + ";" + b.getData());
                    //System.out.println(b.getBlockData().getAsString());
                }
            }
        }
        return blocks;
    }

    public void paste(int plotID, Location l) {
        List<String> blocks = this.load(plotID);
        Plot plot = PlotManager.getPlot(plotID);
        Location location = new Location(plot.getCreatedLocation().getWorld(), plot.getBorderBottomLeftCorner().getBlockX(), plot.getBorderBottomLeftCorner().getBlockY(), plot.getBorderBottomLeftCorner().getBlockZ());
        Location location2 = new Location(plot.getCreatedLocation().getWorld(), plot.getBorderTopRightCorner().getBlockX(), plot.getBorderTopRightCorner().getBlockY(), plot.getBorderTopRightCorner().getBlockZ());
        Block block = location.getBlock();
        Block block2 = location2.getBlock();
        int minX = (block.getX() < block2.getX()) ? block.getX() : block2.getX();
        int minZ = (block.getZ() < block2.getZ()) ? block.getZ() : block2.getZ();
        int minY = (block.getY() < block2.getY()) ? block.getY() : block2.getY();
        int maxX = (block.getX() > block2.getX()) ? block.getX() : block2.getX();
        int maxZ = (block.getZ() > block2.getZ()) ? block.getZ() : block2.getZ();
        int maxY = (block.getY() > block2.getY()) ? block.getY() : block2.getY();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block b = block.getWorld().getBlockAt(x, y, z);
                    b.setType(Material.AIR);
                }
            }
        }
        for (String block1 : blocks) {
            String[] cords = block1.split(";");
            int x = Integer.parseInt(cords[0]);
            int y = Integer.parseInt(cords[1]);
            int z = Integer.parseInt(cords[2]);
            Location displaced = l.clone();
            displaced.add(x, y, z);
            Block b = displaced.getBlock();
            b.setType(Material.getMaterial(Integer.parseInt(cords[3])));
            b.setData((byte) Integer.parseInt(cords[4]));
            b.getState().update(true);
        }
    }

    public void save(int plotID, List<String> b) {
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        File f = new File(VirtualRealty.plotsSchemaFolder, "plot" + plotID + ".schem");
        try {
            f.createNewFile();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            fout = new FileOutputStream(f);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(b);
        }
        catch (Exception e2) {
            e2.printStackTrace();
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        }
        finally {
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        }
    }

    public List<String> load(int plotID) {
        File f = new File(VirtualRealty.plotsSchemaFolder, "plot" + plotID + ".schem");
        List<String> loaded = new ArrayList<String>();
        try {
            FileInputStream streamIn = new FileInputStream(f);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            loaded = (List<String>)objectinputstream.readObject();
            objectinputstream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return loaded;
    }

}
