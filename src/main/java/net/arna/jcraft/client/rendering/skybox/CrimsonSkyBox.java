package net.arna.jcraft.client.rendering.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.mixin.client.WorldRendererAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

public class CrimsonSkyBox extends AbstractSkyBox {

    @Override
    public float getAlpha() {
        return 1;
    }

    @Override
    public void render(WorldRendererAccess worldRendererAccess, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        this.alpha = 1;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, WorldRendererAccess.getEndSky());
        Tessellator tessellator = Tessellator.getInstance();
        BackgroundRenderer.clearFog();
        for (int i = 0; i < 6; ++i) {
            matrices.push();
            if (i == 1) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
            }

            if (i == 2) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
            }

            if (i == 3) {
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F));
            }

            if (i == 4) {
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
            }

            if (i == 5) {
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90.0F));
            }

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            Color color = new Color(255, 20, 105);
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).texture(0.0F, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), 255 * getAlpha()).next();
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).texture(0.0F, 16.0F).color(color.getRed(), color.getGreen(), color.getBlue(), 255 * getAlpha()).next();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).texture(16.0F, 16.0F).color(color.getRed(), color.getGreen(), color.getBlue(), 255 * getAlpha()).next();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).texture(16.0F, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), 255 * getAlpha()).next();
            tessellator.draw();
            matrices.pop();
        }

        this.renderDecorations(worldRendererAccess, matrices, projectionMatrix, tickDelta, bufferBuilder, getAlpha());

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
