package net.arna.jcraft.client.renderer.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.common.splatter.JSplatterManager;
import net.arna.jcraft.common.splatter.SplatterSection;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SplatterEffectRenderer {

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(SplatterEffectRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        JSplatterManager splatterManager = JUtils.getSplatterManager(ctx.world());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);

        MatrixStack matrices = ctx.matrixStack();
        Vec3d camPos = ctx.camera().getPos();

        splatterManager.iterateSplatters(splatter -> {
            if (splatter.isRemoved()) return;

            RenderSystem.setShaderTexture(0, splatter.getType().getTexture());

            matrices.push();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
            float alpha = splatter.getStrength(ctx.tickDelta());

            for (SplatterSection section : splatter.getSections())
                if (!section.isRemoved())
                    renderSection(section, buf, matrices, alpha, splatter.getOffset());

            tess.draw();
            matrices.pop();
        });

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }

    @SuppressWarnings("DuplicatedCode") // I do not care how similar the different directions' code are.
    private static void renderSection(SplatterSection section, BufferBuilder buf, MatrixStack matrices, float alpha, float offset) {
        matrices.push();
        Vector3f offsetVec = section.getDirection().getUnitVector();
        offsetVec.mul(offset, offset, offset); // Prevent z-fighting with anchor block and other splatters.
        matrices.translate(offsetVec.x(), offsetVec.y(), offsetVec.z());
        Matrix4f m = matrices.peek().getPositionMatrix();

        int blockLight = section.getWorld().getLightLevel(LightType.BLOCK, section.getBlockPos());
        int skyLight = section.getWorld().getLightLevel(LightType.SKY, section.getBlockPos());
        int light = LightmapTextureManager.pack(blockLight, skyLight);

        Vector3f min = section.getMinPos();
        Vector3f max = section.getMaxPos();
        Vec2f minUv = section.getMinUv();
        Vec2f maxUv = section.getMaxUv();

        float minX = min.x();
        float minY = min.y();
        float minZ = min.z();
        float maxX = max.x();
        float maxY = max.y();
        float maxZ = max.z();

        switch (section.getDirection()) {
            case UP -> {
                vertex(buf, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
            }
            case DOWN -> {
                vertex(buf, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
            }
            case NORTH -> {
                vertex(buf, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, m, minX, maxY, minZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, m, maxX, maxY, minZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
            }
            case EAST -> {
                vertex(buf, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, m, maxX, maxY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, m, maxX, maxY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
            }
            case SOUTH -> {
                vertex(buf, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, m, maxX, maxY, maxZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, m, minX, maxY, maxZ, minUv.x, minUv.y, alpha, light);
            }
            case WEST -> {
                vertex(buf, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, m, minX, maxY, maxZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, m, minX, maxY, minZ, maxUv.x, minUv.y, alpha, light);
            }
        }

        matrices.pop();
    }

    private static void vertex(BufferBuilder buf, Matrix4f matrix, float x, float y, float z, float u, float v, float alpha, int light) {
        buf
                .vertex(matrix, x, y, z)
                .color(1f, 1f, 1f, alpha)
                .texture(u, v)
                .light(light)
                .next();
    }
}
