package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnkhModel extends AnimatedGeoModel<AnkhProjectile> {
    @Override
    public Identifier getModelResource(AnkhProjectile object) {
        return JCraft.id("geo/ankh.geo.json");
    }

    @Override
    public Identifier getTextureResource(AnkhProjectile object) {
        return JCraft.id("textures/entity/projectiles/ankh.png");
    }

    @Override
    public Identifier getAnimationResource(AnkhProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }

}
