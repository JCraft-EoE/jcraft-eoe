package net.arna.jcraft.client.model.entity.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.spec.BrawlerSpecUser;
import net.minecraft.resources.ResourceLocation;

public class BrawlerSpecUserModel extends SpecUserModel<BrawlerSpecUser> {
    private static final ResourceLocation model = JCraft.id("geo/humanoid.geo.json");
    private static final ResourceLocation texture = JCraft.id("textures/entity/jonathan.png");

    @Override
    public ResourceLocation getModelResource(final BrawlerSpecUser animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(final BrawlerSpecUser animatable) {
        return texture;
    }

}
