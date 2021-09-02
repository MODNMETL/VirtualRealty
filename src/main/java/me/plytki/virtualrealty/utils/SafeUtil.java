package me.plytki.virtualrealty.utils;

public class SafeUtil {

    private static void reportUnsafe(Throwable th) {
        try {
            throw new Exception(th.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T safeInit(SafeInitializer<T> initializer) {
        try {
            return initializer.initialize();
        } catch (Exception e) {
            reportUnsafe(e);
            return null;
        }
    }

    @FunctionalInterface
    public interface SafeInitializer<T> {
        T initialize() throws Exception;
    }

}
