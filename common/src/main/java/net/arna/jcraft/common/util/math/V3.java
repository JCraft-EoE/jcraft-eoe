package net.arna.jcraft.common.util.math;

import net.minecraft.world.phys.Vec3;

/**
 * Mutable 3-D double-precision vector class.
 * Use instead of the mutating calls of {@link Vec3} because those create new instances and add unnecessary GC pressure.
 */
public final class V3 {
    public static V3 ZERO = new V3();
    public double x, y, z;

    /** Creates a null-vector. */
    public V3() {}

    public V3(Vec3 src) { this(src.x, src.y, src.z); }
    public V3(Vec3 src, double scalar) { this(src.x * scalar, src.y * scalar, src.z * scalar); }
    public V3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public V3 add(Vec3 rhs) { return add(rhs.x, rhs.y, rhs.z); }
    public V3 add(Vec3 rhs, double scalar) { return add(rhs.x * scalar, rhs.y * scalar, rhs.z * scalar); }
    public V3 add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public V3 scale(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    public double lengthSqr() {
        return x * x + y * y + z * z;
    }
}
