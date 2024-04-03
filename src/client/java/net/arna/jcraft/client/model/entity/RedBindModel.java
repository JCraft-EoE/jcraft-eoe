package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.RedBindEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RedBindModel extends GeoModel<RedBindEntity> {
    @Override
    public Identifier getModelResource(RedBindEntity object) {
        return JCraft.id("geo/red_bind.geo.json");
    }

    @Override
    public Identifier getTextureResource(RedBindEntity object) {
        return JCraft.id("textures/entity/red_bind.png");
    }

    @Override
    public Identifier getAnimationResource(RedBindEntity animatable) {
        return JCraft.id("animations/red_bind.animation.json");
    }

}
