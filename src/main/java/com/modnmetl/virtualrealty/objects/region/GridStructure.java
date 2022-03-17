package com.modnmetl.virtualrealty.objects.region;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.RegionUtil;
import com.modnmetl.virtualrealty.utils.data.VirtualBlock;
import lombok.Data;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Data
public class GridStructure {

    public static final HashMap<UUID, Set<Integer>> ACTIVE_GRIDS = new HashMap<>();

    private final Player viewer;
    private final Location previewLocation;
    private int length;
    private int height;
    private int width;
    private int cuboidId;
    private final Set<VirtualBlock> changedBlocks;
    private final World world;
    private boolean displayingBlocks;
    private long displayTicks;

    public GridStructure(Player viewer, int length, int height, int width, int cuboidId, World world, long displayTicks, Location previewLocation) {
        if (!ACTIVE_GRIDS.containsKey(viewer.getUniqueId())) {
            ACTIVE_GRIDS.put(viewer.getUniqueId(), new HashSet<>());
        }
        ACTIVE_GRIDS.get(viewer.getUniqueId()).add(cuboidId);
        this.viewer = viewer;
        this.length = length;
        this.height = height;
        this.width = width;
        this.cuboidId = cuboidId;
        this.changedBlocks = new HashSet<>();
        this.world = world;
        this.displayTicks = displayTicks;
        this.previewLocation = previewLocation;
    }

    public static boolean isCuboidGridDisplaying(Player player, int cuboidId) {
        return ACTIVE_GRIDS.containsKey(player.getUniqueId()) && ACTIVE_GRIDS.get(player.getUniqueId()).contains(cuboidId);
    }

    public void preview(boolean visualization, boolean colliding) {
        preview(null, visualization, colliding);
    }

