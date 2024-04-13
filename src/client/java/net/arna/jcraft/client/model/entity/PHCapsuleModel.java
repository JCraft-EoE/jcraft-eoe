package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.PHCapsuleProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PHCapsuleModel extends AnimatedGeoModel<PHCapsuleProjectile> {
    @Override
    public Identifier getModelResource(PHCapsuleProjectile object) {
        return JCraft.id("geo/ph_capsule.geo.json");
    }

    @Override
    public Identifier getTextureResource(PHCapsuleProjectile object) {
        return JCraft.id("textures/entity/projectiles/ph_capsule.png");
    }

    @Override
    public Identifier getAnimationResource(PHCapsuleProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }
}
