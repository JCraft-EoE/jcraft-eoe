package net.arna.jcraft.client.model.entity.spec;

import lombok.NonNull;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class SpecUserModel<T extends SpecUserMob> extends GeoModel<T> {
    protected static final ResourceLocation GENERIC_ANIMATIONS = JCraft.id("animations/spec/spec_user.animation.json");

    @Override
    public ResourceLocation getAnimationResource(final T animatable) {
        return GENERIC_ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(final @NonNull T animatable, final long instanceId, final AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        this.getBone("head").ifPresent(head -> {
            head.setRotX(-animatable.getXRot() * Mth.DEG_TO_RAD);
            head.setRotY((animatable.getYRot() - animatable.getViewYRot(animationState.getPartialTick())) * Mth.DEG_TO_RAD);
        });
    }
}
