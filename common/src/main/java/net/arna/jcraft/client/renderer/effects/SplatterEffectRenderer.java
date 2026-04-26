package net.arna.jcraft.client.renderer.effects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.arna.jcraft.common.splatter.JSplatterManager;
import net.arna.jcraft.common.splatter.SplatterSection;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.function.Function;

public class SplatterEffectRenderer {

    private static final Function<ResourceLocation, RenderType> SPLATTER_RENDER_TYPE =
            Util.memoize(SplatterEffectRenderer::createRenderType);

    public static void render(final PoseStack matrices, final Vec3 camPos, final ClientLevel world, final float tickDelta, final MultiBufferSource bufferSource) {
        final JSplatterManager splatterManager = JUtils.getSplatterManager(world);

        splatterManager.iterateSplatters(splatter -> {
            if (splatter.isRemoved()) {
                return;
            }

            final VertexConsumer vc = bufferSource.getBuffer(SPLATTER_RENDER_TYPE.apply(splatter.getTexture()));
            final float alpha = splatter.getStrength(tickDelta);

            for (SplatterSection section : splatter.getSections()) {
                if (!section.isRemoved()) {
                    renderSection(section, vc, matrices, camPos, alpha, splatter.getOffset());
                }
            }
        });
    }

    @SuppressWarnings("DuplicatedCode") // I do not care how similar the different directions' code is. (vased)
    private static void renderSection(final SplatterSection section, final VertexConsumer vc, final PoseStack matrices,
                                      final Vec3 camPos, final float alpha, final float offset) {
        matrices.pushPose();
        final Vector3f offsetVec = section.getDirection().step();
        offsetVec.mul(offset, offset, offset); // Prevent z-fighting with anchor block and other splatters.
        matrices.translate(offsetVec.x(), offsetVec.y(), offsetVec.z());
        final Matrix4f m = matrices.last().pose();

        final int blockLight = section.getWorld().getBrightness(LightLayer.BLOCK, section.getBlockPos());
        final int skyLight = section.getWorld().getBrightness(LightLayer.SKY, section.getBlockPos());
        final int light = LightTexture.pack(blockLight, skyLight);

        final Vec2 minUv = section.getMinUv();
        final Vec2 maxUv = section.getMaxUv();

        final Vector3d min = new Vector3d(section.getMinPos()).sub(camPos.x(), camPos.y(), camPos.z());
        final float minX = (float) min.x(), minY = (float) min.y(), minZ = (float) min.z();
        final Vector3d max = new Vector3d(section.getMaxPos()).sub(camPos.x(), camPos.y(), camPos.z());
        final float maxX = (float) max.x(), maxY = (float) max.y(), maxZ = (float) max.z();

        final Vector3f normal = section.getDirection().step();
        final float nx = normal.x(), ny = normal.y(), nz = normal.z();

        switch (section.getDirection()) {
            case UP -> {
                vertex(vc, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light, nx, ny, nz);
            }
            case DOWN -> {
                vertex(vc, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light, nx, ny, nz);
            }
            case NORTH -> {
                vertex(vc, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, maxY, minZ, minUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, maxY, minZ, maxUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light, nx, ny, nz);
            }
            case EAST -> {
                vertex(vc, m, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, maxY, minZ, minUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, maxY, maxZ, minUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light, nx, ny, nz);
            }
            case SOUTH -> {
                vertex(vc, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, maxX, maxY, maxZ, maxUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, maxY, maxZ, minUv.x, minUv.y, alpha, light, nx, ny, nz);
            }
            case WEST -> {
                vertex(vc, m, minX, minY, minZ, minUv.x, minUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, maxY, maxZ, maxUv.x, maxUv.y, alpha, light, nx, ny, nz);
                vertex(vc, m, minX, maxY, minZ, maxUv.x, minUv.y, alpha, light, nx, ny, nz);
            }
        }

        matrices.popPose();
    }

    private static void vertex(final VertexConsumer vc, final Matrix4f matrix, final float x, final float y, final float z,
                               final float u, final float v, final float alpha, final int light,
                               final float nx, final float ny, final float nz) {
        vc
                .vertex(matrix, x, y, z)
                .color(1f, 1f, 1f, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();
    }

    /**
     * Use Entity Translucent shader to render our splatters so
     * it works with translucency AND lightmap.
     * The regular shader that is supposed to support both, doesn't actually
     * support lightmap.
     * @param texture The texture to render
     * @return A rendertype for splatters for the given texture
     */
    private static RenderType createRenderType(ResourceLocation texture) {
        final RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .setCullState(RenderType.NO_CULL)
                .setWriteMaskState(RenderType.COLOR_WRITE) // No depth writes, same as before.
                .createCompositeState(true);
        return RenderType.create("jcraft_splatter", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, true, state);
    }
}
