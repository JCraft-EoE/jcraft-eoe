package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.SandTornadoEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class SandTornadoModel extends GeoModel<SandTornadoEntity> {
    @Override
    public Identifier getModelResource(SandTornadoEntity object) {
        return JCraft.id("geo/sandtornado.geo.json");
    }

    @Override
    public Identifier getTextureResource(SandTornadoEntity object) {
        return JCraft.id("textures/entity/sandtornado.png");
    }

    @Override
    public Identifier getAnimationResource(SandTornadoEntity animatable) {
        return JCraft.id("animations/sandtornado.animation.json");
    }
}
