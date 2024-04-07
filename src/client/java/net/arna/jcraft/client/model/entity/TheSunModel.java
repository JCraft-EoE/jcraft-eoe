package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.entity.stand.TheSunEntity;

public class TheSunModel extends StandEntityModel<TheSunEntity> {
    public TheSunModel() {
        super(StandType.THE_SUN);
    }

    @Override
    protected boolean skipCustomAnimations() {
        return true;
    }
}
