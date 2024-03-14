package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.EmeraldProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class EmeraldModel extends AnimatedGeoModel<EmeraldProjectile> {
    @Override
    public Identifier getModelResource(EmeraldProjectile object) {
        return JCraft.id("geo/emerald.geo.json");
    }

    @Override
    public Identifier getTextureResource(EmeraldProjectile object) {
        return JCraft.id("textures/entity/projectiles/emerald.png");
    }

    @Override
    public Identifier getAnimationResource(EmeraldProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }

}
