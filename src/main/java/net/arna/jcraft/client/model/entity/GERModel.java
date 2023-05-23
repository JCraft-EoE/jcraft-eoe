package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.GEREntity;
import net.arna.jcraft.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class GERModel extends AnimatedTickingGeoModel<GEREntity> {
    @Override
    public Identifier getModelResource(GEREntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/ger.geo.json");
    }

    @Override
    public Identifier getTextureResource(GEREntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/ger.png");
    }

    @Override
    public Identifier getAnimationResource(GEREntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/ger.animation.json");
    }

    @Override
    public void setCustomAnimations(GEREntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser());
    }
}