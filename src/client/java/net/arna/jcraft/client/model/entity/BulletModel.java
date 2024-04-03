package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.BulletProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class BulletModel extends GeoModel<BulletProjectile> {
    @Override
    public Identifier getModelResource(BulletProjectile object) {
        return JCraft.id("geo/bullet.geo.json");
    }

    @Override
    public Identifier getTextureResource(BulletProjectile object) {
        return JCraft.id("textures/entity/projectiles/bullet.png");
    }

    @Override
    public Identifier getAnimationResource(BulletProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }

}
