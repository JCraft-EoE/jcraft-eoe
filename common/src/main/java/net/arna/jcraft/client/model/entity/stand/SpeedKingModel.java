package net.arna.jcraft.client.model.entity.stand;

import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.MandomEntity;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;

public class SpeedKingModel extends StandEntityModel<SpeedKingEntity> {
    public SpeedKingModel() {
        super(JStandTypeRegistry.SPEED_KING.get(), 0f, 0f);
    }
}