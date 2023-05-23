package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.MadeInHeavenEntity;
import net.arna.jcraft.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class MadeInHeavenModel extends AnimatedTickingGeoModel<MadeInHeavenEntity> {
    @Override
    public Identifier getModelResource(MadeInHeavenEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/mih.geo.json");
    }

    @Override
    public Identifier getTextureResource(MadeInHeavenEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/mih.png");
    }

    @Override
    public Identifier getAnimationResource(MadeInHeavenEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/mih.animation.json");
    }

    @Override
    public void setCustomAnimations(MadeInHeavenEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser());
    }
}