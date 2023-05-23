package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.KillerQueenEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class KillerQueenModel extends AnimatedTickingGeoModel<KillerQueenEntity> {

    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(KillerQueenEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/killerqueen.geo.json");
    }

    @Override
    public Identifier getTextureResource(KillerQueenEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/killerqueen.png");
    }

    @Override
    public Identifier getAnimationResource(KillerQueenEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/killerqueen.animation.json");
    }

    @Override
    public void setCustomAnimations(KillerQueenEntity entity, int uniqueID) {
        super.setCustomAnimations(entity, uniqueID);

        IBone head = this.getAnimationProcessor().getBone("head");
        if (head != null && entity.hasUser()) {
            float pitch = entity.getUser().getPitch();
            if (entity.getState() == 1) {
                pitch += 30;
            }
            head.setRotationX(pitch * 3.1415f / 180f);
        }
    }
}