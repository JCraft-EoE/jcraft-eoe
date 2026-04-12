package net.arna.jcraft.client.model.armor;

import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

public class JArmorModel {//<T extends GeoAnimatable> extends GeoModel<T> {//<T extends ArmorItem & IAnimatable> extends AnimatedGeoModel<T> {
    protected final String modelName;
    protected final String textureName;

    public JArmorModel(final String name) {
        this(name, name);
    }

    public JArmorModel(final String modelName, final String textureName) {
        this.modelName = modelName;
        this.textureName = textureName;
    }

    /*
    @Override
    public ResourceLocation getModelResource(final T object) {
        return JCraft.id("geo/" + modelName + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(final T object) {
        return JCraft.id("textures/armor/" + textureName + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(final T animatable) {
        return JCraft.id("animations/" + modelName + ".animation.json");
    }
    */
}
