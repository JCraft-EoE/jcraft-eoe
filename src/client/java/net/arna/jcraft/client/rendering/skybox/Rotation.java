package net.arna.jcraft.client.rendering.skybox;


import org.joml.Vector3f;

public class Rotation {
    public static final Rotation DEFAULT = new Rotation(new Vector3f(0F, 0F, 0F), new Vector3f(0F, 0F, 0F), 1);
    private final Vector3f staticRot;
    private final Vector3f axisRot;
    private final float rotationSpeed;

    public Rotation(Vector3f staticRot, Vector3f axisRot, float rotationSpeed) {
        this.staticRot = staticRot;
        this.axisRot = axisRot;
        this.rotationSpeed = rotationSpeed;
    }

    public Vector3f getStatic() {
        return this.staticRot;
    }

    public Vector3f getAxis() {
        return this.axisRot;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }
}
