package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GoldenExperienceEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class GoldenExperienceModel extends AnimatedTickingGeoModel<GoldenExperienceEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(GoldenExperienceEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/ge.geo.json");
    }

    @Override
    public Identifier getTextureResource(GoldenExperienceEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/ge.png");
    }

    @Override
    public Identifier getAnimationResource(GoldenExperienceEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/ge.animation.json");
    }

    @Override
    public void setCustomAnimations(GoldenExperienceEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);

        if (animatable.hasUser()) // -10Pi/180
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationEvent.getPartialTick(), true, true, 0, -0.1f);
    }
}