package com.modnmetl.virtualrealty.util;

import java.util.Map;

public class MapUtils {

    public static <K, V> K getKeyByValue(Map<K, V> map, V targetValue) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(targetValue)) {
                return entry.getKey();
            }
        }
        return null; // Key not found for the given value
    }

}