    public void preview(Location playerPreviewLocation, boolean visualization, boolean colliding) {
        changedBlocks.clear();
        Plot plot = PlotManager.getPlot(cuboidId);
        Location location = previewLocation;
        LinkedList<Block> blocks = new LinkedList<>();
        LinkedList<Block> borderBlocks = new LinkedList<>();
        Direction direction = Direction.byYaw(location.getYaw());
        int maxX;
        int maxZ;
        int minX;
        int minZ;
        switch(direction) {
            case SOUTH: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + getLength() + 1;
                minX = location.getBlockX() - getWidth() + 1;
                minZ = location.getBlockZ() - 1;
                break;
            }
            case WEST: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX() - getLength() + 1;
                minZ = location.getBlockZ() - getWidth();
                break;
            }
            case NORTH: {
                maxX = location.getBlockX() + getWidth() + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - getLength();
                break;
            }
            case EAST: {
                maxX = location.getBlockX() + getLength() + 1;
                maxZ = location.getBlockZ() + getWidth() + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - 1;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
        for (int x = minX - 1; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                    borderBlocks.add(location.getWorld().getBlockAt(x, location.getBlockY(), z));
                }
            }
        }
        Location topRightCorner;
        Location bottomLeftCorner;
        if (plot != null) {
            topRightCorner = plot.getBorderTopRightCorner().toLocation(plot.getCreatedWorld());
            bottomLeftCorner = plot.getBorderBottomLeftCorner().toLocation(plot.getCreatedWorld());
        } else {
            switch (direction) {
                case SOUTH: {
                    bottomLeftCorner = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                    topRightCorner = new Location(location.getWorld(), location.getBlockX() - getWidth(), location.getBlockY() + getHeight(), location.getBlockZ() + getLength());
                    break;
                }
                case WEST: {
                    bottomLeftCorner = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                    topRightCorner = new Location(location.getWorld(), location.getBlockX() - getLength(), location.getBlockY() + getHeight(), location.getBlockZ() - getWidth());
                    break;
                }
                case NORTH: {
                    bottomLeftCorner = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                    topRightCorner = new Location(location.getWorld(), location.getBlockX() + getWidth(), location.getBlockY() + getHeight(), location.getBlockZ() - getLength());
                    break;
                }
                case EAST: {
                    bottomLeftCorner = new Location(location.getWorld(), location.getBlockX() + getLength(), location.getBlockY() - 10, location.getBlockZ() - 1);
                    topRightCorner = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() + getHeight(), location.getBlockZ() + getWidth());
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + direction);
            }
        }
        for (Block borderBlock : new LinkedList<>(borderBlocks)) {
            // top grid floor
            blocks.add(borderBlock.getLocation().add(0, topRightCorner.getBlockY() - borderBlock.getLocation().getBlockY(), 0).getBlock());
            // bottom grid floor
            blocks.add(borderBlock.getLocation().subtract(0, borderBlock.getLocation().getBlockY() - bottomLeftCorner.getBlockY(), 0).getBlock());
            Location playerBlockLocation = borderBlock.getLocation();
            if (playerPreviewLocation != null) {
                playerBlockLocation.setY(playerPreviewLocation.getY());
            } else {
                playerBlockLocation.add(0, 1, 0);
            }
            // plot player level border
            blocks.add(playerBlockLocation.getBlock());
        }
        // grid pillars
        for (int y = bottomLeftCorner.getBlockY(); y < bottomLeftCorner.getBlockY() + getHeight() + 10; y++) {
            blocks.add(new Location(world, minX - 1, y, minZ).getBlock());
            blocks.add(new Location(world, maxX - 1, y, minZ).getBlock());
            blocks.add(new Location(world, minX - 1, y, maxZ - 1).getBlock());
            blocks.add(new Location(world, maxX - 1, y, maxZ - 1).getBlock());
        }
        swapBlocks(
                blocks,
                visualization && colliding
        );
        if (displayTicks == 0) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                removeGrid();
                ACTIVE_GRIDS.get(viewer.getUniqueId()).remove(cuboidId);
            }
        }.runTaskLater(VirtualRealty.getInstance(), displayTicks);
    }

    @SneakyThrows
    public void removeGrid() {
        for (VirtualBlock changedBlock : changedBlocks) {
            Block changedBukkitBlock = changedBlock.getBlock(world);
            if (VirtualRealty.legacyVersion) {
                viewer.sendBlockChange(changedBukkitBlock.getLocation(), changedBukkitBlock.getType(), changedBukkitBlock.getData());
            } else {
                viewer.sendBlockChange(changedBukkitBlock.getLocation(), changedBukkitBlock.getBlockData());
            }
        }
    }

    private void swapBlocks(LinkedList<Block> blocks, boolean collidingArea) {
        Plot plot = PlotManager.getPlot(previewLocation);
        for (Block block : blocks) {
            Location blockLocation = block.getLocation();
            VirtualBlock convertedBlock;
            if (VirtualRealty.legacyVersion) {
                convertedBlock = new VirtualBlock(block.getX(), block.getY(), block.getZ(), block.getType().getId(), block.getData());
                viewer.sendBlockChange(blockLocation, Objects.requireNonNull(Material.matchMaterial("STAINED_GLASS")), ((plot != null && plot.getID() == cuboidId) ? (byte)1 : collidingArea ? (byte)14 : (byte)5));
            } else {
                convertedBlock = new VirtualBlock(block.getX(), block.getY(), block.getZ(), block.getBlockData().getAsString());
                BlockData greenBlockData = Material.LIME_STAINED_GLASS.createBlockData();
                BlockData redBlockData = Material.RED_STAINED_GLASS.createBlockData();
                BlockData orangeBlockData = Material.ORANGE_STAINED_GLASS.createBlockData();
                viewer.sendBlockChange(blockLocation, ((plot != null && plot.getID() == cuboidId) ? orangeBlockData : collidingArea ? redBlockData : greenBlockData));
            }
            changedBlocks.add(convertedBlock);
        }
    }

}
