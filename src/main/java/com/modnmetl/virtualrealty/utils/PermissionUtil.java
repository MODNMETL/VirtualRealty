package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.VirtualRealty;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class PermissionUtil {

    public static boolean hasPermission(CommandSender sender, String permission) {
        Permission finalPermission = new Permission(permission);
        if (!sender.hasPermission(finalPermission)) {
            sender.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().insufficientPermissions.replaceAll("%permission%", finalPermission.getName()));
            return false;
        }
        return true;
    }

}
