package net.arna.jcraft.common.util.extensions;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

@UtilityClass
public class VecExtensions {
    public static float getComponentAlongAxis(Vec3f vec, Direction.Axis axis) {
        return switch (axis) {
            case X -> vec.getX();
            case Y -> vec.getY();
            case Z -> vec.getZ();
        };
    }
}
