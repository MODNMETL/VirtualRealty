package com.modnmetl.virtualrealty.objects;

import com.modnmetl.virtualrealty.configs.PluginConfiguration;
import com.modnmetl.virtualrealty.enums.Direction;
import com.modnmetl.virtualrealty.enums.permissions.RegionPermission;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.managers.DynmapManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.data.PlotMember;
import com.modnmetl.virtualrealty.objects.region.Cuboid;
import com.modnmetl.virtualrealty.sql.Database;
import com.modnmetl.virtualrealty.utils.EnumUtils;
import com.modnmetl.virtualrealty.utils.data.OldSchematicUtil;
import com.modnmetl.virtualrealty.utils.data.SchematicUtil;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.math.BlockVector3;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

@Getter
@Setter
public class Plot {

    public static final LocalDateTime MAX_DATE = LocalDateTime.of(2999, 12, 31, 0, 0);
    public static final DateTimeFormatter PLOT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter SHORT_PLOT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private int ID;
    private UUID ownedBy;
    public final LinkedList<PlotMember> members;
    public final Set<RegionPermission> nonMemberPermissions;
    private String assignedBy;
    private LocalDateTime ownedUntilDate;
    private PlotSize plotSize;
    private final int length, width, height;
    private Material floorMaterial, borderMaterial;
    private byte floorData, borderData;
    private final Location createdLocation;
    private Direction createdDirection;
    private BlockVector3 bottomLeftCorner, topRightCorner, borderBottomLeftCorner, borderTopRightCorner;
    private GameMode selectedGameMode;
    private final String createdWorld;
    private Instant modified;
    private LocalDateTime createdAt;

    public Plot(Location location, Material floorMaterial, Material borderMaterial, PlotSize plotSize, int length, int width, int height, boolean natural) {
        this.ID =  PlotManager.getPlots().isEmpty() ? 10000 : PlotManager.getPlotMaxID() + 1;
        this.ownedBy = null;
        this.members = new LinkedList<>();
        this.nonMemberPermissions = new HashSet<>(VirtualRealty.getPermissions().getDefaultNonMemberPlotPerms());
        this.assignedBy = null;
        this.ownedUntilDate = MAX_DATE;
        if (natural) {
            this.floorMaterial = Material.AIR;
            this.borderMaterial = Material.AIR;
        } else {
            this.floorMaterial = floorMaterial;
            this.borderMaterial = borderMaterial;
        }
        this.floorData = 0;
        this.borderData = 0;
        this.createdLocation = location;
        this.createdDirection = Direction.byYaw(location.getYaw());
        this.selectedGameMode = VirtualRealty.getPluginConfiguration().getDefaultPlotGamemode();
        this.createdWorld = Objects.requireNonNull(location.getWorld()).getName();
        this.modified = Instant.now();
        this.createdAt = LocalDateTime.now();
        this.plotSize = plotSize;
        this.length = length;
        this.width = width;
        this.height = height;
        initialize(natural);
        if (VirtualRealty.getDynmapManager() != null && VirtualRealty.getDynmapManager().markerset != null) {
            DynmapManager.resetPlotMarker(this);
        }
    }

