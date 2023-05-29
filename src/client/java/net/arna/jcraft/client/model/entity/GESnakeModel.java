package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GESnakeEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GESnakeModel extends AnimatedGeoModel<GESnakeEntity> {
    @Override
    public Identifier getModelResource(GESnakeEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/gesnake.geo.json");
    }

    @Override
    public Identifier getTextureResource(GESnakeEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/gesnake.png");
    }

    @Override
    public Identifier getAnimationResource(GESnakeEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/gesnake.animation.json");
    }
}
