package net.arna.jcraft.client.rendering.skybox;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public interface JSkyBox {
    float getAlpha();

    void render(MatrixStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog);
}