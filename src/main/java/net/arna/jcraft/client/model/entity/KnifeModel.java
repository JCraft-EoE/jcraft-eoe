package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.KnifeProjectile;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KnifeModel extends AnimatedGeoModel<KnifeProjectile> {
    @Override
    public Identifier getModelResource(KnifeProjectile object) {
        return new Identifier(JCraft.MOD_ID, "geo/knife.geo.json");
    }

    @Override
    public Identifier getTextureResource(KnifeProjectile object) {
        return (object.getLightning()) ? new Identifier(JCraft.MOD_ID, "textures/entity/projectiles/lknife.png") : new Identifier(JCraft.MOD_ID, "textures/entity/projectiles/knife.png");
    }

    @Override
    public Identifier getAnimationResource(KnifeProjectile animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/knife.animation.json");
    }

}
