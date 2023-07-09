package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.CMoonEntity;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class CMoonModel extends AnimatedTickingGeoModel<CMoonEntity> {
    @Override
    public Identifier getModelResource(CMoonEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/cmoon.geo.json");
    }

    @Override
    public Identifier getTextureResource(CMoonEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/cmoon.png");
    }

    @Override
    public Identifier getAnimationResource(CMoonEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/cmoon.animation.json");
    }

    @Override
    public void setCustomAnimations(CMoonEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JClientUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationEvent.getPartialTick(), true, true);
    }
}