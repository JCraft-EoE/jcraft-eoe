package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.GoldenExperienceEntity;
import net.arna.jcraft.common.entity.StandType;

public class GoldenExperienceModel extends StandEntityModel<GoldenExperienceEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
    
    public GoldenExperienceModel() {
        super(StandType.GOLD_EXPERIENCE, 0, -0.1f);
    }
}
