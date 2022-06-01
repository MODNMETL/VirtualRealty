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
        sender.spigot().sendMessage(text);
    }


}
