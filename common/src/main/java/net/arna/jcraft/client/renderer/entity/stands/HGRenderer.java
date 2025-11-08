package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.HGGlowLayer;
import net.arna.jcraft.common.entity.stand.HGEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link HGEntity}.
 */
public class HGRenderer extends StandEntityRenderer<HGEntity> {
    public HGRenderer(final EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new HGGlowLayer()), JStandTypeRegistry.HIEROPHANT_GREEN.get(), 0f, -0.2f);
    }
}
