package net.arna.jcraft.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;

/**
 * A {@link MultiBufferSource} that wraps every buffer it hands out in an {@link AlphaVertexConsumer}, so that
 * anything drawn through it (e.g. a vanilla item via {@link net.minecraft.client.renderer.entity.ItemRenderer})
 * is faded by a fixed alpha factor. The underlying buffers are still the delegate's, so the geometry flushes with
 * the normal batch.
 * <p>
 * Opaque/cutout block-atlas sheets are additionally remapped to {@link Sheets#translucentItemSheet()}: those render
 * types have no alpha blending, so a reduced vertex alpha would otherwise be ignored. The remap targets only the
 * block-atlas entity sheets, which share both the atlas texture and the {@code NEW_ENTITY} vertex format, so it is
 * format- and texture-safe. Sheets with their own texture (shields, banners, beds, ...) and glint/foil layers are
 * left untouched.
 */
public record AlphaMultiBufferSource(MultiBufferSource delegate, float alpha) implements MultiBufferSource {

    @Override
    public VertexConsumer getBuffer(final RenderType renderType) {
        return new AlphaVertexConsumer(delegate.getBuffer(blendable(renderType)), alpha);
    }

    /**
     * Swaps a non-blending block-atlas item/block sheet for the translucent one so the alpha actually takes effect.
     */
    private static RenderType blendable(final RenderType renderType) {
        if (renderType == Sheets.cutoutBlockSheet()
                || renderType == Sheets.solidBlockSheet()
                || renderType == Sheets.translucentCullBlockSheet()) {
            return Sheets.translucentItemSheet();
        }
        return renderType;
    }
}
