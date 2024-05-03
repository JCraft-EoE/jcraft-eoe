package net.arna.jcraft.common.util.extensions;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

@UtilityClass
public class VecExtensions {
    public static float getComponentAlongAxis(Vector3f vec, Direction.Axis axis) {
        return switch (axis) {
            case X -> vec.x();
            case Y -> vec.y();
            case Z -> vec.z();
        };
    }
}
