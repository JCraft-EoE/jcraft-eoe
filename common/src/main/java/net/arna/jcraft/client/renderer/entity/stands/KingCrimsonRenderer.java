package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.model.entity.stand.KingCrimsonModel;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link KingCrimsonEntity}.
 * @see KingCrimsonModel
 */
@Environment(EnvType.CLIENT)
public class KingCrimsonRenderer extends StandEntityRenderer<KingCrimsonEntity> {

    public KingCrimsonRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.KING_CRIMSON.get());
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
