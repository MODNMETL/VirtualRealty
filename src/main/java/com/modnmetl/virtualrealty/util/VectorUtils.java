package com.modnmetl.virtualrealty.util;

import com.modnmetl.virtualrealty.model.math.BlockVector2;
import org.bukkit.util.NumberConversions;

public final class VectorUtils {

    public static double distance(BlockVector2 vector, BlockVector2 vector2) {
        return Math.sqrt(distanceSquared(vector, vector2));
    }

    public static double distanceSquared(BlockVector2 vector, BlockVector2 vector2) {
        return NumberConversions.square(vector.getBlockX() - vector2.getBlockX()) +
                NumberConversions.square(vector.getBlockZ() - vector2.getBlockZ());
    }

}