package net.arna.jcraft.client.renderer.effects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent.Shockwave;

public class ShockwaveEffectRenderer {
    private static final Map<Shockwave.Type, List<ResourceLocation>> TEXTURES = new EnumMap<>(Shockwave.Type.class);

    /**
     * One {@link RenderType} per texture, memoized so we don't allocate a fresh composite state per frame.
     * The render type is responsible for setting up and tearing down all GL state (blend, depth, cull, shader,
     * lightmap, texture), so this renderer no longer touches {@link com.mojang.blaze3d.systems.RenderSystem}
     * directly. That state-leak previously polluted the translucent block pass and made water look more transparent.
     */
    private static final Function<ResourceLocation, RenderType> SHOCKWAVE_RENDER_TYPE =
            Util.memoize(ShockwaveEffectRenderer::createRenderType);

    static {
        for (Shockwave.Type type : Shockwave.Type.values()) {
            List<ResourceLocation> list = new ArrayList<>();

            // Generate ResourceLocation for each index, prefixed with type.name
            for (int i = 0; i < Shockwave.MAX_AGE; i++) {
                ResourceLocation id = JCraft.id("textures/effect/shockwave/" + type.getName() + "_" + i + ".png");
                list.add(id);
            }

            TEXTURES.put(type, list);
        }
    }
    private static final List<Shockwave> toRender = new ArrayList<>();

    public static void render(final PoseStack stack, final Vec3 camPos, final ClientLevel world, final MultiBufferSource consumerProvider) {
        final CommonShockwaveHandlerComponent shockwaveHandler = JComponentPlatformUtils.getShockwaveHandler(world);

        // java.util.ConcurrentModificationException prevention
        toRender.clear();
        toRender.addAll(shockwaveHandler.getShockwaves());
        for (final Shockwave shockwave : toRender) {
            stack.pushPose();

            // Calculate matrix
            stack.translate(shockwave.getX() - camPos.x, shockwave.getY() - camPos.y, shockwave.getZ() - camPos.z);
            stack.mulPose(Axis.YP.rotationDegrees(-shockwave.getYaw()));
            stack.mulPose(Axis.XP.rotationDegrees(shockwave.getPitch()));
            final Matrix4f mat = stack.last().pose();

            // Calculate light
            final int blockLight = world.getBrightness(LightLayer.BLOCK, shockwave.getBlockPos());
            final int skyLight = world.getBrightness(LightLayer.SKY, shockwave.getBlockPos());
            final int light = LightTexture.pack(blockLight, skyLight);

            // Resolve the per-frame texture and grab the matching buffer from the shared MultiBufferSource.
            final ResourceLocation texture = TEXTURES.get(shockwave.getType()).get(shockwave.getFrame());
            final VertexConsumer vc = consumerProvider.getBuffer(SHOCKWAVE_RENDER_TYPE.apply(texture));

            // Fill buffer (UV mapping preserved from the previous Tesselator-based implementation).
            final float min = -0.5f * shockwave.getScale();
            final float max =  0.5f * shockwave.getScale();
            vertex(vc, mat, min, min, 0, 0, 0, light);
            vertex(vc, mat, max, min, 0, 0, 1, light);
            vertex(vc, mat, max, max, 0, 1, 1, light);
            vertex(vc, mat, min, max, 0, 1, 0, light);

            stack.popPose();
        }
    }

    private static void vertex(final VertexConsumer vc, final Matrix4f matrix,
                               final float x, final float y, final float z,
                               final float u, final float v, final int light) {
        vc
                .vertex(matrix, x, y, z)
                .color(1f, 1f, 1f, 1f)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                // The quad is already oriented in world space via the pose stack; +Z is the front face.
                .normal(0f, 0f, 1f)
                .endVertex();
    }

    /**
     * Custom {@link RenderType} for shockwaves. Mirrors {@code SplatterEffectRenderer#createRenderType} so we
     * get translucent blending, lightmap support, and proper state setup/teardown via the standard pipeline
     * instead of leaking {@code RenderSystem} state into the translucent block pass.
     */
    private static RenderType createRenderType(final ResourceLocation texture) {
        final RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .setCullState(RenderType.NO_CULL)
                .setWriteMaskState(RenderType.COLOR_WRITE) // No depth writes — standard for translucent particles.
                .createCompositeState(true);
        return RenderType.create("jcraft_shockwave", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, true, state);
    }
}
