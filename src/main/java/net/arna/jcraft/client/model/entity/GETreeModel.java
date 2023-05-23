package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.GETreeEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GETreeModel extends AnimatedGeoModel<GETreeEntity> {
    @Override
    public Identifier getModelResource(GETreeEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/getree.geo.json");
    }
    @Override
    public Identifier getTextureResource(GETreeEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/getree.png");
    }
    @Override
    public Identifier getAnimationResource(GETreeEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/getree.animation.json");
    }

}
