package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.HorusEyesLayer;
import net.arna.jcraft.common.entity.stand.HorusEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link HorusEntity}.
 */
@Environment(EnvType.CLIENT)
public class HorusRenderer extends StandEntityRenderer<HorusEntity> {

    public HorusRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new HorusEyesLayer()), JStandTypeRegistry.HORUS.get(), false, false, 0f, 0f, 90f);
    }

}
