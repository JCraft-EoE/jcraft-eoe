package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.entity.stand.StandType;

public class GoldenExperienceModel extends StandEntityModel<GoldExperienceEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    public GoldenExperienceModel() {
        super(StandType.GOLD_EXPERIENCE, 0, -0.1f);
    }
}
