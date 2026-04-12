package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.HandErasureLayer;
import net.arna.jcraft.common.entity.stand.TheHandEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link TheHandEntity}
 */
@Environment(EnvType.CLIENT)
public class TheHandRenderer extends StandEntityRenderer<TheHandEntity> {

    public TheHandRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new HandErasureLayer()), JStandTypeRegistry.THE_HAND.get(), false, false, 0f, 0f, 90f);
    }

}
