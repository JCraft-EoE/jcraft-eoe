package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.TheFoolEntity;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class TheFoolModel extends AnimatedTickingGeoModel<TheFoolEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(TheFoolEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/thefool.geo.json");
    }

    @Override
    public Identifier getTextureResource(TheFoolEntity object) {
        return new Identifier(JCraft.MOD_ID, object.isSand() ? "textures/entity/thefoolsand.png" : "textures/entity/thefool.png");
    }

    @Override
    public Identifier getAnimationResource(TheFoolEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/thefool.animation.json");
    }

    @Override
    public void setCustomAnimations(TheFoolEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser()) // -10Pi/180
            JCraftUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), true, true, 0.445329251f, -0.1745329251f);
    }
}