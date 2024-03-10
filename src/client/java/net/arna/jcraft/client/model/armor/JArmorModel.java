package net.arna.jcraft.client.model.armor;

import net.arna.jcraft.JCraft;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class JArmorModel<T extends ArmorItem & IAnimatable> extends AnimatedGeoModel<T> {
    protected final String name;
    
    public JArmorModel(String name) {
        this.name = name;
    }

    @Override
    public Identifier getModelResource(T object) {
        return JCraft.id("geo/" + name + ".geo.json");
    }

    @Override
    public Identifier getTextureResource(T object) {
        return JCraft.id("textures/armor/" + name + ".png");
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return JCraft.id("animations/" + name + ".animation.json");
    }
}
