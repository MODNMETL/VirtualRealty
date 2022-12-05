package com.modnmetl.virtualrealty.util;

import com.modnmetl.virtualrealty.model.other.ChatMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public final class PermissionsUtil {

    public static boolean hasPermission(CommandSender sender, Permission basePermission, String permission) {
        Permission finalPermission = new Permission(basePermission.getName() + "." + permission);
        if (!sender.hasPermission(finalPermission)) {
            ChatMessage.of("§cInsufficient permissions! §8(§7" + finalPermission.getName() + "§8)").sendWithPrefix(sender);
            return false;
        }
        return true;
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        Permission finalPermission = new Permission(permission);
        if (!sender.hasPermission(finalPermission)) {
            ChatMessage.of("§cInsufficient permissions! §8(§7" + finalPermission.getName() + "§8)").sendWithPrefix(sender);
            return false;
        }
        return true;
    }

}
