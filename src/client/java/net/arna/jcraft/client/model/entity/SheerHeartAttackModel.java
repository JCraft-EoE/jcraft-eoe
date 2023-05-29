package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class SheerHeartAttackModel extends AnimatedTickingGeoModel<SheerHeartAttackEntity> {

    @Override
    public Identifier getModelResource(SheerHeartAttackEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/sha.geo.json");
    }

    @Override
    public Identifier getTextureResource(SheerHeartAttackEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/sha.png");
    }

    @Override
    public Identifier getAnimationResource(SheerHeartAttackEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/sha.animation.json");
    }
}