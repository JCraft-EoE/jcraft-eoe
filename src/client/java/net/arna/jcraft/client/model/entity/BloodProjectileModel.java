package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.BloodProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BloodProjectileModel extends AnimatedGeoModel<BloodProjectile> {
    @Override
    public Identifier getModelResource(BloodProjectile object) {
        return JCraft.id("geo/bloodprojectile.geo.json");
    }

    @Override
    public Identifier getTextureResource(BloodProjectile object) {
        return JCraft.id("textures/entity/projectiles/bloodprojectile.png");
    }

    @Override
    public Identifier getAnimationResource(BloodProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }

}
