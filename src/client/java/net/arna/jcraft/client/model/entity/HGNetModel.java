package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HGNetModel extends AnimatedGeoModel<HGNetEntity> {
    @Override
    public Identifier getModelResource(HGNetEntity object) {
        return JCraft.id("geo/hg_nets.geo.json");
    }

    @Override
    public Identifier getTextureResource(HGNetEntity object) {
        return JCraft.id("textures/entity/hg_nets.png");
    }

    @Override
    public Identifier getAnimationResource(HGNetEntity animatable) {
        return JCraft.id("animations/hg_nets.animation.json");
    }

}
