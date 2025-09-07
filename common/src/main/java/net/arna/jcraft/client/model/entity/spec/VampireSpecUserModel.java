package net.arna.jcraft.client.model.entity.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.spec.VampireSpecUser;
import net.minecraft.resources.ResourceLocation;

public class VampireSpecUserModel extends SpecUserModel<VampireSpecUser> {
    private static final ResourceLocation model = JCraft.id("geo/humanoid.geo.json");
    private static final ResourceLocation texture = JCraft.id("textures/entity/vampire_spec_user.png");

    @Override
    public ResourceLocation getModelResource(final VampireSpecUser animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(final VampireSpecUser animatable) {
        return texture;
    }

}
