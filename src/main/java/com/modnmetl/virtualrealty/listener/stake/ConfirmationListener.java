package com.modnmetl.virtualrealty.listener.stake;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.listener.VirtualListener;
import com.modnmetl.virtualrealty.manager.ConfirmationManager;
import com.modnmetl.virtualrealty.model.other.Confirmation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfirmationListener extends VirtualListener {

    public ConfirmationListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        for (Confirmation confirmation : ConfirmationManager.getConfirmations()) {
            if (player.getUniqueId() == confirmation.getSender().getUniqueId()) {
                e.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (e.getMessage().matches(".*(?i)" + confirmation.getProceedText() +".*")) {
                            confirmation.success();
                        } else {
                            confirmation.failed();
                        }
                    }
                }.runTask(VirtualRealty.getInstance());
                break;
            }
        }
    }

}
