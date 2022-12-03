package com.modnmetl.virtualrealty.model.plot;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.model.permission.ManagementPermission;
import com.modnmetl.virtualrealty.model.permission.RegionPermission;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.sql.Database;
import lombok.Data;
import lombok.SneakyThrows;
import org.bukkit.GameMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class PlotMember {

    private final UUID uuid;

    private final int plotId;
    private GameMode selectedGameMode;
    private final Set<RegionPermission> permissions;
    private final Set<ManagementPermission> managementPermissions;

    public PlotMember(UUID uuid, int plotId) {
        this.uuid = uuid;
        this.plotId = plotId;
        this.selectedGameMode = getPlot().getSelectedGameMode();
        this.permissions = new HashSet<>(VirtualRealty.getPermissions().getDefaultMemberPerms());
        this.managementPermissions = new HashSet<>();
    }

    @SneakyThrows
    public PlotMember(ResultSet rs) {
        this.uuid = UUID.fromString(rs.getString("uuid"));
        this.plotId = rs.getInt("plot");
        this.selectedGameMode = GameMode.valueOf(rs.getString("selectedGameMode"));
        Set<RegionPermission> plotPermissions = new HashSet<>();
        if (!rs.getString("permissions").isEmpty()) {
            for (String s : rs.getString("permissions").split("¦")) {
                plotPermissions.add(RegionPermission.valueOf(s.toUpperCase()));
            }
        }
        this.permissions = plotPermissions;
        Set<ManagementPermission> managementPermissions = new HashSet<>();
        if (!rs.getString("managementPermissions").isEmpty()) {
            for (String s : rs.getString("managementPermissions").split("¦")) {
                managementPermissions.add(ManagementPermission.valueOf(s.toUpperCase()));
            }
        }
        this.managementPermissions = managementPermissions;
        if (getPlot() != null)
            getPlot().members.add(this);
    }

    public Plot getPlot() {
        return PlotManager.getInstance().getPlot(this.plotId);
    }

    public void togglePermission(RegionPermission plotPermission) {
        if (permissions.contains(plotPermission)) {
            permissions.remove(plotPermission);
        } else {
            permissions.add(plotPermission);
        }
    }

    public boolean hasPermission(RegionPermission plotPermission) {
        return permissions.contains(plotPermission);
    }

    public void addPermission(RegionPermission plotPermission) {
        permissions.add(plotPermission);
    }

    public void removePermission(RegionPermission plotPermission) {
        permissions.remove(plotPermission);
    }


    public void toggleManagementPermission(ManagementPermission managementPermission) {
        if (managementPermissions.contains(managementPermission)) {
            managementPermissions.remove(managementPermission);
        } else {
            managementPermissions.add(managementPermission);
        }
    }

    public boolean hasManagementPermission(ManagementPermission managementPermission) {
        return managementPermissions.contains(managementPermission);
    }

    public void addManagementPermission(ManagementPermission managementPermission) {
        managementPermissions.add(managementPermission);
    }

    public void removeManagementPermission(ManagementPermission managementPermission) {
        managementPermissions.remove(managementPermission);
    }


    @SneakyThrows
    public void insert() {
        StringBuilder permissions = new StringBuilder();
        for (RegionPermission permission : this.permissions) {
            permissions.append(permission.name()).append("¦");
        }
        StringBuilder managementPermissions = new StringBuilder();
        for (ManagementPermission permission : this.managementPermissions) {
            managementPermissions.append(permission.name()).append("¦");
        }
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName +
                             "` (`uuid`, `plot`, `selectedGameMode`, `permissions`, `managementPermissions`) " +
                             "VALUES (?, ?, ?, ?, ?)"
             )) {
            ps.setString(1, this.uuid.toString());
            ps.setInt(2, this.plotId);
            ps.setString(3, this.getSelectedGameMode().name());
            ps.setString(4, permissions.toString());
            ps.setString(5, managementPermissions.toString());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void update() {
        StringBuilder permissions = new StringBuilder();
        for (RegionPermission permission : this.permissions) {
            permissions.append(permission.name()).append("¦");
        }
        StringBuilder managementPermissions = new StringBuilder();
        for (ManagementPermission permission : this.managementPermissions) {
            managementPermissions.append(permission.name()).append("¦");
        }
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE `" +
                             VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName +
                             "` SET `permissions`= ?," +
                             "`managementPermissions`= ?," +
                             "`selectedGameMode`= ?" +
                             " WHERE `uuid`= ? AND `plot`= ?"
             )) {
            ps.setString(1, permissions.toString());
            ps.setString(2, managementPermissions.toString());
            ps.setString(3, selectedGameMode.name());
            ps.setString(4, this.uuid.toString());
            ps.setInt(5, this.plotId);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void delete() {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "`" +
                             " WHERE `uuid` = ? AND `plot` = ?"
             )) {
            ps.setString(1, this.uuid.toString());
            ps.setInt(2, this.plotId);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
