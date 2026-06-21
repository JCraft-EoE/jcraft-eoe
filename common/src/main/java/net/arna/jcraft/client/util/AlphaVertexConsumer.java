package net.arna.jcraft.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;

/**
 * A {@link VertexConsumer} that delegates to another consumer but multiplies the alpha component of every
 * vertex colour by a fixed factor.
 * <p>
 * This is used to fade items (e.g. an item held in a stand's hand) together with a translucent stand. Baking the
 * alpha into the vertex colours - rather than relying on
 * {@link com.mojang.blaze3d.systems.RenderSystem#setShaderColor} - is what makes this work: item geometry is
 * buffered and only drawn when the {@link net.minecraft.client.renderer.MultiBufferSource} is later flushed, by
 * which point any shader colour set at submission time has been reset.
 */
public class AlphaVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final float alpha;

    public AlphaVertexConsumer(final VertexConsumer delegate, final float alpha) {
        this.delegate = delegate;
        this.alpha = Mth.clamp(alpha, 0f, 1f);
    }

    @Override
    public VertexConsumer vertex(final double x, final double y, final double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
        delegate.color(red, green, blue, (int) (alpha * this.alpha));
        return this;
    }

    @Override
    public VertexConsumer uv(final float u, final float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(final int u, final int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(final int u, final int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(final float x, final float y, final float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(final int red, final int green, final int blue, final int alpha) {
        delegate.defaultColor(red, green, blue, (int) (alpha * this.alpha));
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
