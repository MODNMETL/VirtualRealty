package me.plytki.virtualrealty.utils.multiversion;

import me.plytki.virtualrealty.VirtualRealty;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Chat {

    private BaseComponent text;

    public Chat(BaseComponent text) {
        this.text = text;
    }

    public Chat(BaseComponent... text) {
        this.text = new TextComponent();
        for (BaseComponent baseComponent : text) {
            this.text.addExtra(baseComponent);
        }
    }

    public Chat(String text) {
        this.text = new TextComponent(text);
    }

    public void sendTo(Player player) {
        if (VirtualRealty.isLegacy) {
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
    }


}
