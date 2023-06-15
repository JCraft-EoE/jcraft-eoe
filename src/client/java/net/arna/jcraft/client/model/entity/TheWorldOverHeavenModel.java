package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.TheWorldOverHeavenEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class TheWorldOverHeavenModel extends AnimatedTickingGeoModel<TheWorldOverHeavenEntity> {

    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(TheWorldOverHeavenEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/twoh.geo.json");
    }

    @Override
    public Identifier getTextureResource(TheWorldOverHeavenEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/twoh.png");
    }

    @Override
    public Identifier getAnimationResource(TheWorldOverHeavenEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/twoh.animation.json");
    }

    @Override
    public void setCustomAnimations(TheWorldOverHeavenEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationEvent.getPartialTick(), true, true, -0.1745329251f, -0.31f);
    }
}