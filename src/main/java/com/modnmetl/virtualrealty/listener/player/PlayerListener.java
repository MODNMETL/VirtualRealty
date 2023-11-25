package com.modnmetl.virtualrealty.listener.player;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.listener.VirtualListener;
import com.modnmetl.virtualrealty.manager.PlotManager;
import com.modnmetl.virtualrealty.model.other.ChatMessage;
import com.modnmetl.virtualrealty.model.plot.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PlayerListener extends VirtualListener {

    public PlayerListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlotManager plotManager = PlotManager.getInstance();
        List<Plot> playerPlots = plotManager.getPlayerPlots(player.getUniqueId());
        LocalDateTime now = LocalDateTime.now();
        for (Plot playerPlot : playerPlots) {
            LocalDateTime expirationDate = playerPlot.getOwnedUntilDate();
            long daysUntilExpiration = ChronoUnit.DAYS.between(now, expirationDate);
            Duration durationUntilExpiration = Duration.between(now, expirationDate);
            if (durationUntilExpiration.isNegative()) {
                String message = VirtualRealty.getMessages().ownershipExpiredJoinMessage
                        .replaceAll("%plot_size%", playerPlot.getPlotSize().name().toLowerCase())
                        .replaceAll("%plot_id%", String.valueOf(playerPlot.getID()));
                ChatMessage.of(message).sendWithPrefix(player);
            } else if (daysUntilExpiration <= VirtualRealty.getPluginConfiguration().daysUntilExpirationThreshold) {
                long hoursUntilExpiration = durationUntilExpiration.toHours() % 24;
                String message = VirtualRealty.getMessages().daysUntilExpirationThresholdMessage
                        .replaceAll("%plot_size%", playerPlot.getPlotSize().name().toLowerCase())
                        .replaceAll("%plot_id%", String.valueOf(playerPlot.getID()))
                        .replaceAll("%days%", String.valueOf(daysUntilExpiration))
                        .replaceAll("%hours%", String.valueOf(hoursUntilExpiration));
                ChatMessage.of(message).sendWithPrefix(player);
            }
        }
    }

}