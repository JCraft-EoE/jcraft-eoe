package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GERScorpionModel extends AnimatedGeoModel<GERScorpionEntity> {
    @Override
    public Identifier getModelResource(GERScorpionEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/gerscorpion.geo.json");
    }

    @Override
    public Identifier getTextureResource(GERScorpionEntity object) {
        return object.isRock() ? new Identifier(JCraft.MOD_ID, "textures/entity/rock.png") : new Identifier(JCraft.MOD_ID, "textures/entity/gerscorpion.png");
    }

    @Override
    public Identifier getAnimationResource(GERScorpionEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/gerscorpion.animation.json");
    }

}
