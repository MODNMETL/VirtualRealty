package com.modnmetl.virtualrealty.objects.data;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.permissions.ManagementPermission;
import com.modnmetl.virtualrealty.enums.permissions.RegionPermission;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.sql.Database;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.GameMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class PlotMember {

    private final UUID uuid;
    private final Plot plot;
    private GameMode selectedGameMode;
    private final Set<RegionPermission> permissions;
    private final Set<ManagementPermission> managementPermissions;

    public PlotMember(UUID uuid, Plot plot) {
        this.uuid = uuid;
        this.plot = plot;
        this.selectedGameMode = plot.getSelectedGameMode();
        this.permissions = new HashSet<>(VirtualRealty.getPermissions().getDefaultMemberPerms());
        this.managementPermissions = new HashSet<>();
    }

    @SneakyThrows
    public PlotMember(ResultSet rs) {
        this.uuid = UUID.fromString(rs.getString("uuid"));
        Plot plot = PlotManager.getPlot(rs.getInt("plot"));
        this.plot = plot;
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
        if (plot != null)
            plot.members.add(this);
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
            ps.setInt(2, this.plot.getID());
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
            ps.setInt(5, this.plot.getID());
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
            ps.setInt(2, plot.getID());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
