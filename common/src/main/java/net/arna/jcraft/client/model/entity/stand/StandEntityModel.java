package net.arna.jcraft.client.model.entity.stand;

import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The base Model for stands of any {@link StandType}.
 * @param <E> the entity to model
 * @see net.arna.jcraft.client.renderer.entity.stands.StandEntityRenderer StandEntityRenderer
 */
public class StandEntityModel<E extends StandEntity<?, ?>> {
    private final StandType type;
    private final ResourceLocation model;
    private final List<ResourceLocation> skins;
    private final ResourceLocation animation;
    private final float torsoPitchOffset, headPitchOffset, velInfluence;
    // public float prevTorsoPitch, prevHeadPitch, prevBasePitch = 0.0f;

    public StandEntityModel(final StandType type) {
        this(type, 0f, 0f);
    }

    public StandEntityModel(final StandType type, final float torsoPitchOffset, final float headPitchOffset) {
        this(type, torsoPitchOffset, headPitchOffset, 90f);
    }

    public StandEntityModel(final StandType type, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        this.type = type;
        model = type.getId().withPath(path -> "geo/" + path + ".geo.json");
        skins = IntStream.rangeClosed(0, 3)
                .mapToObj(i -> type.getId().withPath(path -> String.format("textures/entity/stands/%s/%s.png",
                        path, i == 0 ? "default" : "skin" + i)))
                .toList();
        animation = type.getId().withPath(path -> "animations/" + path + ".animation.json");

        this.torsoPitchOffset = torsoPitchOffset;
        this.headPitchOffset = headPitchOffset;
        this.velInfluence = velInfluence;
    }
    /*
    @Override
    public ResourceLocation getModelResource(final E entity) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(final E entity) {
        return skins.get(Mth.clamp(entity.getSkin(), 0, type.getData().getInfo().getSkinCount()));
    }

    @Override
    public ResourceLocation getAnimationResource(final E entity) {
        return animation;
    }

    @Override
    public void setCustomAnimations(final E animatable, final long instanceId, final AnimationState<E> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (skipCustomAnimations() || !animatable.hasUser()) {
            return;
        }

        // Note: KingCrimsonEntity overrides this due to some inversions, we should probably change the model later and standardize him.
        JClientUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationState.getPartialTick(),
                true, true, torsoPitchOffset, headPitchOffset, velInfluence);
    }*/

    protected boolean skipCustomAnimations() {
        return false;
    }
}
