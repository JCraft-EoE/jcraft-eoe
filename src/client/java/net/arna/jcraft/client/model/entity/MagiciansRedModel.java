package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.MagiciansRedEntity;
import net.arna.jcraft.common.entity.StandType;

public class MagiciansRedModel extends StandEntityModel<MagiciansRedEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
    
    public MagiciansRedModel() {
        super(StandType.MAGICIANS_RED, -0.10f, -0.05f);
    }
}
