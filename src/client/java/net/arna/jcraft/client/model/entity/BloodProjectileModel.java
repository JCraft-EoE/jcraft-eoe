package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.BloodProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class BloodProjectileModel extends GeoModel<BloodProjectile> {
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
