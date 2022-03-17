package com.modnmetl.virtualrealty.utils;

import com.modnmetl.virtualrealty.objects.math.BlockVector2;
import org.bukkit.util.NumberConversions;

public class VectorUtils {

    public static double distance(BlockVector2 vector, BlockVector2 vector2) {
        return Math.sqrt(distanceSquared(vector, vector2));
    }

    public static double distanceSquared(BlockVector2 vector, BlockVector2 vector2) {
        return NumberConversions.square(vector.getBlockX() - vector2.getBlockX()) +
                NumberConversions.square(vector.getBlockZ() - vector2.getBlockZ());
    }

}
