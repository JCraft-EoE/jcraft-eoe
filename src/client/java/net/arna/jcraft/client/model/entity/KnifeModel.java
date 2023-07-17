package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.KnifeProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KnifeModel extends AnimatedGeoModel<KnifeProjectile> {
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
