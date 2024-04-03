package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GETreeModel extends GeoModel<GETreeEntity> {
    @Override
    public Identifier getModelResource(GETreeEntity object) {
        return JCraft.id("geo/getree.geo.json");
    }

    @Override
    public Identifier getTextureResource(GETreeEntity object) {
        return JCraft.id("textures/entity/getree.png");
    }

    @Override
    public Identifier getAnimationResource(GETreeEntity animatable) {
        return JCraft.id("animations/getree.animation.json");
    }

}
