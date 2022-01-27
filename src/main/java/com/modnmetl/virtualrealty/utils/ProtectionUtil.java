package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.enums.Permission;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.Flag;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class ProtectionUtil {

    public static boolean canBuild(Player player, Location location) {
        Plot plot = PlotManager.getBorderedPlot(location);
        if (plot != null) {
            if (plot.getOwnedBy() != null) {
                if (!plot.getOwnedBy().equals(player.getUniqueId())) {
                    return false;
                } else {
                    if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermissionToWorld(Player player, Flag.World flag) {
        if (player.hasPermission(Permission.WORLD_BUILD.getPermission())) return true;
        if (player.hasPermission(VirtualRealty.GLOBAL_PERMISSION.getName() + ".world." + flag.name().toLowerCase())) return true;
        if (player.isOp()) return true;
        return false;
    }

}
