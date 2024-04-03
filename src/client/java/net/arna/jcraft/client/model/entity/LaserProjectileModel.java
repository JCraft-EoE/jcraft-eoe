package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.LaserProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class LaserProjectileModel extends GeoModel<LaserProjectile> {
    @Override
    public Identifier getModelResource(LaserProjectile object) {
        return JCraft.id("geo/laser.geo.json");
    }

    @Override
    public Identifier getTextureResource(LaserProjectile object) {
        return JCraft.id("textures/entity/projectiles/laser.png");
    }

    @Override
    public Identifier getAnimationResource(LaserProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }

}
