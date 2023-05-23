package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.BloodProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BloodProjectileModel extends AnimatedGeoModel<BloodProjectile> {
    @Override
    public Identifier getModelResource(BloodProjectile object) {
        return new Identifier(JCraft.MOD_ID, "geo/bloodprojectile.geo.json");
    }

    @Override
    public Identifier getTextureResource(BloodProjectile object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/projectiles/bloodprojectile.png");
    }

    @Override
    public Identifier getAnimationResource(BloodProjectile animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/knife.animation.json");
    }

}
