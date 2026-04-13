package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link SpeedKingEntity}.
 */
@Environment(EnvType.CLIENT)
public class SpeedKingRenderer extends StandEntityRenderer<SpeedKingEntity> {
    public SpeedKingRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.SPEED_KING.get());
    }
}
