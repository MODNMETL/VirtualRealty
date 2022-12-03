package com.modnmetl.virtualrealty.util;

import java.util.UUID;

public final class UUIDUtils {

    public static boolean isValidUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
