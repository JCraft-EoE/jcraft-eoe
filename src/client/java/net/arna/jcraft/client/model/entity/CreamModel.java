package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.CreamEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class CreamModel extends AnimatedTickingGeoModel<CreamEntity> {

    @Override
    public Identifier getModelResource(CreamEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/cream.geo.json");
    }

    @Override
    public Identifier getTextureResource(CreamEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/cream.png");
    }

    @Override
    public Identifier getAnimationResource(CreamEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/cream.animation.json");
    }

    @Override
    public void setCustomAnimations(CreamEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);

        if (animatable.hasUser()) // -10Pi/180
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), true, true, -0.1745329251f, -0.1f);
    }
}