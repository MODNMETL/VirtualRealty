package com.modnmetl.virtualrealty.listeners.stake;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.plot.subcommand.StakeSubCommand;
import com.modnmetl.virtualrealty.commands.vrplot.subcommand.RemoveSubCommand;
import com.modnmetl.virtualrealty.enums.ConfirmationType;
import com.modnmetl.virtualrealty.enums.PlotSize;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.managers.ConfirmationManager;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.data.Confirmation;
import com.modnmetl.virtualrealty.objects.data.PlotItem;
import com.modnmetl.virtualrealty.objects.region.GridStructure;
import com.modnmetl.virtualrealty.utils.multiversion.Chat;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.persistence.Version;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.AbstractMap;

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
                        if (confirmation.getProceedText().equalsIgnoreCase(e.getMessage())) {
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
