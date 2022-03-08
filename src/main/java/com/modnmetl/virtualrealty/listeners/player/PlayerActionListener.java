package com.modnmetl.virtualrealty.listeners.player;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerActionListener extends VirtualListener {

    public PlayerActionListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!player.isOp()) return;
        if (VirtualRealty.upToDate) return;
        player.sendMessage(VirtualRealty.PREFIX + "§7A new version of VirtualRealty plugin is available. §a[" + VirtualRealty.latestVersion + "]");
        TextComponent textComponent = new TextComponent(VirtualRealty.PREFIX + "§aDownload the new version of the plugin here!");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§f§oClick here to download the update!")}));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/virtual-realty.95599/"));
        player.spigot().sendMessage(textComponent);
    }

}
