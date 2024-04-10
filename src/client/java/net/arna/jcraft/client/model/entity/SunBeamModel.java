package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.SunBeamProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SunBeamModel extends AnimatedGeoModel<SunBeamProjectile> {
    @Override
    public Identifier getModelResource(SunBeamProjectile object) {
        return JCraft.id("geo/sunbeam.geo.json");
    }

    @Override
    public Identifier getTextureResource(SunBeamProjectile object) {
        return JCraft.id("textures/entity/sunbeam/skin_" + object.getSkin() + ".png");
    }

    @Override
    public Identifier getAnimationResource(SunBeamProjectile animatable) {
        return JCraft.id("animations/sunbeam.animation.json");
    }

}
