package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.AnkhProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnkhModel extends AnimatedGeoModel<AnkhProjectile> {
    @Override
    public Identifier getModelResource(AnkhProjectile object) {
        return new Identifier(JCraft.MOD_ID, "geo/ankh.geo.json");
    }

    @Override
    public Identifier getTextureResource(AnkhProjectile object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/projectiles/ankh.png");
    }

    @Override
    public Identifier getAnimationResource(AnkhProjectile animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/knife.animation.json");
    }

}
