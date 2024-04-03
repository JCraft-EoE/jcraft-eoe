package net.arna.jcraft.client.rendering.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

public class CrimsonSkyBoxCool implements JSkyBox {
    public Rotation rotation = Rotation.DEFAULT;
    public float alpha = 10;
    protected Textures.Texture texture = new Textures.Texture(JCraft.id("textures/environment/time_erase/space.png"));
    public Textures textures;

    public CrimsonSkyBoxCool() {
        this.textures = new Textures(
                texture.withUV(1.0F / 3.0F, 1.0F / 2.0F, 2.0F / 3.0F, 1),
                texture.withUV(2.0F / 3.0F, 0, 1, 1.0F / 2.0F),
                texture.withUV(2.0F / 3.0F, 1.0F / 2.0F, 1, 1),
                texture.withUV(0, 1.0F / 2.0F, 1.0F / 3.0F, 1),
                texture.withUV(1.0F / 3.0F, 0, 2.0F / 3.0F, 1.0F / 2.0F),
                texture.withUV(0, 0, 1.0F / 3.0F, 1.0F / 2.0F)
        );
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    private boolean isShouldRotate() {
        return true;
    }

    @Override
    public void render(MatrixStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);

        ClientWorld world = Objects.requireNonNull(MinecraftClient.getInstance().world);

        Vector3f rotationStatic = this.rotation.getStatic();

        matrices.push();
        double timeRotation = isShouldRotate() ? 360.0D * MathHelper.floorMod(world.getLunarTime() / (24000.D / this.rotation.getRotationSpeed()) + 0.75D, 1) : 0D;
        this.applyTimeRotation(matrices, (float) timeRotation);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationStatic.x()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationStatic.y()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationStatic.z()));
        this.renderSkybox(matrices, tickDelta);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationStatic.z()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationStatic.y()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationStatic.x()));
        matrices.pop();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSkybox(MatrixStack matrices, float tickDelta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        for (int i = 0; i < 6; ++i) {
            Textures.Texture tex = this.textures.byId(i);
            matrices.push();

            RenderSystem.setShaderTexture(0, tex.getTextureId());

            if (i == 1) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            } else if (i == 2) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            } else if (i == 3) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
            } else if (i == 4) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
            } else if (i == 5) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            }

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).texture(tex.getMinU(), tex.getMinV()).color(1f, 1f, 1f, alpha).next();
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).texture(tex.getMinU(), tex.getMaxV()).color(1f, 1f, 1f, alpha).next();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).texture(tex.getMaxU(), tex.getMaxV()).color(1f, 1f, 1f, alpha).next();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).texture(tex.getMaxU(), tex.getMinV()).color(1f, 1f, 1f, alpha).next();
            tessellator.draw();
            matrices.pop();
        }
    }

    private void applyTimeRotation(MatrixStack matrices, float timeRotation) {
        Vector3f timeRotationAxis = this.rotation.getAxis();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(timeRotationAxis.x()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(timeRotationAxis.y()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(timeRotationAxis.z()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(timeRotation));
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(timeRotationAxis.z()));
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(timeRotationAxis.y()));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(timeRotationAxis.x()));
    }
}
