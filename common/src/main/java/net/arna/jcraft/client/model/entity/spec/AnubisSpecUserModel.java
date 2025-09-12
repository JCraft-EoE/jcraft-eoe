package net.arna.jcraft.client.model.entity.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.spec.AnubisSpecUser;
import net.minecraft.resources.ResourceLocation;

public class AnubisSpecUserModel extends SpecUserModel<AnubisSpecUser> {
    private static final ResourceLocation model = JCraft.id("geo/chaka.geo.json");
    private static final ResourceLocation texture = JCraft.id("textures/entity/chaka.png");

    @Override
    public ResourceLocation getModelResource(final AnubisSpecUser animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(final AnubisSpecUser animatable) {
        return texture;
    }

}
