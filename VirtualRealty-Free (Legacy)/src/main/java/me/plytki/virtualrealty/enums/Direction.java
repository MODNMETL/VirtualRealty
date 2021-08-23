package me.plytki.virtualrealty.enums;

public enum Direction {

    SOUTH(315, 45),
    WEST(45, 135),
    NORTH(135, 225),
    EAST(225, 315);

    private final float minYaw;
    private final float maxYaw;

    Direction(float minYaw, float maxYaw) {
        this.minYaw = minYaw;
        this.maxYaw = maxYaw;
    }

    public static Direction byYaw(float yaw) {
        float absoluteYaw = Math.abs(yaw);
        Direction direction = null;
        if(absoluteYaw > 315 || absoluteYaw <= 45) {
            //south
            direction = SOUTH;
        } else if(absoluteYaw > 45 && absoluteYaw <= 135) {
            //west
            direction = yaw > 0 ? WEST : EAST;
        } else if(absoluteYaw > 135 && absoluteYaw <= 225) {
            //north
            direction = NORTH;
        } else if(absoluteYaw > 225 && absoluteYaw <= 315) {
            //east
            direction = yaw > 0 ? EAST : WEST;
        }
        return direction;
    }

    public float getMaxYaw() {
        return maxYaw;
    }

    public float getMinYaw() {
        return minYaw;
    }

}