    @SneakyThrows
    public Plot(ResultSet rs) {
        this.ID = rs.getInt("ID");
        this.ownedBy = rs.getString("ownedBy").isEmpty() ? null : UUID.fromString(rs.getString("ownedBy"));
        this.members = new LinkedList<>();
        Set<RegionPermission> plotPermissions = new HashSet<>();
        if (!rs.getString("nonMemberPermissions").isEmpty()) {
            for (String s : rs.getString("nonMemberPermissions").split("¦")) {
                plotPermissions.add(RegionPermission.valueOf(s.toUpperCase()));
            }
        }
        this.nonMemberPermissions = plotPermissions;
        this.assignedBy = rs.getString("assignedBy").equalsIgnoreCase("null") ? null : rs.getString("assignedBy");
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral(' ').append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter();
        if (VirtualRealty.getPluginConfiguration().dataModel == PluginConfiguration.DataModel.SQLITE) {
            this.ownedUntilDate = LocalDateTime.parse(rs.getString("ownedUntilDate"), dateTimeFormatter);
            if (rs.getString("created") != null)
                this.createdAt = LocalDateTime.parse(rs.getString("created"), dateTimeFormatter);
        } else {
            this.ownedUntilDate = rs.getTimestamp("ownedUntilDate").toLocalDateTime();
            if (rs.getTimestamp("created") != null)
                this.createdAt = rs.getTimestamp("created").toLocalDateTime();
        }
        this.floorMaterial = Material.getMaterial(rs.getString("floorMaterial").split(":")[0]);
        this.floorData = rs.getString("floorMaterial").split(":").length == 1 ? 0 : Byte.parseByte(rs.getString("floorMaterial").split(":")[1]);
        if (rs.getString("borderMaterial") != null) {
            this.borderMaterial = Material.getMaterial(rs.getString("borderMaterial").split(":")[0]);
            this.borderData = rs.getString("borderMaterial").split(":").length == 1 ? 0 : Byte.parseByte(rs.getString("borderMaterial").split(":")[1]);
        }
        this.plotSize = PlotSize.valueOf(rs.getString("plotSize"));
        this.length = rs.getInt("length");
        this.width = rs.getInt("width");
        this.height = rs.getInt("height");
        ArrayList<String> location = new ArrayList<>(Arrays.asList(rs.getString("createdLocation").subSequence(0, rs.getString("createdLocation").length() - 1).toString().split(";")));
        Location createLocation = new Location(Bukkit.getWorld(location.get(0)), Double.parseDouble(location.get(1)), Double.parseDouble(location.get(2)), Double.parseDouble(location.get(3)), Float.parseFloat(location.get(4)), Float.parseFloat(location.get(5)));
        this.createdLocation = rs.getString("createdLocation").isEmpty() ? null : createLocation;
        if (this.createdLocation != null) {
            this.createdDirection = Direction.byYaw(createdLocation.getYaw());
        }
        if (!rs.getString("selectedGameMode").isEmpty() && EnumUtils.isValidEnum(GameMode.class, rs.getString("selectedGameMode"))) {
            this.selectedGameMode = GameMode.valueOf(rs.getString("selectedGameMode"));
        } else {
            this.selectedGameMode = VirtualRealty.getPluginConfiguration().getDefaultPlotGamemode();
        }
        this.createdWorld = location.get(0);
        if (floorMaterial == null) {
            floorMaterial = plotSize.getFloorMaterial();
            floorData = plotSize.getFloorData();
        }
        if (borderMaterial == null) {
            borderMaterial = plotSize.getBorderMaterial();
            borderData = plotSize.getBorderData();
        }
        prepareCorners();
    }

    public String getFloorMaterialName() {
        if (this.floorMaterial == Material.AIR) return "NONE";
        return this.floorMaterial.name();
    }

    public String getBorderMaterialName() {
        if (this.borderMaterial == Material.AIR) return "NONE";
        return this.borderMaterial.name();
    }

    public void teleportPlayer(Player player) {
        World world = Bukkit.getWorld(createdWorld);
        if (world == null) return;
        Location location = new Location(world, getCenter().getBlockX(), getCenter().getBlockY() + 1, getCenter().getBlockZ());
        if (!world.getName().endsWith("_nether")) {
            location.setY(Objects.requireNonNull(location.getWorld()).getHighestBlockAt(location.getBlockX(), location.getBlockZ()).getY() + 1);
        }
        player.teleport(location);
    }

    public boolean hasMembershipAccess(UUID uuid) {
        PlotMember member = getMember(uuid);
        return member != null || (ownedBy != null && getPlotOwner().getUniqueId() == uuid);
    }

