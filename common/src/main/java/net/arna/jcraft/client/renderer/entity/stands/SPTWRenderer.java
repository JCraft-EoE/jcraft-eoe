package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.SPHairLayer;
import net.arna.jcraft.common.entity.stand.AbstractStarPlatinumEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link net.arna.jcraft.common.entity.stand.SPTWEntity SPTWEntity}.
 */
@Environment(EnvType.CLIENT)
public class SPTWRenderer extends StandEntityRenderer<AbstractStarPlatinumEntity<?, ?>> {

    public SPTWRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new SPHairLayer()), JStandTypeRegistry.STAR_PLATINUM_THE_WORLD.get(), false, false, 0f, 0f, 90f);
    }

}
