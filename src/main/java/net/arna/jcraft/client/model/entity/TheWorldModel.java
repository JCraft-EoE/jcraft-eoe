package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.TheWorldEntity;
import net.arna.jcraft.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class TheWorldModel extends AnimatedTickingGeoModel<TheWorldEntity> {

    @Override
    public Identifier getModelResource(TheWorldEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/theworld.geo.json");
    }

    @Override
    public Identifier getTextureResource(TheWorldEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/theworld.png");
    }

    @Override
    public Identifier getAnimationResource(TheWorldEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/theworld.animation.json");
    }

    @Override
    public void setCustomAnimations(TheWorldEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);

        if (animatable.hasUser()) // -10Pi/180
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), true, true, -0.1745329251f, -0.1745329251f);
    }
}