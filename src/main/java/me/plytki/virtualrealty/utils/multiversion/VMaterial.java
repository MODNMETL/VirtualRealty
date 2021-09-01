package me.plytki.virtualrealty.utils.multiversion;

import me.plytki.virtualrealty.VirtualRealty;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VMaterial {

    public static Material getMaterial(int materialID) {
        if (VirtualRealty.isLegacy) {
            try {
                Method m = Material.class.getDeclaredMethod("getMaterial", int.class);
                m.setAccessible(true);
                return (Material) m.invoke(Material.class, materialID);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            int counter = 1;
            for (Material material : Material.values()) {
                if (counter == materialID) {
                    return material;
                }
                counter++;
            }
        }
        return null;
    }

    public static Material getMaterial(String material) {
        try {
            Method m = Material.class.getDeclaredMethod("getMaterial", String.class);
            m.setAccessible(true);
            return (Material) m.invoke(Material.class, material);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
