package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GESnakeEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GESnakeModel extends GeoModel<GESnakeEntity> {
    @Override
    public Identifier getModelResource(GESnakeEntity object) {
        return JCraft.id("geo/gesnake.geo.json");
    }

    @Override
    public Identifier getTextureResource(GESnakeEntity object) {
        return JCraft.id("textures/entity/gesnake.png");
    }

    @Override
    public Identifier getAnimationResource(GESnakeEntity animatable) {
        return JCraft.id("animations/gesnake.animation.json");
    }
}
