package net.arna.jcraft.client.model.entity.stand;

import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;

/**
 * The {@link StandEntityModel} for {@link SpeedKingEntity}.
 * @see net.arna.jcraft.client.renderer.entity.stands.SpeedKingRenderer SpeedKingRenderer
 */
public class SpeedKingModel extends StandEntityModel<SpeedKingEntity> {
    public SpeedKingModel() {
        super(JStandTypeRegistry.SPEED_KING.get());
    }
}
