package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.MeteorProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MeteorModel extends AnimatedGeoModel<MeteorProjectile> {
    @Override
    public Identifier getModelResource(MeteorProjectile object) {
        return JCraft.id("geo/meteor.geo.json");
    }

    @Override
    public Identifier getTextureResource(MeteorProjectile object) {
        return JCraft.id("textures/entity/meteor/skin_" + object.getSkin() + ".png");
    }

    @Override
    public Identifier getAnimationResource(MeteorProjectile animatable) {
        return JCraft.id("animations/meteor.animation.json");
    }
}
