package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class KnifeModel extends GeoModel<KnifeProjectile> {
    @Override
    public Identifier getModelResource(KnifeProjectile object) {
        return JCraft.id("geo/knife.geo.json");
    }

    @Override
    public Identifier getTextureResource(KnifeProjectile object) {
        return (object.getLightning()) ? JCraft.id("textures/entity/projectiles/lknife.png") : JCraft.id("textures/entity/projectiles/knife.png");
    }

    @Override
    public Identifier getAnimationResource(KnifeProjectile animatable) {
        return JCraft.id("animations/knife.animation.json");
    }

}
