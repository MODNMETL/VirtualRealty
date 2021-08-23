package me.plytki.virtualrealty.utils;

import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
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

}
