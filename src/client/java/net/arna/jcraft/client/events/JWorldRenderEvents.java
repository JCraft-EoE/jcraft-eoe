package net.arna.jcraft.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class JWorldRenderEvents {

    public static void onLast(WorldRenderContext context) {
        MatrixStack matrixStack = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        if (context.worldRenderer().transparencyShader != null) {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        }
        RenderHandler.beginBufferedRendering(matrixStack);

        if (RenderHandler.MATRIX4F != null) {
            RenderSystem.getModelViewMatrix().load(RenderHandler.MATRIX4F);
        }
        RenderHandler.renderBufferedBatches(matrixStack);
        RenderHandler.endBufferedRendering(matrixStack);
        if (context.worldRenderer().transparencyShader != null) {
            context.worldRenderer().getCloudsFramebuffer().beginWrite(false);
        }
        matrixStack.pop();
    }

    public static void afterTranslucent(WorldRenderContext context) {
        MatrixStack matrixStack = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderHandler.MATRIX4F = new Matrix4f(RenderSystem.getModelViewMatrix());
        matrixStack.pop();
    }
}
