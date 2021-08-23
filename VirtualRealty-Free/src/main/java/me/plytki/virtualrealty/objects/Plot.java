package me.plytki.virtualrealty.objects;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.Direction;
import me.plytki.virtualrealty.enums.PlotSize;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.math.BlockVector3;
import me.plytki.virtualrealty.sql.SQL;
import me.plytki.virtualrealty.utils.SchematicUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
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
    private Location createdLocation;
    private Direction createdDirection;
    private BlockVector3 bottomLeftCorner;
    private BlockVector3 topRightCorner;
    private BlockVector3 borderBottomLeftCorner;
    private BlockVector3 borderTopRightCorner;
    private GameMode selectedGameMode;

    @Override
    public String toString() {
        return "{ ID: " + ID + ", ownedBy: " + ownedBy + "}";
    }

    public Plot(Location location, Material floorMaterial, PlotSize plotSize) {
        this.ID = PlotManager.plots.isEmpty() ? 10000 : PlotManager.plots.get(PlotManager.plots.size() - 1).getID() + 1;
        this.ownedBy = null;
        this.assignedBy = null;
        this.ownedUntilDate = LocalDateTime.of(2999, 1, 1, 0, 0);
        this.floorMaterial = floorMaterial;
        this.plotSize = plotSize;
        this.length = plotSize.getLength();
        this.width = plotSize.getWidth();
        this.height = plotSize.getHeight();
        this.createdLocation = location;
        this.createdDirection = Direction.byYaw(location.getYaw());
        this.selectedGameMode = GameMode.CREATIVE;
        initialize();
        initializeCorners();
    }

    public Plot(Location location, Material floorMaterial, int length, int width, int height) {
        this.ID =  PlotManager.plots.isEmpty() ? 10000 : PlotManager.plots.get(PlotManager.plots.size() - 1).getID() + 1;
        this.ownedBy = null;
        this.assignedBy = null;
        this.ownedUntilDate = LocalDateTime.of(2999, 12, 31, 0, 0);
        this.floorMaterial = floorMaterial;
        this.plotSize = PlotSize.CUSTOM;
        this.length = length;
        this.width = width;
        this.height = height;
        this.createdLocation = location;
        this.createdDirection = Direction.byYaw(location.getYaw());
        this.selectedGameMode = GameMode.CREATIVE;
        initialize();
        initializeCorners();
    }


    public Plot(ResultSet rs) {
        try {
            this.ID = rs.getInt("ID");
            this.ownedBy = rs.getString("ownedBy").isEmpty() ? null : UUID.fromString(rs.getString("ownedBy"));
            this.assignedBy = rs.getString("assignedBy").equalsIgnoreCase("null") ? null : rs.getString("assignedBy");
            this.ownedUntilDate = rs.getTimestamp("ownedUntilDate").toLocalDateTime();
            this.floorMaterial = Material.getMaterial(rs.getString("floorMaterial"));
            this.plotSize = PlotSize.valueOf(rs.getString("plotSize"));
            this.length = rs.getInt("length");
            this.width = rs.getInt("width");
            this.height = rs.getInt("height");
            ArrayList<String> location = new ArrayList<>(Arrays.asList(rs.getString("createdLocation").subSequence(0, rs.getString("createdLocation").length() - 1).toString().split(";")));
            this.createdLocation = rs.getString("createdLocation").isEmpty() ? null : new Location(Bukkit.getWorld(location.get(0)), Double.parseDouble(location.get(1)), Double.parseDouble(location.get(2)), Double.parseDouble(location.get(3)), Float.parseFloat(location.get(4)), Float.parseFloat(location.get(5)));
            this.createdDirection = Direction.byYaw(createdLocation.getYaw());
            this.selectedGameMode = GameMode.CREATIVE;
            initializeCorners();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public void setFloorMaterial(Material floorMaterial) {
        this.floorMaterial = floorMaterial;
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

    public BlockVector3 getCenter() {
        return new Cuboid(borderBottomLeftCorner, borderTopRightCorner, createdLocation.getWorld()).getCenterVector();
    }

    public GameMode getSelectedGameMode() {
        return selectedGameMode;
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
                        floorBlock.setType(floorMaterial);
                    }
                }
                break;
            }
            case WEST: {
                for (int x = location.getBlockX() - length + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ() - width + 1; z < location.getBlockZ() + 1; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                    }
                }
                break;
            }
            case NORTH: {
                for (int x = location.getBlockX(); x < location.getBlockX() + width; x++) {
                    for (int z = location.getBlockZ() - length + 1; z < location.getBlockZ() + 1; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                    }
                }
                break;
            }
            case EAST: {
                 for (int x = location.getBlockX(); x < location.getBlockX() + length; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + width; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
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
        //System.out.println(
                prepareBlocks(createdLocation);
                        //+ " blocks");
    }

    public List<Block> prepareBlocks(Location location) {
        List<Block> blocks = new ArrayList<>();
        Direction direction = Direction.byYaw(location.getYaw());
        Location location1;
        Location location2;
        long time = System.currentTimeMillis();
        long time2;
        switch(direction) {
            case SOUTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - width, location.getBlockY() + height, location.getBlockZ() + length);
                SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                schematicUtil.save(ID, schematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                time2 = System.currentTimeMillis();
                // SchematicUtil.saveSchematic(ID, location1, location2);
                for (int x = location.getBlockX() - width + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + length; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        for (int y = location.getBlockY(); y < location.getBlockY() + height; y++) {
                            Block block = location.getWorld().getBlockAt(x, y, z);
                            Block airBlock = location.getWorld().getBlockAt(x, y + 1, z);
                            airBlock.setType(Material.AIR);
                            blocks.add(block);
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
                            borderBlock.setType(Material.matchMaterial("STONE_BRICK_SLAB"));
                        }
                    }
                }
                break;
            }
            case WEST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - length, location.getBlockY() + height, location.getBlockZ() - width);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.save(ID, schematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                time2 = System.currentTimeMillis();
                //SchematicUtil.saveSchematic(ID, location1, location2);
                for (int x = location.getBlockX() - length + 1; x < location.getBlockX() + 1; x++) {
                    for (int z = location.getBlockZ() - width + 1; z < location.getBlockZ() + 1; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        for (int y = location.getBlockY(); y < location.getBlockY() + height; y++) {
                            Block block = location.getWorld().getBlockAt(x, y, z);
                            Block airBlock = location.getWorld().getBlockAt(x, y + 1, z);
                            airBlock.setType(Material.AIR);
                            blocks.add(block);
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
                            borderBlock.setType(Material.matchMaterial("STONE_BRICK_SLAB"));
                        }
                    }
                }
                break;
            }
            case NORTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() + width, location.getBlockY() + height, location.getBlockZ() - length);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.save(ID, schematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                time2 = System.currentTimeMillis();
                //SchematicUtil.saveSchematic(ID, location1, location2);
                for (int x = location.getBlockX(); x < location.getBlockX() + width; x++) {
                    for (int z = location.getBlockZ() - length + 1; z < location.getBlockZ() + 1; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        for (int y = location.getBlockY(); y < location.getBlockY() + height; y++) {
                            Block block = location.getWorld().getBlockAt(x, y, z);
                            Block airBlock = location.getWorld().getBlockAt(x, y + 1, z);
                            airBlock.setType(Material.AIR);
                            blocks.add(block);
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
                            borderBlock.setType(Material.matchMaterial("STONE_BRICK_SLAB"));
                        }
                    }
                }
                break;
            }
            case EAST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + length, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() + height, location.getBlockZ() + width);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.save(ID, schematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                time2 = System.currentTimeMillis();
                //SchematicUtil.saveSchematic(ID, location1, location2);
                for (int x = location.getBlockX(); x < location.getBlockX() + length; x++) {
                    for (int z = location.getBlockZ(); z < location.getBlockZ() + width; z++) {
                        Block floorBlock = location.getWorld().getBlockAt(x, location.getBlockY(), z);
                        floorBlock.setType(floorMaterial);
                        for (int y = location.getBlockY(); y < location.getBlockY() + height; y++) {
                            Block block = location.getWorld().getBlockAt(x, y, z);
                            Block airBlock = location.getWorld().getBlockAt(x, y + 1, z);
                            airBlock.setType(Material.AIR);
                            blocks.add(block);
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
                            borderBlock.setType(Material.matchMaterial("STONE_BRICK_SLAB"));
                        }
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
        //System.out.println(time2 - time + "  ms (saved)");
        return blocks;
    }

    public void unloadPlot() {
        switch (createdDirection) {
            case SOUTH: {
//                SchematicUtil3.loadSchematic(ID, createdLocation.getWorld(),
//                        createdLocation.getBlockX() - width, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                            createdLocation.getBlockX() - width, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1));
                break;
            }
            case WEST: {
//                SchematicUtil3.loadSchematic(ID, createdLocation.getWorld(),
//                        createdLocation.getBlockX() - length, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - width);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                            createdLocation.getBlockX() - length, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - width));
                break;
            }
            case NORTH: {
//                SchematicUtil3.loadSchematic(ID, createdLocation.getWorld(),
//                        createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - length);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.paste(ID, new Location(createdLocation.getWorld(),
                            createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - length));
                break;
            }
            case EAST: {
//                SchematicUtil3.loadSchematic(ID, createdLocation.getWorld(),
//                        createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1);
                    SchematicUtil schematicUtil = new SchematicUtil(VirtualRealty.getInstance());
                    schematicUtil.paste(ID, new Location(createdLocation.getWorld(),
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
                    "'" + Timestamp.valueOf(this.ownedUntilDate) + "', '" + this.floorMaterial + "'," +
                    " '" + this.plotSize + "', '" + this.length + "', '" + this.width + "', '" + this.height + "', '" + builder.toString() + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        try {
            SQL.getStatement().execute("UPDATE `vr_plots` SET `ownedBy`='" + (this.ownedBy == null ? "" : this.ownedBy.toString()) + "', `assignedBy`='" + this.assignedBy + "'," +
                    " `ownedUntilDate`='" + Timestamp.valueOf(this.ownedUntilDate) + "', `floorMaterial`='" + this.floorMaterial + "'," +
                    " `plotSize`='" + this.plotSize + "', `length`='" + this.length + "', `width`='" + this.width + "', `height`='" + this.height + "'" +
                    " WHERE `ID`='" + this.ID + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        this.unloadPlot();
        try {
            SQL.getStatement().execute("DELETE FROM `vr_plots` WHERE `ID` = '" + ID + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int id = ID;
        File file = new File(VirtualRealty.plotsSchemaFolder, "plot" + id + ".schem");
        file.delete();
        PlotManager.plots.remove(this);
    }

    public Direction getCreatedDirection() {
        return createdDirection;
    }

    public void setCreatedDirection(Direction createdDirection) {
        this.createdDirection = createdDirection;
    }
}