package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.KQBTDEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class KQBTDModel extends AnimatedTickingGeoModel<KQBTDEntity> {
    @Override
    public Identifier getModelResource(KQBTDEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/kqbtd.geo.json");
    }

    @Override
    public Identifier getTextureResource(KQBTDEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/kqbtd.png");
    }

    @Override
    public Identifier getAnimationResource(KQBTDEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/kqbtd.animation.json");
    }

    @Override
    public void setCustomAnimations(KQBTDEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser());
    }
}