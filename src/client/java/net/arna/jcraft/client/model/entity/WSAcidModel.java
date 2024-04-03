package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WSAcidModel extends GeoModel<WSAcidProjectile> {
    @Override
    public Identifier getModelResource(WSAcidProjectile object) {
        return JCraft.id("geo/wsacid.geo.json");
    }

    @Override
    public Identifier getTextureResource(WSAcidProjectile object) {
        return JCraft.id("textures/entity/projectiles/wsacid.png");
    }

    @Override
    public Identifier getAnimationResource(WSAcidProjectile animatable) {
        return JCraft.id("animations/wsacid.animation.json");
    }

}
