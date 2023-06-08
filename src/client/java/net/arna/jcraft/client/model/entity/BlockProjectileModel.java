package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.BlockProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BlockProjectileModel extends AnimatedGeoModel<BlockProjectile> {
    @Override
    public Identifier getModelResource(BlockProjectile object) {
        return new Identifier(JCraft.MOD_ID, "geo/block.geo.json");
    }

    @Override
    public Identifier getTextureResource(BlockProjectile object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/projectiles/block.png");
    }

    @Override
    public Identifier getAnimationResource(BlockProjectile animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/block.animation.json");
    }

}
