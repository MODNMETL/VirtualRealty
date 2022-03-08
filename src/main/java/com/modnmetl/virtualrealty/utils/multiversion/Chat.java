package com.modnmetl.virtualrealty.utils.multiversion;

import com.modnmetl.virtualrealty.VirtualRealty;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Chat {

    private final BaseComponent text;

    public Chat(BaseComponent text) {
        this.text = text;
    }

    public Chat(String text) {
        this.text = new TextComponent(text);
    }

    public void sendTo(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (VirtualRealty.legacyVersion) {
                try {
                    Method m = Player.class.getDeclaredMethod("sendMessage", BaseComponent.class);
                    m.setAccessible(true);
                    m.invoke(player, text);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                player.spigot().sendMessage(text);
            }
        } else {
            sender.sendMessage(text.toLegacyText());
        }
    }


}
