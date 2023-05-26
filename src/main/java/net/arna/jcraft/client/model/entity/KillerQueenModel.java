package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.KillerQueenEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class KillerQueenModel extends AnimatedTickingGeoModel<KillerQueenEntity> {
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
    public void setCustomAnimations(KillerQueenEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);

        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), true, true, -0.1745329251f, -0.34f);
    }
}