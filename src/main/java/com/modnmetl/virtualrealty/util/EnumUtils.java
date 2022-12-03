package com.modnmetl.virtualrealty.util;

public final class EnumUtils {

    public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
        if (enumName == null) {
            return false;
        } else {
            try {
                Enum.valueOf(enumClass, enumName);
                return true;
            } catch (IllegalArgumentException var3) {
                return false;
            }
        }
    }

}
