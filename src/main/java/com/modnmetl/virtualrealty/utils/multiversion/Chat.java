package com.modnmetl.virtualrealty.utils.multiversion;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

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
