package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.permissions.RegionPermission;

import java.util.List;

public class WorldUtil {

    public static List<RegionPermission> getWorldPermissions() {
        return VirtualRealty.getPermissions().getWorldProtection();
    }

    public static void togglePermission(RegionPermission plotPermission) {
        if (getWorldPermissions().contains(plotPermission)) {
            VirtualRealty.getPermissions().worldProtection.remove(plotPermission);
        } else {
            VirtualRealty.getPermissions().worldProtection.add(plotPermission);
        }
    }

    public static boolean hasPermission(RegionPermission plotPermission) {
        return getWorldPermissions().contains(plotPermission);
    }

    public static void addPermission(RegionPermission plotPermission) {
        VirtualRealty.getPermissions().worldProtection.add(plotPermission);
    }

    public static void removePermission(RegionPermission plotPermission) {
        VirtualRealty.getPermissions().worldProtection.remove(plotPermission);
    }

}
