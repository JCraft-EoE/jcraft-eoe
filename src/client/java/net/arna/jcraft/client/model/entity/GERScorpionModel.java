package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GERScorpionModel extends GeoModel<GERScorpionEntity> {
    @Override
    public Identifier getModelResource(GERScorpionEntity object) {
        return JCraft.id("geo/gerscorpion.geo.json");
    }

    @Override
    public Identifier getTextureResource(GERScorpionEntity object) {
        return object.isRock() ? JCraft.id("textures/entity/rock.png") : JCraft.id("textures/entity/gerscorpion.png");
    }

    @Override
    public Identifier getAnimationResource(GERScorpionEntity animatable) {
        return JCraft.id("animations/gerscorpion.animation.json");
    }

}
