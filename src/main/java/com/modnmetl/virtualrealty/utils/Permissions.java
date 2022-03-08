package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.VirtualRealty;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class Permissions {

    public static boolean hasPermission(CommandSender sender, Permission basePermission, String permission) {
        Permission finalPermission = new Permission(basePermission.getName() + "." + permission);
        if (!sender.hasPermission(finalPermission)) {
            sender.sendMessage(VirtualRealty.PREFIX + "§cInsufficient permissions! §8(§7" + finalPermission.getName() + "§8)");
            return false;
        }
        return true;
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        Permission finalPermission = new Permission(permission);
        if (!sender.hasPermission(finalPermission)) {
            sender.sendMessage(VirtualRealty.PREFIX + "§cInsufficient permissions! §8(§7" + finalPermission.getName() + "§8)");
            return false;
        }
        return true;
    }

}
