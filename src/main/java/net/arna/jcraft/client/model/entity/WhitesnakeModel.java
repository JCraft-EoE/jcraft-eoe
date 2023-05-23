package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.WhitesnakeEntity;
import net.arna.jcraft.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class WhitesnakeModel extends AnimatedTickingGeoModel<WhitesnakeEntity> {

    @Override
    public Identifier getModelResource(WhitesnakeEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/whitesnake.geo.json");
    }

    @Override
    public Identifier getTextureResource(WhitesnakeEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/whitesnake.png");
    }

    @Override
    public Identifier getAnimationResource(WhitesnakeEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/whitesnake.animation.json");
    }

    @Override
    public void setCustomAnimations(WhitesnakeEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser());
    }
}