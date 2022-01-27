package com.modnmetl.virtualrealty.utils.multiversion;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.exceptions.MaterialMatchException;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VMaterial {

    public static Material getMaterial(int materialID) throws MaterialMatchException {
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
        throw new MaterialMatchException("Couldn't parse material: " + materialID);
    }

    public static Material catchMaterial(String material) throws MaterialMatchException {
        try {
            Method m = Material.class.getDeclaredMethod("getMaterial", String.class);
            m.setAccessible(true);
            Material mat = (Material) m.invoke(Material.class, material);
            if (mat == null) {
                throw new MaterialMatchException("Couldn't parse material: \"" + material + "\"");
            }
            return mat;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
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
