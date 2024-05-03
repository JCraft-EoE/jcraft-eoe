package net.arna.jcraft.client.renderer.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.world.ShockwaveHandlerComponent;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

import java.util.List;
import java.util.stream.IntStream;

public class ShockwaveEffectRenderer {
    private static final List<Identifier> TEXTURES = IntStream.range(0, ShockwaveHandlerComponent.Shockwave.MAX_AGE)
            .mapToObj(i -> JCraft.id("textures/effect/shockwave/shockwave_" + i + ".png"))
            .toList();

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(ShockwaveEffectRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        MatrixStack stack = ctx.matrixStack();
        Vec3d camPos = ctx.camera().getPos();

        ShockwaveHandlerComponent shockwaveHandler = JComponents.getShockwaveHandler(ctx.world());

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);

        for (ShockwaveHandlerComponent.Shockwave shockwave : shockwaveHandler.getShockwaves()) {
            stack.push();

            // Calculate matrix
            stack.translate(shockwave.getX() - camPos.x, shockwave.getY() - camPos.y, shockwave.getZ() - camPos.z);
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-shockwave.getYaw()));
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(shockwave.getPitch()));
            Matrix4f mat = stack.peek().getPositionMatrix();

            // Calculate light
            int blockLight = ctx.world().getLightLevel(LightType.BLOCK, shockwave.getBlockPos());
            int skyLight = ctx.world().getLightLevel(LightType.SKY, shockwave.getBlockPos());
            int light = LightmapTextureManager.pack(blockLight, skyLight);

            // Set texture
            RenderSystem.setShaderTexture(0, TEXTURES.get(shockwave.getFrame()));

            // Setup buffer
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buff = tess.getBuffer();
            buff.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);

            // Fill buffer
            float min = -0.5f * shockwave.getScale();
            float max = 0.5f * shockwave.getScale();
            buff.vertex(mat, min, min, 0).color(255, 255, 255, 255).texture(0, 0).light(light).next();
            buff.vertex(mat, max, min, 0).color(255, 255, 255, 255).texture(0, 1).light(light).next();
            buff.vertex(mat, max, max, 0).color(255, 255, 255, 255).texture(1, 1).light(light).next();
            buff.vertex(mat, min, max, 0).color(255, 255, 255, 255).texture(1, 0).light(light).next();

            // Finish up
            tess.draw();
            stack.pop();
        }
    }
}
