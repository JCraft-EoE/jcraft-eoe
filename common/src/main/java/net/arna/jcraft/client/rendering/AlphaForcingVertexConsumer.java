
package net.arna.jcraft.client.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Delegating {@link VertexConsumer} that overrides the alpha of every vertex with a fixed value. Used to fade the
 * Made In Heaven afterimage copies; all other vertex data passes through unchanged.
 */
public class AlphaForcingVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int alpha; // 0..255

    public AlphaForcingVertexConsumer(final VertexConsumer delegate, final int alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer vertex(final double x, final double y, final double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int a) {
        delegate.color(red, green, blue, alpha);
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
    public void defaultColor(final int red, final int green, final int blue, final int a) {
        delegate.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
