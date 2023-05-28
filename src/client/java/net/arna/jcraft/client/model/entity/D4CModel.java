package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.D4CEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class D4CModel extends AnimatedTickingGeoModel<D4CEntity> {

    @Override
    public Identifier getModelResource(D4CEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/d4c.geo.json");
    }

    @Override
    public Identifier getTextureResource(D4CEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/d4c.png");
    }

    @Override
    public Identifier getAnimationResource(D4CEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/d4c.animation.json");
    }

    @Override
    public void setCustomAnimations(D4CEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), true, true);
    }
}