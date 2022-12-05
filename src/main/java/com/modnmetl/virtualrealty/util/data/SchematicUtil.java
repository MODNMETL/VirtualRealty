package com.modnmetl.virtualrealty.util.data;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.plot.Plot;
import com.modnmetl.virtualrealty.model.region.VirtualBlock;
import com.modnmetl.virtualrealty.model.region.VirtualLocation;
import com.modnmetl.virtualrealty.util.multiversion.VMaterial;
import lombok.SneakyThrows;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class SchematicUtil {

    public static final String LEGACY_REGION_PREFIX = "legacy_plot-";
    public static final String REGION_PREFIX = "plot-";
    public static final String REGION_SUFFIX = ".region";

    public static LinkedList<VirtualBlock> getStructure(Block block, Block block2) {
        long time = System.currentTimeMillis();
        int minX = Math.min(block.getX(), block2.getX());
        int minZ = Math.min(block.getZ(), block2.getZ());
        int minY = Math.min(block.getY(), block2.getY());
        int maxX = Math.max(block.getX(), block2.getX());
        int maxZ = Math.max(block.getZ(), block2.getZ());
        int maxY = Math.max(block.getY(), block2.getY());
        LinkedList<VirtualBlock> blocks = new LinkedList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (block.getWorld().getHighestBlockAt(x, z).getLocation().getY() < minY) continue;
                for (int y = minY; y <= maxY; ++y) {
                    Block oldBlock = block.getWorld().getBlockAt(x, y, z);
                    if (oldBlock.getType() == Material.AIR) continue;
                    if (VirtualRealty.legacyVersion) {
                        blocks.add(new VirtualBlock(x, y, z, oldBlock.getType().getId(), oldBlock.getData()));
                    } else {
                        blocks.add(new VirtualBlock(x, y, z, oldBlock.getBlockData().getAsString().substring(10)));
                    }
                }
            }
        }
        VirtualRealty.debug("Got " + blocks.size() + " blocks in: " + (System.currentTimeMillis() - time) + " ms");
        return blocks;
    }

    public static void paste(int plotID) {
        List<VirtualBlock> blocks = load(plotID);
        if (blocks == null) return;
        Plot plot = PlotManager.getInstance().getPlot(plotID);
        if (plot == null) return;
        long time = System.currentTimeMillis();
        Location location = plot.getBorderBottomLeftCorner().toLocation(plot.getCreatedWorld());
        Location location2 = plot.getBorderTopRightCorner().toLocation(plot.getCreatedWorld());
        Block pos1Block = location.getBlock();
        Block pos2Block = location2.getBlock();
        int minX = Math.min(pos1Block.getX(), pos2Block.getX());
        int minZ = Math.min(pos1Block.getZ(), pos2Block.getZ());
        int minY = Math.min(pos1Block.getY(), pos2Block.getY());
        int maxX = Math.max(pos1Block.getX(), pos2Block.getX());
        int maxZ = Math.max(pos1Block.getZ(), pos2Block.getZ());
        int maxY = Math.max(pos1Block.getY(), pos2Block.getY());
        World world = location.getWorld();
        if (world == null) return;
        List<VirtualLocation> virtualLocations = new ArrayList<>();
        Bukkit.getScheduler().runTaskAsynchronously(VirtualRealty.getInstance(), () -> {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = maxY; y > minY; y--) {
                        virtualLocations.add(new VirtualLocation(x,y,z));
                    }
                }
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(VirtualRealty.getInstance(), () -> {
                int i = 0;
                for (VirtualLocation virtualLocation : virtualLocations) {
                    Block block = world.getBlockAt(virtualLocation.getX(), virtualLocation.getY(), virtualLocation.getZ());
                    if (block.getType() == Material.AIR) continue;
                    block.setType(Material.AIR);
                    i++;
                }
                VirtualRealty.debug("Pasted " + i + " air blocks in: " + (System.currentTimeMillis() - time) + " ms");
                Bukkit.getScheduler().runTaskAsynchronously(VirtualRealty.getInstance(), () -> {
                    List<List<VirtualBlock>> chunks = chunkArrayList(blocks, 15000);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(VirtualRealty.getInstance(), () -> {
                        for (int j = 0; j < chunks.size(); j++) {
                            int finalJ = j;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    List<VirtualBlock> virtualBlocks = chunks.get(finalJ);
                                    for (VirtualBlock block : virtualBlocks) {
                                        Location blockLocation = new Location(plot.getCreatedWorld(), block.getX(), block.getY(), block.getZ());
                                        Block oldBlock = blockLocation.getBlock();
                                        if (VirtualRealty.legacyVersion) {
                                            try {
                                                oldBlock.setType(VMaterial.getMaterial(block.getMaterial()));
                                                Method m = Block.class.getDeclaredMethod("setData", byte.class);
                                                m.setAccessible(true);
                                                m.invoke(oldBlock, block.getData());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            oldBlock.setBlockData(
                                                    Bukkit.createBlockData("minecraft:" + block.getBlockData()),
                                                    false
                                            );
                                        }
                                    }
                                    VirtualRealty.debug("Pasted chunk #" + finalJ);
                                }
                            }.runTaskLater(VirtualRealty.getInstance(), 2L * finalJ);
                        }
                        VirtualRealty.debug("Pasted " + blocks.size() + " blocks in: " + (System.currentTimeMillis() - time) + " ms");
                        VirtualRealty.debug("Region pasted in: " + (System.currentTimeMillis() - time) + " ms");
                    });
                });
            });
        });
    }

    private static ArrayList<List<VirtualBlock>> chunkArrayList(List<VirtualBlock> arrayToChunk, int chunkSize) {
        ArrayList<List<VirtualBlock>> chunkList = new ArrayList<>();
        int guide = arrayToChunk.size();
        int index = 0;
        int tale = chunkSize;
        while (tale < arrayToChunk.size()){
            chunkList.add(arrayToChunk.subList(index, tale));
            guide = guide - chunkSize;
            index = index + chunkSize;
            tale = tale + chunkSize;
        }
        if (guide >0) {
            chunkList.add(arrayToChunk.subList(index, index + guide));
        }
        return chunkList;
    }

    @SneakyThrows
    public static void save(int plotID, Block block1, Block block2) {
        long time = System.currentTimeMillis();
        LinkedList<VirtualBlock> blocks = SchematicUtil.getStructure(block1, block2);
        Bukkit.getScheduler().runTaskAsynchronously(VirtualRealty.getInstance(), () -> {
            File f = new File(VirtualRealty.plotsSchemaFolder, (VirtualRealty.legacyVersion ? LEGACY_REGION_PREFIX : REGION_PREFIX) + plotID + REGION_SUFFIX);
            long serialization = System.currentTimeMillis();
            byte[] data = SerializationUtils.serialize(blocks);
            VirtualRealty.debug("Serialized in: " + (System.currentTimeMillis() - serialization) + " ms");
            long compression = System.currentTimeMillis();
            try {
                new DataCompressor().compressData(data, f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            VirtualRealty.debug("Compressed in: " + (System.currentTimeMillis() - compression) + " ms");
            VirtualRealty.debug("Region saved in: " + (System.currentTimeMillis() - time) + " ms");
        });
    }

    @SneakyThrows
    public static LinkedList<VirtualBlock> load(int plotID) {
        long time = System.currentTimeMillis();
        File region = new File(VirtualRealty.plotsSchemaFolder, REGION_PREFIX + plotID + REGION_SUFFIX);
        File legacyRegion = new File(VirtualRealty.plotsSchemaFolder, LEGACY_REGION_PREFIX + plotID + REGION_SUFFIX);
        ByteArrayInputStream bais;
        ObjectInputStream ois;
        if (region.exists()) {
            byte[] bytes = new DataCompressor().decompressData(region);
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            VirtualRealty.debug("Loaded in: " + (System.currentTimeMillis() - time) + " ms");
            return (LinkedList<VirtualBlock>) ois.readObject();
        } else if (legacyRegion.exists()) {
            byte[] bytes = new DataCompressor().decompressData(legacyRegion);
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            VirtualRealty.debug("Loaded in: " + (System.currentTimeMillis() - time) + " ms");
            return (LinkedList<VirtualBlock>) ois.readObject();
        }
        return null;
    }

    public static boolean doesPlotFileExist(int plotID) {
        File region = new File(VirtualRealty.plotsSchemaFolder, REGION_PREFIX + plotID + REGION_SUFFIX);
        File legacyRegion = new File(VirtualRealty.plotsSchemaFolder, LEGACY_REGION_PREFIX + plotID + REGION_SUFFIX);
        File oldRegion = new File(VirtualRealty.plotsSchemaFolder, OldSchematicUtil.OLD_REGION_PREFIX + plotID + OldSchematicUtil.OLD_REGION_SUFFIX);
        return region.exists() || legacyRegion.exists() || oldRegion.exists();
    }

    public static boolean isPlotFileLegacy(int plotID) throws FileNotFoundException {
        File region = new File(VirtualRealty.plotsSchemaFolder, REGION_PREFIX + plotID + REGION_SUFFIX);
        File legacyRegion = new File(VirtualRealty.plotsSchemaFolder, LEGACY_REGION_PREFIX + plotID + REGION_SUFFIX);
        if (region.exists()) return false;
        if (legacyRegion.exists()) return true;
        throw new FileNotFoundException();
    }

    public static void deletePlotFile(int id) {
        File file = new File(VirtualRealty.plotsSchemaFolder, OldSchematicUtil.OLD_REGION_PREFIX + id + OldSchematicUtil.OLD_REGION_SUFFIX);
        if (file.exists()) {
            if (file.delete()) {
                VirtualRealty.debug("Deleted plot file (" + file.getName() + ")");
            }
        }
        file = new File(VirtualRealty.plotsSchemaFolder, REGION_PREFIX + id + REGION_SUFFIX);
        if (file.exists()) {
            if (file.delete()) {
                VirtualRealty.debug("Deleted plot file (" + file.getName() + ")");
            }
        }
        file = new File(VirtualRealty.plotsSchemaFolder, LEGACY_REGION_PREFIX + id + REGION_SUFFIX);
        if (file.exists()) {
            if (file.delete()) {
                VirtualRealty.debug("Deleted plot file (" + file.getName() + ")");
            }
        }
    }

    public static boolean isOldSerialization(int plotID) {
        File oldRegion = new File(VirtualRealty.plotsSchemaFolder, "plot" + plotID + ".region");
        return oldRegion.exists();
    }

}
