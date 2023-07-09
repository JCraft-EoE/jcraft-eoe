package net.arna.jcraft.client.rendering.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.client.registry.JShaderRegistry;
import net.arna.jcraft.client.rendering.shader.JShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.util.function.Supplier;

public class CrimsonSkyBox implements JSkyBox {
    public transient float alpha = 1;

    @Override
    public float getAlpha() {
        return alpha;
    }

    @Override
    public void render(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        this.alpha = 1;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        Matrix4f mat = matrices.peek().getPositionMatrix();
        Matrix4f invMat = mat.copy();
        invMat.invert();
        JShader jShader = (JShader) JShaderRegistry.TEST.getInstance().get();
        jShader.getUniformOrDefault("InverseTransformMatrix").set(invMat);
        jShader.getUniformOrDefault("Time").set((client.world.getTime() + tickDelta) / 20);
        Supplier<Shader> shaderInstanceSupplier = () -> jShader;
        RenderSystem.setShader(shaderInstanceSupplier);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        for(int i = 0; i < 6; ++i) {
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

            projectionMatrix = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(projectionMatrix, -110.0F, -110.0F, -110.0F).texture(0.0F, 0.0F).color(40, 40, 40, 255 * getAlpha()).next();
            bufferBuilder.vertex(projectionMatrix, -110.0F, -110.0F, 110.0F).texture(0.0F, 16.0F).color(40, 40, 40, 255 * getAlpha()).next();
            bufferBuilder.vertex(projectionMatrix, 110.0F, -110.0F, 110.0F).texture(16.0F, 16.0F).color(40, 40, 40, 255 * getAlpha()).next();
            bufferBuilder.vertex(projectionMatrix, 110.0F, -110.0F, -110.0F).texture(16.0F, 0.0F).color(40, 40, 40, 255 * getAlpha()).next();
            tessellator.draw();
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
