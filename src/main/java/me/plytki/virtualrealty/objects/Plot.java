package me.plytki.virtualrealty.objects;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.Direction;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.math.BlockVector3;
import me.plytki.virtualrealty.sql.SQL;
import me.plytki.virtualrealty.utils.SchematicUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Plot {

    private int ID;
    private UUID ownedBy;
    private String assignedBy;
    private LocalDateTime ownedUntilDate;
    private PlotSize plotSize;
    private int length;
    private int width;
    private int height;
    private Material floorMaterial;
    private byte floorData;
    private Location createdLocation;
    private Direction createdDirection;
    private BlockVector3 bottomLeftCorner;
    private BlockVector3 topRightCorner;
    private BlockVector3 borderBottomLeftCorner;
    private BlockVector3 borderTopRightCorner;
    private GameMode selectedGameMode;
    private String createdWorld;

    @Override
    public String toString() {
        return "{ ID: " + ID + ", ownedBy: " + ownedBy + "}";
    }

    public Plot(Location location, Material floorMaterial, PlotSize plotSize) {
        this.ID = PlotManager.plots.isEmpty() ? 10000 : PlotManager.plots.get(PlotManager.plots.size() - 1).getID() + 1;
        this.ownedBy = null;
        this.assignedBy = null;
        this.ownedUntilDate = LocalDateTime.of(2999, 12, 31, 0, 0);
        this.floorMaterial = floorMaterial;
        this.floorData = 0;
        this.plotSize = plotSize;
        this.length = plotSize.getLength();
        this.width = plotSize.getWidth();
        this.height = plotSize.getHeight();
        this.createdLocation = location;
        this.createdDirection = Direction.byYaw(location.getYaw());
        this.selectedGameMode = VirtualRealty.getPluginConfiguration().getGameMode();
        this.createdWorld = location.getWorld().getName();
        initialize();
        initializeCorners();
        PlotManager.resetPlotMarker(this);
    }

    public Plot(Location location, Material floorMaterial, int length, int width, int height) {
        this.ID =  PlotManager.plots.isEmpty() ? 10000 : PlotManager.plots.get(PlotManager.plots.size() - 1).getID() + 1;
        this.ownedBy = null;
        this.assignedBy = null;
        this.ownedUntilDate = LocalDateTime.of(2999, 12, 31, 0, 0);
        this.floorMaterial = floorMaterial;
        this.floorData = 0;
        this.plotSize = PlotSize.CUSTOM;
        this.length = length;
        this.width = width;
        this.height = height;
        this.createdLocation = location;
        this.createdDirection = Direction.byYaw(location.getYaw());
        this.selectedGameMode = VirtualRealty.getPluginConfiguration().getGameMode();
        this.createdWorld = location.getWorld().getName();
        initialize();
        initializeCorners();
        PlotManager.resetPlotMarker(this);
    }


    public Plot(ResultSet rs) {
        try {
            this.ID = rs.getInt("ID");
            this.ownedBy = rs.getString("ownedBy").isEmpty() ? null : UUID.fromString(rs.getString("ownedBy"));
            this.assignedBy = rs.getString("assignedBy").equalsIgnoreCase("null") ? null : rs.getString("assignedBy");
            this.ownedUntilDate = rs.getTimestamp("ownedUntilDate").toLocalDateTime();
            this.floorMaterial = Material.getMaterial(rs.getString("floorMaterial").split(":")[0]);
            this.floorData = rs.getString("floorMaterial").split(":").length == 1 ? 0 : Byte.parseByte(rs.getString("floorMaterial").split(":")[1]);
            this.plotSize = PlotSize.valueOf(rs.getString("plotSize"));
            this.length = rs.getInt("length");
            this.width = rs.getInt("width");
            this.height = rs.getInt("height");
            ArrayList<String> location = new ArrayList<>(Arrays.asList(rs.getString("createdLocation").subSequence(0, rs.getString("createdLocation").length() - 1).toString().split(";")));
            Location createLocation = new Location(Bukkit.getWorld(location.get(0)), Double.parseDouble(location.get(1)), Double.parseDouble(location.get(2)), Double.parseDouble(location.get(3)), Float.parseFloat(location.get(4)), Float.parseFloat(location.get(5)));
            this.createdLocation = rs.getString("createdLocation").isEmpty() ? null : createLocation;
            this.createdDirection = Direction.byYaw(createdLocation.getYaw());
            this.selectedGameMode = GameMode.CREATIVE;
            this.createdWorld = location.get(0);
            initializeCorners();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getXMin() {
        return Math.min(this.getBorderBottomLeftCorner().getBlockX(), this.borderTopRightCorner.getBlockX());
    }

    public int getXMax() {
        return Math.max(this.getBorderBottomLeftCorner().getBlockX(), this.borderTopRightCorner.getBlockX());
    }

    public int getZMin() {
        return Math.min(this.getBorderBottomLeftCorner().getBlockZ(), this.borderTopRightCorner.getBlockZ());
    }

    public int getZMax() {
        return Math.max(this.getBorderBottomLeftCorner().getBlockZ(), this.borderTopRightCorner.getBlockZ());
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public UUID getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(UUID ownedBy) {
        this.ownedBy = ownedBy;
        updateMarker();
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getOwnedUntilDate() {
        return ownedUntilDate;
    }

    public void setOwnedUntilDate(LocalDateTime ownedUntilDate) {
        this.ownedUntilDate = ownedUntilDate;
        updateMarker();
    }

    public PlotSize getPlotSize() {
        return plotSize;
    }

    public void setPlotSize(PlotSize plotSize) {
        this.plotSize = plotSize;
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

    public void setFloorMaterial(Material floorMaterial, byte data) {
        this.floorMaterial = floorMaterial;
        this.floorData = data;
    }

    public Location getCreatedLocation() {
        return createdLocation;
    }

    public void setCreatedLocation(Location createdLocation) {
        this.createdLocation = createdLocation;
    }

    public BlockVector3 getBottomLeftCorner() {
        return bottomLeftCorner;
    }

    public void setBottomLeftCorner(BlockVector3 bottomLeftCorner) {
        this.bottomLeftCorner = bottomLeftCorner;
    }

    public BlockVector3 getTopRightCorner() {
        return topRightCorner;
    }

    public void setTopRightCorner(BlockVector3 topRightCorner) {
        this.topRightCorner = topRightCorner;
    }

    public BlockVector3 getBorderBottomLeftCorner() {
        return borderBottomLeftCorner;
    }

    public void setBorderBottomLeftCorner(BlockVector3 borderBottomLeftCorner) {
        this.borderBottomLeftCorner = borderBottomLeftCorner;
    }

    public BlockVector3 getBorderTopRightCorner() {
        return borderTopRightCorner;
    }

    public void setBorderTopRightCorner(BlockVector3 borderTopRightCorner) {
        this.borderTopRightCorner = borderTopRightCorner;
    }

    public BlockVector3 getBorderedCenter() {
        return new Cuboid(borderBottomLeftCorner, borderTopRightCorner, createdLocation.getWorld()).getCenterVector();
    }

    public BlockVector3 getCenter() {
        return new Cuboid(bottomLeftCorner, topRightCorner, createdLocation.getWorld()).getCenterVector();
    }

    public GameMode getSelectedGameMode() {
        return selectedGameMode;
    }

    public String getCreatedWorld() {
        return createdWorld;
    }

    public void setSelectedGameMode(GameMode selectedGameMode) {
        this.selectedGameMode = selectedGameMode;
    }

    public void initializeFloor() {
        Location location = createdLocation;
        Direction direction = Direction.byYaw(location.getYaw());
        switch(direction) {
            case SOUTH: {
                for (int x = location.getBlockX() - width + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + length; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m = Block.class.getDeclaredMethod("setType", Material.class);
                                m.setAccessible(true);
                                m.invoke(floorBlock, this.floorMaterial);
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, this.floorData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            floorBlock.setType(floorMaterial);
                        }
                    }
                }
                break;
            }
            case WEST: {
                for (int x = location.getBlockX() - length + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ() - width + 1; z < location.getBlockZ() + 1; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m = Block.class.getDeclaredMethod("setType", Material.class);
                                m.setAccessible(true);
                                m.invoke(floorBlock, this.floorMaterial);
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, this.floorData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            floorBlock.setType(floorMaterial);
                        }
                    }
                }
                break;
            }
            case NORTH: {
                for (int x = location.getBlockX(); x < location.getBlockX() + width; x++) {
                    for (int z = location.getBlockZ() - length + 1; z < location.getBlockZ() + 1; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m = Block.class.getDeclaredMethod("setType", Material.class);
                                m.setAccessible(true);
                                m.invoke(floorBlock, this.floorMaterial);
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, this.floorData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            floorBlock.setType(floorMaterial);
                        }
                    }
                }
                break;
            }
            case EAST: {
                 for (int x = location.getBlockX(); x < location.getBlockX() + length; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + width; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m = Block.class.getDeclaredMethod("setType", Material.class);
                                m.setAccessible(true);
                                m.invoke(floorBlock, this.floorMaterial);
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, this.floorData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            floorBlock.setType(floorMaterial);
                        }
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public void initializeCorners() {
        Location location = createdLocation;
        Direction direction = Direction.byYaw(location.getYaw());
        Location location1;
        Location location2;
        Location border1;
        Location border2;
        switch(direction) {
            case SOUTH: {
                location1 = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(location.getWorld(), location.getBlockX() - width + 1, location.getBlockY() + height, location.getBlockZ() + length - 1);
                border1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                border2 = new Location(location.getWorld(), location.getBlockX() - width, location.getBlockY() + height, location.getBlockZ() + length);
                break;
            }
            case WEST: {
                location1 = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(location.getWorld(), location.getBlockX() - length + 1, location.getBlockY() + height, location.getBlockZ() - width + 1);
                border1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                border2 = new Location(location.getWorld(), location.getBlockX() - length, location.getBlockY() + height, location.getBlockZ() - width);
                break;
            }
            case NORTH: {
                location1 = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(location.getWorld(), location.getBlockX() + width - 1, location.getBlockY() + height, location.getBlockZ() - length + 1);
                border1 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                border2 = new Location(location.getWorld(), location.getBlockX() + width, location.getBlockY() + height, location.getBlockZ() - length);
                break;
            }
            case EAST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + length - 1, location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() + height, location.getBlockZ() + width - 1);
                border1 = new Location(location.getWorld(), location.getBlockX() + length, location.getBlockY() - 10, location.getBlockZ() - 1);
                border2 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() + height, location.getBlockZ() + width);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
        this.bottomLeftCorner = BlockVector3.at(location1.getBlockX(), location1.getBlockY(), location1.getBlockZ());
        this.topRightCorner = BlockVector3.at(location2.getBlockX(), location2.getBlockY(), location2.getBlockZ());
        this.borderBottomLeftCorner = BlockVector3.at(border1.getBlockX(), border1.getBlockY(), border1.getBlockZ());
        this.borderTopRightCorner = BlockVector3.at(border2.getBlockX(), border2.getBlockY(), border2.getBlockZ());
    }

    public void initialize() {
        prepareBlocks(createdLocation);
    }

    public void setBorder(Material material, byte data) {
        Location location = this.getCreatedLocation();
        Direction direction = Direction.byYaw(location.getYaw());
        switch(direction) {
            case SOUTH: {
                int maxX = location.getBlockX() + 1 + 1;
                int maxZ = location.getBlockZ() + length + 1;
                int minX = location.getBlockX() - width + 1;
                int minZ = location.getBlockZ() - 1;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(material);
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, data);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(material);
                            }
                        }
                    }
                }
                break;
            }
            case WEST: {
                int maxX = location.getBlockX() + 1 + 1;
                int maxZ = location.getBlockZ() + 1 + 1;
                int minX = location.getBlockX() - length + 1;
                int minZ = location.getBlockZ() - width;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(material);
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, data);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(material);
                            }
                        }
                    }
                }
                break;
            }
            case NORTH: {
                int maxX = location.getBlockX() + width + 1;
                int maxZ = location.getBlockZ() + 1 + 1;
                int minX = location.getBlockX();
                int minZ = location.getBlockZ() - length;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(material);
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, data);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(material);
                            }
                        }
                    }
                }
                break;
            }
            case EAST: {
                int maxX = location.getBlockX() + length + 1;
                int maxZ = location.getBlockZ() + width + 1;
                int minX = location.getBlockX();
                int minZ = location.getBlockZ() - 1;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(material);
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, data);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(material);
                            }
                        }
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public void prepareBlocks(Location location) {
        Direction direction = Direction.byYaw(location.getYaw());
        Location location1;
        Location location2;
        switch (direction) {
            case SOUTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - width, location.getBlockY() + height, location.getBlockZ() + length);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                for (int x = location.getBlockX() - width + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + length; z++) {
                        for (int y = location.getBlockY() + height; y > location.getBlockY() - 1; y--) {
                            Block airBlock = location.getWorld().getBlockAt(x, y, z);
                            airBlock.setType(Material.AIR);
                        }
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, plotSize.getFloorData());
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                int maxX = location.getBlockX() + 1 + 1;
                int maxZ = location.getBlockZ() + length + 1;
                int minX = location.getBlockX() - width + 1;
                int minZ = location.getBlockZ() - 1;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(plotSize.getBorderMaterial());
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, plotSize.getBorderData());
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(plotSize.getBorderMaterial());
                            }
                        }
                    }
                }
                break;
            }
            case WEST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - length, location.getBlockY() + height, location.getBlockZ() - width);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                for (int x = location.getBlockX() - length + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ() - width + 1; z < location.getBlockZ() + 1; z++) {
                        for (int y = location.getBlockY() + height; y > location.getBlockY() - 1; y--) {
                            Block airBlock = location.getWorld().getBlockAt(x, y, z);
                            airBlock.setType(Material.AIR);
                        }
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, plotSize.getFloorData());
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                int maxX = location.getBlockX() + 1 + 1;
                int maxZ = location.getBlockZ() + 1 + 1;
                int minX = location.getBlockX() - length + 1;
                int minZ = location.getBlockZ() - width;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(plotSize.getBorderMaterial());
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, plotSize.getBorderData());
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(plotSize.getBorderMaterial());
                            }
                        }
                    }
                }
                break;
            }
            case NORTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() + width, location.getBlockY() + height, location.getBlockZ() - length);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                for (int x = location.getBlockX(); x < location.getBlockX() + width; x++) {
                    for (int z = location.getBlockZ() - length + 1; z < location.getBlockZ() + 1; z++) {
                        for (int y = location.getBlockY() + height; y > location.getBlockY() - 1; y--) {
                            Block airBlock = location.getWorld().getBlockAt(x, y, z);
                            airBlock.setType(Material.AIR);
                        }
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, plotSize.getFloorData());
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                int maxX = location.getBlockX() + width + 1;
                int maxZ = location.getBlockZ() + 1 + 1;
                int minX = location.getBlockX();
                int minZ = location.getBlockZ() - length;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(plotSize.getBorderMaterial());
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, plotSize.getBorderData());
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(plotSize.getBorderMaterial());
                            }
                        }
                    }
                }
                break;
            }
            case EAST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + length, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() + height, location.getBlockZ() + width);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                for (int x = location.getBlockX(); x < location.getBlockX() + length; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + width; z++) {
                        for (int y = location.getBlockY() + height; y > location.getBlockY() - 1; y--) {
                            Block airBlock = location.getWorld().getBlockAt(x, y, z);
                            airBlock.setType(Material.AIR);
                        }
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        if (VirtualRealty.isLegacy) {
                            try {
                                Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                m2.setAccessible(true);
                                m2.invoke(floorBlock, plotSize.getFloorData());
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                int maxX = location.getBlockX() + length + 1;
                int maxZ = location.getBlockZ() + width + 1;
                int minX = location.getBlockX();
                int minZ = location.getBlockZ() - 1;
                for (int x = minX - 1; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                            Block borderBlock = location.getWorld().getBlockAt(x, location.getBlockY() + 1, z);
                            if (VirtualRealty.isLegacy) {
                                borderBlock.setType(plotSize.getBorderMaterial());
                                try {
                                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                                    m2.setAccessible(true);
                                    m2.invoke(borderBlock, plotSize.getBorderData());
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                borderBlock.setType(plotSize.getBorderMaterial());
                            }
                        }
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public void unloadPlot() {
        switch (createdDirection) {
            case SOUTH: {
                SchematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                        createdLocation.getBlockX() - width, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1));
                break;
            }
            case WEST: {
                SchematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                        createdLocation.getBlockX() - length, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - width));
                break;
            }
            case NORTH: {
                SchematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                        createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - length));
                break;
            }
            case EAST: {
                SchematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                        createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1));
                break;
            }
        }
    }

    public void insert() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.createdLocation.getWorld().getName() + ";");
        builder.append(this.createdLocation.getX() + ";");
        builder.append(this.createdLocation.getY() + ";");
        builder.append(this.createdLocation.getZ() + ";");
        builder.append(this.createdLocation.getYaw() + ";");
        builder.append(this.createdLocation.getPitch() + ";");
        try {
            SQL.getStatement().execute("INSERT INTO `vr_plots` (`ID`, `ownedBy`, `assignedBy`, `ownedUntilDate`," +
                    " `floorMaterial`, `plotSize`, `length`, `width`, `height`, `createdLocation`) " +
                    "VALUES ('" + this.ID + "', '" + (this.ownedBy == null ? "" : this.ownedBy.toString()) + "', '" + this.assignedBy + "', " +
                    "'" + Timestamp.valueOf(this.ownedUntilDate) + "', '" + this.floorMaterial + ":" + this.floorData + "'," +
                    " '" + this.plotSize + "', '" + this.length + "', '" + this.width + "', '" + this.height + "', '" + builder.toString() + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        try {
            SQL.getStatement().execute("UPDATE `vr_plots` SET `ownedBy`='" + (this.ownedBy == null ? "" : this.ownedBy.toString()) + "', `assignedBy`='" + this.assignedBy + "'," +
                    " `ownedUntilDate`='" + Timestamp.valueOf(this.ownedUntilDate) + "', `floorMaterial`='" + this.floorMaterial + ":" + this.floorData + "'," +
                    " `plotSize`='" + this.plotSize + "', `length`='" + this.length + "', `width`='" + this.width + "', `height`='" + this.height + "'" +
                    " WHERE `ID`='" + this.ID + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        this.unloadPlot();
        PlotManager.removeDynMapMarker(this);
        try {
            SQL.getStatement().execute("DELETE FROM `vr_plots` WHERE `ID` = '" + ID + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int id = ID;
        File file = new File(VirtualRealty.plotsSchemaFolder, "plot" + id + ".region");
        if (file.exists())
            FileUtils.deleteQuietly(file);
        PlotManager.plots.remove(this);
    }

    public Direction getCreatedDirection() {
        return createdDirection;
    }

    public void setCreatedDirection(Direction createdDirection) {
        this.createdDirection = createdDirection;
    }

    public void updateMarker() {
        PlotManager.resetPlotMarker(this);
    }

}