package com.modnmetl.virtualrealty.utils;

import java.util.UUID;

public class UUIDUtils {

    public static boolean isValidUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
