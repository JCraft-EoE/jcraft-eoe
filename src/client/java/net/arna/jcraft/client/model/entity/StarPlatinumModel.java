package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StarPlatinumEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class StarPlatinumModel extends AnimatedTickingGeoModel<StarPlatinumEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(StarPlatinumEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/starplatinum.geo.json");
    }

    @Override
    public Identifier getTextureResource(StarPlatinumEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/starplatinum.png");
    }

    @Override
    public Identifier getAnimationResource(StarPlatinumEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/starplatinum.animation.json");
    }

    @Override
    public void setCustomAnimations(StarPlatinumEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationEvent.getPartialTick(), true, true);
    }
}