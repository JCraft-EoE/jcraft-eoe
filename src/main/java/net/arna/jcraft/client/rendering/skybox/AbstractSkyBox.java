package net.arna.jcraft.client.rendering.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.mixin.client.WorldRendererAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public abstract class AbstractSkyBox implements JSkyBox {
    public transient float alpha;

    protected AbstractSkyBox() {
    }

    @Override
    public final float updateAlpha() {
        //TODO, fade here perhaps
        return this.alpha;
    }

    public void renderDecorations(WorldRendererAccess worldRendererAccess, MatrixStack matrices, Matrix4f matrix4f, float tickDelta, BufferBuilder bufferBuilder, float alpha) {
        RenderSystem.enableBlend();
        Vec3f rotationStatic = Rotation.DEFAULT.getStatic();
        Vec3f rotationAxis = Rotation.DEFAULT.getAxis();
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        matrices.push();
        // static rotation
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationStatic.getX()));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationStatic.getY()));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationStatic.getZ()));

        // axis rotation
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationAxis.getX()));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationAxis.getY()));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationAxis.getZ()));

        double timeRotation = 360D * MathHelper.floorMod(world.getTimeOfDay() / (24000.0D / 50), 1);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-90.0F));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float) timeRotation));

        // axis rotation
        matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationAxis.getZ()));
        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationAxis.getY()));
        matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationAxis.getX()));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        if (true /*make this depend on the alpha*/) {
            float i = 1.0F - world.getRainGradient(tickDelta);
            float brightness = world.method_23787(tickDelta) * i;
            if (brightness > 0.0F) {
                RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);
                BackgroundRenderer.clearFog();
                worldRendererAccess.getStarsBuffer().bind();
                worldRendererAccess.getStarsBuffer().draw(matrices.peek().getPositionMatrix(), matrix4f, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.pop();

    }
}
