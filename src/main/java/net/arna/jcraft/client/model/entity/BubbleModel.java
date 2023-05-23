package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.BubbleProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BubbleModel extends AnimatedGeoModel<BubbleProjectile> {
    @Override
    public Identifier getModelResource(BubbleProjectile object) {
        return new Identifier(JCraft.MOD_ID, "geo/bubble.geo.json");
    }

    @Override
    public Identifier getTextureResource(BubbleProjectile object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/projectiles/bubble.png");
    }

    @Override
    public Identifier getAnimationResource(BubbleProjectile animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/bubble.animation.json");
    }

}
