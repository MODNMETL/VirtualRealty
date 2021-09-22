package me.plytki.virtualrealty.utils;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.utils.data.Data;
import me.plytki.virtualrealty.utils.multiversion.VMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class SchematicUtil {
    
    public static String[] getStructure(Block block, Block block2) {
        long time = System.currentTimeMillis();
        int minX = Math.min(block.getX(), block2.getX());
        int minZ = Math.min(block.getZ(), block2.getZ());
        int minY = Math.min(block.getY(), block2.getY());
        int maxX = Math.max(block.getX(), block2.getX());
        int maxZ = Math.max(block.getZ(), block2.getZ());
        int maxY = Math.max(block.getY(), block2.getY());
        List<String> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block b = block.getWorld().getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR) {
                        if (VirtualRealty.isLegacy) {
                            blocks.add(x - minX + ";" + (y - minY) + ";" + (z - minZ) + ";" + b.getType().getId() + ";" + b.getData());
                        } else {
                            blocks.add(x - minX + ";" + (y - minY) + ";" + (z - minZ) + ";" + b.getBlockData().getAsString().substring(10));
                        }
                    }
                }
            }
        }
        VirtualRealty.debug("Getted and serialized blocks in: " + (System.currentTimeMillis() - time) + " ms");
        return blocks.toArray(new String[0]);
    }

    public static void paste(int plotID, Location l) {
        long time = System.currentTimeMillis();
        String[] blocks = load(plotID);
        if (blocks == null) return;
        Plot plot = PlotManager.getPlot(plotID);
        Location location = new Location(plot.getCreatedLocation().getWorld(), plot.getBorderBottomLeftCorner().getBlockX(), plot.getBorderBottomLeftCorner().getBlockY(), plot.getBorderBottomLeftCorner().getBlockZ());
        Location location2 = new Location(plot.getCreatedLocation().getWorld(), plot.getBorderTopRightCorner().getBlockX(), plot.getBorderTopRightCorner().getBlockY(), plot.getBorderTopRightCorner().getBlockZ());
        Block pos1Block = location.getBlock();
        Block pos2Block = location2.getBlock();
        int minX = Math.min(pos1Block.getX(), pos2Block.getX());
        int minZ = Math.min(pos1Block.getZ(), pos2Block.getZ());
        int minY = Math.min(pos1Block.getY(), pos2Block.getY());
        int maxX = Math.max(pos1Block.getX(), pos2Block.getX());
        int maxZ = Math.max(pos1Block.getZ(), pos2Block.getZ());
        int maxY = Math.max(pos1Block.getY(), pos2Block.getY());
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block b = location.getWorld().getBlockAt(x, y, z);
                    b.setType(Material.AIR);
                }
            }
        }
        if (blocks[0].isEmpty()) return;
        for (String block : blocks) {
            String[] cords = block.split(";");
            int x = Integer.parseInt(cords[0]);
            int y = Integer.parseInt(cords[1]);
            int z = Integer.parseInt(cords[2]);
            Location displaced = l.clone();
            displaced.add(x, y, z);
            Block b = displaced.getBlock();
            if (VirtualRealty.isLegacy) {
                try {
                    Method m = Block.class.getDeclaredMethod("setType", Material.class);
                    m.setAccessible(true);
                    m.invoke(b, VMaterial.getMaterial(Integer.parseInt(cords[3])));
                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                    m2.setAccessible(true);
                    m2.invoke(b, (byte) Integer.parseInt(cords[4]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                BlockData blockData = Bukkit.createBlockData("minecraft:" + cords[3]);
                b.setBlockData(blockData);
            }
            b.getState().update(true);
        }
        VirtualRealty.debug("Pasted in: " + (System.currentTimeMillis() - time) + " ms");
    }

    public static void save(int plotID, String[] blocks) {
        long time = System.currentTimeMillis();
        File f = new File(VirtualRealty.plotsSchemaFolder, "plot" + plotID + ".region");

        StringBuilder stringBuilder = new StringBuilder();
        for (String s : blocks) {
            stringBuilder.append(s).append("|");
        }
        String plotString = stringBuilder.toString();
        if (plotString.isEmpty())
            plotString = "clear|";
        try {
            new Data().compressData(plotString.substring(0, plotString.length() - 1).getBytes(StandardCharsets.UTF_8), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        VirtualRealty.debug("Saved in: " + (System.currentTimeMillis() - time) + " ms");
    }

    public static String[] load(int plotID) {
        long time = System.currentTimeMillis();
        File f = new File(VirtualRealty.plotsSchemaFolder, "plot" + plotID + ".region");
        if (f.exists()) {
            String loaded = null;
            try {
                loaded = new String(new Data().decompressData(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (loaded.equalsIgnoreCase("clear")) return new String[]{""};
            VirtualRealty.debug("Loaded in: " + (System.currentTimeMillis() - time) + " ms");
            return loaded.split("\\|");
        }
        return null;
    }

    public static List<String> oldLoad(int plotID) {
        File f = new File(VirtualRealty.plotsSchemaFolder, "plot" + plotID + ".schem");
        List<String> loaded = new ArrayList<>();
        try {
            FileInputStream streamIn = new FileInputStream(f);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            loaded = (List<String>)objectinputstream.readObject();
            objectinputstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loaded;
    }

}
