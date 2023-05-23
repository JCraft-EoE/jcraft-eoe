package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.SilverChariotEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class SilverChariotModel extends AnimatedTickingGeoModel<SilverChariotEntity> {

    @Override
    public Identifier getModelResource(SilverChariotEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/silverchariot.geo.json");
    }

    @Override
    public Identifier getTextureResource(SilverChariotEntity object) {
        int mode = object.getMode();

        if (mode == 3) {
            return new Identifier(JCraft.MOD_ID, "textures/entity/possessedchariot.png");
        }
        if (mode == 2) {
            return new Identifier(JCraft.MOD_ID, "textures/entity/noarmorchariot.png");
        }
        return new Identifier(JCraft.MOD_ID, "textures/entity/silverchariot.png");
    }

    @Override
    public Identifier getAnimationResource(SilverChariotEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/silverchariot.animation.json");
    }

    @Override
    public void setCustomAnimations(SilverChariotEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser());
    }
}