    public void togglePermission(RegionPermission plotPermission) {
        modified();
        if (nonMemberPermissions.contains(plotPermission)) {
            nonMemberPermissions.remove(plotPermission);
        } else {
            nonMemberPermissions.add(plotPermission);
        }
    }

    public boolean hasPermission(RegionPermission plotPermission) {
        return nonMemberPermissions.contains(plotPermission);
    }

    public void addPermission(RegionPermission plotPermission) {
        nonMemberPermissions.add(plotPermission);
    }

    public void removePermission(RegionPermission plotPermission) {
        nonMemberPermissions.remove(plotPermission);
    }

    public PlotMember getMember(UUID uuid) {
        for (PlotMember member : members) {
            if (member.getUuid().equals(uuid)) return member;
        }
        return null;
    }

    public void addMember(UUID uuid) {
        PlotMember plotMember = new PlotMember(uuid, this);
        members.add(plotMember);
        plotMember.insert();
    }

    public void removeMember(PlotMember plotMember) {
        members.remove(plotMember);
        plotMember.delete();
    }

    public boolean isOwnershipExpired() {
        return ownedUntilDate.isBefore(LocalDateTime.now());
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

    public void setOwnedBy(UUID ownedBy) {
        modified();
        this.ownedBy = ownedBy;
        PlotMember plotMember = getMember(ownedBy);
        if (plotMember != null) {
            removeMember(plotMember);
        }
        updateMarker();
    }

    public void setOwnedUntilDate(LocalDateTime ownedUntilDate) {
        modified();
        this.ownedUntilDate = ownedUntilDate;
        updateMarker();
    }

    public void setFloorMaterial(Material floorMaterial, byte data) {
        modified();
        this.floorMaterial = floorMaterial;
        this.floorData = data;
        initializeFloor();
    }

    private void initializeFloor() {
        for (Block floorBlock : getFloorBlocks()) {
            floorBlock.setType(this.floorMaterial);
            if (VirtualRealty.legacyVersion) {
                try {
                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                    m2.setAccessible(true);
                    m2.invoke(floorBlock, this.floorData);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setBorderMaterial(Material borderMaterial, byte data) {
        modified();
        this.borderMaterial = borderMaterial;
        this. borderData = data;
        for (Block borderBlock : getBorderBlocks()) {
            if (VirtualRealty.legacyVersion) {
                borderBlock.setType(borderMaterial);
                try {
                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                    m2.setAccessible(true);
                    m2.invoke(borderBlock, data);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                borderBlock.setType(borderMaterial);
            }
        }
    }

    public BlockVector3 getCenter() {
        return new Cuboid(bottomLeftCorner, topRightCorner, getCreatedWorld()).getCenterVector();
    }

    public Cuboid getCuboid() {
        return new Cuboid(bottomLeftCorner, topRightCorner, getCreatedWorld());
    }

    public org.bukkit.World getCreatedWorld() {
        return Bukkit.getWorld(createdWorld);
    }

    public String getCreatedWorldString() {
        return createdWorld;
    }

    public OfflinePlayer getPlotOwner() {
        return ownedBy == null ? null : Bukkit.getOfflinePlayer(ownedBy);
    }

    public Set<OfflinePlayer> getPlayerMembers() {
        Set<OfflinePlayer> offlinePlayers = new HashSet<>();
        for (PlotMember member : members) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
            offlinePlayers.add(offlinePlayer);
        }
        return offlinePlayers;
    }

    public void prepareCorners() {
        Location location = createdLocation;
        Direction direction = Direction.byYaw(location.getYaw());
        Location location1;
        Location location2;
        Location border1;
        Location border2;
        switch(direction) {
            case SOUTH: {
                location1 = new Location(getCreatedWorld(), location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(getCreatedWorld(), location.getBlockX() - width + 1, location.getBlockY() + height, location.getBlockZ() + length - 1);
                border1 = new Location(getCreatedWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                border2 = new Location(getCreatedWorld(), location.getBlockX() - width, location.getBlockY() + height, location.getBlockZ() + length);
                break;
            }
            case WEST: {
                location1 = new Location(getCreatedWorld(), location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(getCreatedWorld(), location.getBlockX() - length + 1, location.getBlockY() + height, location.getBlockZ() - width + 1);
                border1 = new Location(getCreatedWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                border2 = new Location(getCreatedWorld(), location.getBlockX() - length, location.getBlockY() + height, location.getBlockZ() - width);
                break;
            }
            case NORTH: {
                location1 = new Location(getCreatedWorld(), location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(getCreatedWorld(), location.getBlockX() + width - 1, location.getBlockY() + height, location.getBlockZ() - length + 1);
                border1 = new Location(getCreatedWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                border2 = new Location(getCreatedWorld(), location.getBlockX() + width, location.getBlockY() + height, location.getBlockZ() - length);
                break;
            }
            case EAST: {
                location1 = new Location(getCreatedWorld(), location.getBlockX() + length - 1, location.getBlockY() - 10, location.getBlockZ());
                location2 = new Location(getCreatedWorld(), location.getBlockX(), location.getBlockY() + height, location.getBlockZ() + width - 1);
                border1 = new Location(getCreatedWorld(), location.getBlockX() + length, location.getBlockY() - 10, location.getBlockZ() - 1);
                border2 = new Location(getCreatedWorld(), location.getBlockX() - 1, location.getBlockY() + height, location.getBlockZ() + width);
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

    public void initialize(boolean natural) {
        long time = System.currentTimeMillis();
        prepareCorners();
        if (plotSize != PlotSize.AREA) prepareBlocks(createdLocation, natural);
        VirtualRealty.debug("Plot initialize time: " + (System.currentTimeMillis() - time) + " ms");
    }

    public Set<Block> getBorderBlocks() {
        Set<Block> blocks = new HashSet<>();
        Location location = this.getCreatedLocation();
        Direction direction = Direction.byYaw(location.getYaw());
        int maxX;
        int maxZ;
        int minX;
        int minZ;
        switch(direction) {
            case SOUTH: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + length + 1;
                minX = location.getBlockX() - width + 1;
                minZ = location.getBlockZ() - 1;
                break;
            }
            case WEST: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX() - length + 1;
                minZ = location.getBlockZ() - width;
                break;
            }
            case NORTH: {
                maxX = location.getBlockX() + width + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - length;
                break;
            }
            case EAST: {
                maxX = location.getBlockX() + length + 1;
                maxZ = location.getBlockZ() + width + 1;
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
                    blocks.add(Objects.requireNonNull(getCreatedWorld()).getBlockAt(x, location.getBlockY() + 1, z));
                }
            }
        }
        return blocks;
    }

    public Set<Block> getFloorBlocks() {
        Set<Block> blocks = new HashSet<>();
        Location location = this.getCreatedLocation();
        Direction direction = Direction.byYaw(location.getYaw());
        int maxX;
        int maxZ;
        int minX;
        int minZ;
        switch(direction) {
            case SOUTH: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + length + 1;
                minX = location.getBlockX() - width + 1;
                minZ = location.getBlockZ() - 1;
                break;
            }
            case WEST: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX() - length + 1;
                minZ = location.getBlockZ() - width;
                break;
            }
            case NORTH: {
                maxX = location.getBlockX() + width + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - length;
                break;
            }
            case EAST: {
                maxX = location.getBlockX() + length + 1;
                maxZ = location.getBlockZ() + width + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - 1;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
        for (int x = minX - 1; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                blocks.add(getCreatedWorld().getBlockAt(x, location.getBlockY(), z));
            }
        }
        return blocks;
    }

    public void prepareBlocks(Location location, boolean natural) {
        Direction direction = Direction.byYaw(location.getYaw());
        Location location1;
        Location location2;
        switch (direction) {
            case SOUTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - width, location.getBlockY() + height, location.getBlockZ() + length);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                break;
            }
            case WEST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - length, location.getBlockY() + height, location.getBlockZ() - width);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                break;
            }
            case NORTH: {
                location1 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() - 10, location.getBlockZ() + 1);
                location2 = new Location(location.getWorld(), location.getBlockX() + width, location.getBlockY() + height, location.getBlockZ() - length);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                break;
            }
            case EAST: {
                location1 = new Location(location.getWorld(), location.getBlockX() + length, location.getBlockY() - 10, location.getBlockZ() - 1);
                location2 = new Location(location.getWorld(), location.getBlockX() - 1, location.getBlockY() + height, location.getBlockZ() + width);
                SchematicUtil.save(ID, SchematicUtil.getStructure(location1.getBlock(), location2.getBlock()));
                break;
            }
        }
        if (natural) return;
        for (Block floorBlock : getFloorBlocks()) {
            for (int y = location.getBlockY() + height; y > location.getBlockY() - 1; y--) {
                Block airBlock = Objects.requireNonNull(location.getWorld()).getBlockAt(floorBlock.getX(), y, floorBlock.getZ());
                airBlock.setType(Material.AIR, false);
            }
            floorBlock.setType(floorMaterial);
            if (VirtualRealty.legacyVersion) {
                try {
                    Method m2 = Block.class.getDeclaredMethod("setData", byte.class);
                    m2.setAccessible(true);
                    m2.invoke(floorBlock, plotSize.getFloorData());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        int maxX = 0;
        int maxZ = 0;
        int minX = 0;
        int minZ = 0;
        switch(direction) {
            case SOUTH: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + length + 1;
                minX = location.getBlockX() - width + 1;
                minZ = location.getBlockZ() - 1;
                break;
            }
            case WEST: {
                maxX = location.getBlockX() + 1 + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX() - length + 1;
                minZ = location.getBlockZ() - width;
                break;
            }
            case NORTH: {
                maxX = location.getBlockX() + width + 1;
                maxZ = location.getBlockZ() + 1 + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - length;
                break;
            }
            case EAST: {
                maxX = location.getBlockX() + length + 1;
                maxZ = location.getBlockZ() + width + 1;
                minX = location.getBlockX();
                minZ = location.getBlockZ() - 1;
                break;
            }
        }
        for (int x = minX - 1; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                if (x == minX - 1 || z == minZ || x == maxX - 1 || z == maxZ - 1) {
                    Block borderBlock = Objects.requireNonNull(location.getWorld()).getBlockAt(x, location.getBlockY() + 1, z);
                    if (VirtualRealty.legacyVersion) {
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
    }

    public void unloadPlot() {
        if (SchematicUtil.isOldSerialization(ID)) {
            long time = System.currentTimeMillis();
            Location location = null;
            switch (createdDirection) {
                case SOUTH: {
                    location = new Location(getCreatedWorld(), createdLocation.getBlockX() - width, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1);
                    break;
                }
                case WEST: {
                    location = new Location(getCreatedWorld(), createdLocation.getBlockX() - length, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - width);
                    break;
                }
                case NORTH: {
                    location = new Location(getCreatedWorld(), createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - length);
                    break;
                }
                case EAST: {
                    location = new Location(getCreatedWorld(), createdLocation.getBlockX() - 1, createdLocation.getBlockY() - 10, createdLocation.getBlockZ() - 1);
                    break;
                }
            }
            OldSchematicUtil.paste(ID, location);
            VirtualRealty.debug("Region pasted in: " + (System.currentTimeMillis() - time) + " ms (Old Serialization)");
        } else {
            long time = System.currentTimeMillis();
            SchematicUtil.paste(ID);
            VirtualRealty.debug("Region pasted in: " + (System.currentTimeMillis() - time) + " ms");
        }
    }

    private void modified() {
        modified = Instant.now();
    }

    @SneakyThrows
    public void insert() {
        String serializedLocation =
                        Objects.requireNonNull(this.getCreatedWorld()).getName() + ";" +
                        this.createdLocation.getX() + ";" +
                        this.createdLocation.getY() + ";" +
                        this.createdLocation.getZ() + ";" +
                        this.createdLocation.getYaw() + ";" +
                        this.createdLocation.getPitch() + ";";
        StringBuilder permissions = new StringBuilder();
        for (RegionPermission permission : this.nonMemberPermissions) {
            permissions.append(permission.name()).append("¦");
        }
        Database.getInstance().getStatement().execute("INSERT INTO `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName +
                "` (`ID`, `ownedBy`, `nonMemberPermissions`, `assignedBy`, `ownedUntilDate`, `floorMaterial`, `borderMaterial`, `plotSize`, `length`, `width`, `height`, `createdLocation`, `created`, `modified`, `selectedGameMode`) " +
                "VALUES ('" + this.ID + "', '" + (this.ownedBy == null ? "" : this.ownedBy.toString()) + "', '" + permissions + "', '" + this.assignedBy + "', '" + Timestamp.valueOf(this.ownedUntilDate) + "', '" +
                this.floorMaterial + ":" + this.floorData + "', '" + this.borderMaterial + ":" + this.borderData + "', '" + this.plotSize + "', '" + this.length + "', '" + this.width + "', '" +
                this.height + "', '" + serializedLocation + "', '" + Timestamp.from(Instant.now()) + "', '" + Timestamp.from(Instant.now()) + "', '" + this.selectedGameMode.name()
                + "')");
    }

    @SneakyThrows
    public void update() {
        StringBuilder permissions = new StringBuilder();
        for (RegionPermission permission : this.nonMemberPermissions) {
            permissions.append(permission.name()).append("¦");
        }
        Database.getInstance().getStatement().execute("UPDATE `" +
                VirtualRealty.getPluginConfiguration().mysql.plotsTableName +
                "` SET `ownedBy`='" + (this.ownedBy == null ? "" : this.ownedBy.toString()) + "'," +
                " `nonMemberPermissions`='" + permissions + "'," +
                " `assignedBy`='" + this.assignedBy + "'," +
                " `ownedUntilDate`='" + Timestamp.valueOf(this.ownedUntilDate) + "'," +
                " `floorMaterial`='" + this.floorMaterial + ":" + this.floorData + "'," +
                " `borderMaterial`='" + this.borderMaterial + ":" + this.borderData + "'," +
                " `plotSize`='" + this.plotSize + "'," +
                " `length`='" + this.length + "'," +
                " `width`='" + this.width + "'," +
                " `height`='" + this.height + "'," +
                " `modified`='" + (this.modified != null ? Timestamp.from(this.modified) : Timestamp.from(Instant.now())) + "'," +
                " `selectedGameMode`='" + this.selectedGameMode.name() + "'" +
                " WHERE `ID`='" + this.ID + "'");
    }

    public void remove(CommandSender sender) {
        if (plotSize != PlotSize.AREA) {
            if (SchematicUtil.doesPlotFileExist(ID)) {
                this.unloadPlot();
                SchematicUtil.deletePlotFile(ID);
            } else {
                sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().noRegionFileFound);
            }
        }
        for (PlotMember member : this.getMembers()) {
            removeMember(member);
        }
        if (VirtualRealty.getDynmapManager() != null) {
            DynmapManager.removeDynMapMarker(this);
        }
        try {
            Database.getInstance().getStatement().execute("DELETE FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` WHERE `ID` = '" + ID + "';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PlotManager.removePlotFromList(this);
        VirtualRealty.debug("Removed plot #" + this.ID);
    }

    public Direction getCreatedDirection() {
        return createdDirection;
    }

    public void updateMarker() {
        DynmapManager.resetPlotMarker(this);
        VirtualRealty.debug("Updated marker #" + this.ID);
    }

    @Override
    public String toString() {
        return "{ ID: " + ID + ", ownedBy: " + ownedBy + "}";
    }

}