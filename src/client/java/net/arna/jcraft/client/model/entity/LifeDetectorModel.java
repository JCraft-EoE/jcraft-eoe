package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.LifeDetectorEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LifeDetectorModel extends AnimatedGeoModel<LifeDetectorEntity> {
    @Override
    public Identifier getModelResource(LifeDetectorEntity object) {
        return JCraft.id("geo/detector.geo.json");
    }

    @Override
    public Identifier getTextureResource(LifeDetectorEntity object) {
        return JCraft.id("textures/entity/projectiles/detector.png");
    }

    @Override
    public Identifier getAnimationResource(LifeDetectorEntity animatable) {
        return JCraft.id("animations/detector.animation.json");
    }

}
