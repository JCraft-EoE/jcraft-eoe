package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GEButterflyEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GEButterflyModel extends GeoModel<GEButterflyEntity> {
    @Override
    public Identifier getModelResource(GEButterflyEntity object) {
        return JCraft.id("geo/gebutterfly.geo.json");
    }

    @Override
    public Identifier getTextureResource(GEButterflyEntity object) {
        return JCraft.id("textures/entity/gebutterfly.png");
    }

    @Override
    public Identifier getAnimationResource(GEButterflyEntity animatable) {
        return JCraft.id("animations/gebutterfly.animation.json");
    }
}
