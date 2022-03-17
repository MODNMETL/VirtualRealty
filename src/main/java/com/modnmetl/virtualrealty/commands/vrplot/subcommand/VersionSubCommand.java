package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class VersionSubCommand extends SubCommand {

    public VersionSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        TextComponent textComponent = new TextComponent();
        if (!VirtualRealty.upToDate) {
            textComponent = new TextComponent(" §7(Newer version available)");
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§a§oClick here to download new version!")}));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/virtual-realty.95599/"));
        }
        sender.spigot().sendMessage(new TextComponent(VirtualRealty.PREFIX + "Version: §a" + VirtualRealty.getInstance().getDescription().getVersion()), textComponent);
    }

}
