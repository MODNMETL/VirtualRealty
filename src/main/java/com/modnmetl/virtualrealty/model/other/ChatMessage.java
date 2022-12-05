package com.modnmetl.virtualrealty.model.other;

import com.modnmetl.virtualrealty.VirtualRealty;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class ChatMessage {

    private final BaseComponent message;

    private ChatMessage(BaseComponent message) {
        this.message = message;
    }

    private ChatMessage(String message) {
        this.message = new TextComponent(message);
    }

    public static ChatMessage of(String message) {
        return new ChatMessage(message);
    }

    public static ChatMessage of(BaseComponent... messages) {
        return new ChatMessage(new TextComponent(messages));
    }

    public static ChatMessage of(BaseComponent message) {
        return new ChatMessage(message);
    }

    public void sendWithPrefix(CommandSender player) {
        player.spigot().sendMessage(new TextComponent(VirtualRealty.PREFIX), message);
    }

    public void send(CommandSender player) {
        player.spigot().sendMessage(message);
    }

}
