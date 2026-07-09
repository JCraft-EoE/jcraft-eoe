package net.arna.jcraft.client.model;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

public class JProjectileModel<T> {
    private final ResourceLocation model, texture, animation;

    public JProjectileModel(@NonNull String name) {
        this(name, false);
    }
    public JProjectileModel(@NonNull String name, final boolean hasAnimation) {
        model = JCraft.id("geo/" + name + ".geo.json");
        texture = JCraft.id("textures/entity/projectiles/" + name + ".png");
        animation = hasAnimation ? JCraft.id("animations/" + name + ".animation.json") : JCraft.id("animations/knife.animation.json");
    }

    /*@Override
    public @NonNull ResourceLocation getModelResource(T animatable) {
        return model;
    }

    @Override
    public @NonNull ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    @Override
    public @NonNull ResourceLocation getAnimationResource(T animatable) {
        return animation;
    }*/
}
