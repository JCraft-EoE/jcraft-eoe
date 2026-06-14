package net.arna.jcraft.client.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arna.jcraft.mixin.client.CompositeStateAccessor;
import net.arna.jcraft.mixin.client.RenderTypeStateAccessor;
import net.arna.jcraft.mixin.client.TextureStateShardAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Wraps a {@link MultiBufferSource} so an entity rendered through it comes out translucent at a fixed alpha. Each
 * requested {@link RenderType} is rerouted to {@link RenderType#entityTranslucent(ResourceLocation)} for the same
 * texture (which actually blends), and the returned consumer forces the alpha. Used for the Made In Heaven afterimage.
 * <p>
 * Fail-safe: if a render type's texture can't be resolved (e.g. lines, glint), the original type is used, so that part
 * simply renders opaque instead of crashing.
 */
public class AlphaFadeBufferSource implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final int alpha; // 0..255

    public AlphaFadeBufferSource(final MultiBufferSource delegate, final int alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer getBuffer(final RenderType type) {
        final ResourceLocation texture = textureOf(type);
        final RenderType target = texture != null ? RenderType.entityTranslucent(texture) : type;
        return new AlphaForcingVertexConsumer(delegate.getBuffer(target), alpha);
    }

    private static ResourceLocation textureOf(final RenderType type) {
        // Walks RenderType.CompositeRenderType -> CompositeState -> TextureStateShard via accessor interfaces.
        // CompositeState is final, so its accessor interface is reached through an Object cast.
        if (!(type instanceof RenderTypeStateAccessor composite)) {
            return null;
        }
        final Object textureState = ((CompositeStateAccessor) (Object) composite.jcraft$callState()).jcraft$getTextureState();
        if (textureState instanceof TextureStateShardAccessor texture) {
            return texture.jcraft$cutoutTexture().orElse(null);
        }
        return null;
    }
}
