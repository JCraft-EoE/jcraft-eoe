package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.model.entity.stand.KingCrimsonModel;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link KingCrimsonEntity}.
 * @see KingCrimsonModel
 */
@Environment(EnvType.CLIENT)
public class KingCrimsonRenderer extends StandEntityRenderer<KingCrimsonEntity> {

    public KingCrimsonRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new StandEntityRenderer.StandHandItemsRenderLayer<>()),
                JStandTypeRegistry.KING_CRIMSON.get(), 0f, 0f);
        //this.addLayer(new KCTELayer(this));
    }

    @Override
    protected float getGreen(final KingCrimsonEntity stand, final float green, final float alpha) {
        return green - (1f - alpha);
    }

    @Override
    protected float getBlue(final KingCrimsonEntity stand, final float blue, final float alpha) {
        return blue - (1f - alpha);
    }
}
