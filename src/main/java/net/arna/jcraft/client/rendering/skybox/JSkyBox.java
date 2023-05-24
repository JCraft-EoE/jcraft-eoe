package net.arna.jcraft.client.rendering.skybox;

import net.arna.jcraft.mixin.client.WorldRendererAccess;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public interface JSkyBox {
    float getAlpha();

    float updateAlpha();

    void render(WorldRendererAccess worldRendererAccess, MatrixStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog);
}