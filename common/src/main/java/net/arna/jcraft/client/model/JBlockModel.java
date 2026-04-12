package net.arna.jcraft.client.model;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class JBlockModel<T extends BlockEntity> {
    private final ResourceLocation model, texture, animation;

    public JBlockModel(@NonNull String name) {
        model = JCraft.id("geo/" + name + ".geo.json");
        texture = JCraft.id("textures/block/" + name + ".png");
        animation = JCraft.id("animations/" + name + ".animation.json");
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
