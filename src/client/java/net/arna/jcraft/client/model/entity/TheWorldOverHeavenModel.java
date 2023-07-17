package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.entity.TheWorldOverHeavenEntity;

public class TheWorldOverHeavenModel extends StandEntityModel<TheWorldOverHeavenEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
    
    public TheWorldOverHeavenModel() {
        super(StandType.THE_WORLD_OVER_HEAVEN, -0.1745329251f, -0.31f);
    }
}
