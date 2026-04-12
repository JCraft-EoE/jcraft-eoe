package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.TWOHEyesLayer;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link TheWorldOverHeavenEntity}.
 */
@Environment(EnvType.CLIENT)
public class TheWorldOverHeavenRenderer extends StandEntityRenderer<TheWorldOverHeavenEntity> {

    public TheWorldOverHeavenRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new TWOHEyesLayer()), JStandTypeRegistry.THE_WORLD_OVER_HEAVEN.get(), -0.1745329251f, -0.31f);
    }

}
