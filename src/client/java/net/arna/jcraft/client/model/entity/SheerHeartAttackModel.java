package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class SheerHeartAttackModel extends GeoModel<SheerHeartAttackEntity> {

    @Override
    public Identifier getModelResource(SheerHeartAttackEntity object) {
        return JCraft.id("geo/sha.geo.json");
    }

    @Override
    public Identifier getTextureResource(SheerHeartAttackEntity object) {
        return JCraft.id("textures/entity/sha.png");
    }

    @Override
    public Identifier getAnimationResource(SheerHeartAttackEntity animatable) {
        return JCraft.id("animations/sha.animation.json");
    }
}