package net.arna.jcraft.client.model.tile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.tile.CoffinTileEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class CoffinModel extends AnimatedGeoModel<CoffinTileEntity> {
    @Override
    public Identifier getAnimationResource(CoffinTileEntity entity) {
        return JCraft.id("animations/coffin.animation.json");
    }

    @Override
    public Identifier getModelResource(CoffinTileEntity animatable) {
        return JCraft.id("geo/coffin.geo.json");
    }

    @Override
    public Identifier getTextureResource(CoffinTileEntity entity) {
        return JCraft.id("textures/block/coffin.png");
    }
}