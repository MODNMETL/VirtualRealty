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

import java.sql.ResultSet;
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
        Database.getInstance().getStatement().execute("INSERT INTO `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName +
                "` (`uuid`, `plot`, `selectedGameMode`, `permissions`, `managementPermissions`) " +
                "VALUES ('" + this.uuid.toString() + "', '" + this.plot.getID() + "', '" + this.getSelectedGameMode().name() + "', '" + permissions + "', '" + managementPermissions
                + "')");
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
        Database.getInstance().getStatement().execute("UPDATE `" +
                VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName +
                "` SET `permissions`='" + permissions + "'," +
                "`managementPermissions`='" + managementPermissions + "'," +
                "`selectedGameMode`='" + selectedGameMode.name() + "'" +
                " WHERE `uuid`='" + this.uuid.toString() + "' AND `plot`='" + this.plot.getID() + "'");
    }

    @SneakyThrows
    public void delete() {
        Database.getInstance().getStatement().execute("DELETE FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "` WHERE `uuid` = '" + this.uuid + "' AND `plot`=" + plot.getID() + ";");
    }

}
