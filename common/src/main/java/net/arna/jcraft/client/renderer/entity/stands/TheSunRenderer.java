package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.SunGlowLayer;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;

/**
 * The {@link StandEntityRenderer} for {@link TheSunEntity}.
 */
@Environment(EnvType.CLIENT)
public class TheSunRenderer extends StandEntityRenderer<TheSunEntity> {

    public TheSunRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b
                        .setScale((sun) -> {
                            return sun.getRawScale();
                        })
                        .addRenderLayer(new SunGlowLayer())
                        //TODO: translucent layer that isn't layered over and has no shading
                        .setRenderType(renderType(RenderType::dragonExplosionAlpha)),
                JStandTypeRegistry.THE_SUN.get(), false, false, 0f, 0f, 90f);
    }

    @Override
    public int getBlockLightLevel(final @NonNull TheSunEntity entity, final @NonNull BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(final @NonNull TheSunEntity entity, final @NonNull BlockPos pos) {
        return 15;
    }
}
