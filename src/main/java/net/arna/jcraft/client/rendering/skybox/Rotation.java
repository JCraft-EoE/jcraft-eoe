package net.arna.jcraft.client.rendering.skybox;

import net.minecraft.util.math.Vec3f;

public class Rotation {
    public static final Rotation DEFAULT = new Rotation(new Vec3f(0F, 0F, 0F), new Vec3f(0F, 0F, 0F), 1);
    private final Vec3f staticRot;
    private final Vec3f axisRot;
    private final float rotationSpeed;

    public Rotation(Vec3f staticRot, Vec3f axisRot, float rotationSpeed) {
        this.staticRot = staticRot;
        this.axisRot = axisRot;
        this.rotationSpeed = rotationSpeed;
    }

    public Vec3f getStatic() {
        return this.staticRot;
    }

    public Vec3f getAxis() {
        return this.axisRot;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }
}
