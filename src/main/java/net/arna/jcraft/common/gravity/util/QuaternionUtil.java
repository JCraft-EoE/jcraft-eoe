package net.arna.jcraft.common.gravity.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Math.cos;
import static org.joml.Math.sin;

public abstract class QuaternionUtil {

    public static float magnitude(Quaternionf quaternion) {
        return MathHelper.sqrt(quaternion.w() * quaternion.w() + quaternion.x() * quaternion.x() + quaternion.y() * quaternion.y() + quaternion.z() * quaternion.z());
    }

    public static float magnitudeSq(Quaternionf quaternion) {
        return quaternion.w() * quaternion.w() + quaternion.x() * quaternion.x() + quaternion.y() * quaternion.y() + quaternion.z() * quaternion.z();
    }

    public static Quaternionf quaternionf(Vector3f axis, float rotationAngle, boolean degrees) {
        if (degrees) {
            rotationAngle *= 0.017453292F;
        }

        float f = sin(rotationAngle / 2.0F);
        var x = axis.x() * f;
        var y = axis.y() * f;
        var z = axis.z() * f;
        var w = cos(rotationAngle / 2.0F);
        return new Quaternionf(x, y, z, w);
    }

    public static Quaternionf hamiltonProduct(Quaternionf p, Quaternionf q) {
        float nw = p.w * q.w - p.x * q.x - p.y * q.y - p.z * q.z;
        float nx = p.w * q.x + p.x * q.w + p.y * q.z - p.z * q.y;
        float ny = p.w * q.y - p.x * q.z + p.y * q.w + p.z * q.x;
        float nz = p.w * q.z + p.x * q.y - p.y * q.x + p.z * q.w;
        return new Quaternionf(nx, ny, nz, nw);
    }

    public static Quaternionf getViewRotation(float pitch, float yaw) {
        Quaternionf r1 = quaternionf(new Vector3f(1, 0, 0), pitch, true);
        Quaternionf r2 = quaternionf(new Vector3f(0, 1, 0), yaw + 180, true);
        return hamiltonProduct(r1, r2);
    }

    // NOTE the "from" and "to" cannot be opposite
    public static Quaternionf getRotationBetween(Vec3d from, Vec3d to) {
        from = from.normalize();
        to = to.normalize();
        Vec3d axis = from.crossProduct(to).normalize();
        double cos = from.dotProduct(to);
        double angle = Math.acos(cos);
        return quaternionf(axis.toVector3f(), (float) angle, false);
    }

    // does not mutate the argument
    public static Quaternionf mult(Quaternionf a, Quaternionf b) {
        Quaternionf r = new Quaternionf(a);
        return hamiltonProduct(r, b);
    }

    // does not mutate the argument
    public static Quaternionf inversed(Quaternionf a) {
        Quaternionf r = new Quaternionf(a);
        r.conjugate();
        return r;
    }
}
