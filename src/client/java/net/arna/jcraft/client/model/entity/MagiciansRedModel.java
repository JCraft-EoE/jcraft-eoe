package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.MagiciansRedEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class MagiciansRedModel extends AnimatedTickingGeoModel<MagiciansRedEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(MagiciansRedEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/mr.geo.json");
    }

    @Override
    public Identifier getTextureResource(MagiciansRedEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/mr.png");
    }

    @Override
    public Identifier getAnimationResource(MagiciansRedEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/mr.animation.json");
    }

    @Override
    public void setCustomAnimations(MagiciansRedEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), true, true, -0.10f, -0.05f);
    }
}