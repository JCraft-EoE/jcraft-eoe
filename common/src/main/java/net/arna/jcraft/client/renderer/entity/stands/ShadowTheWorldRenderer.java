package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.renderer.entity.StandEntityModelRenderer;
import net.arna.jcraft.client.renderer.entity.layer.STWGlowLayer;
import net.arna.jcraft.common.entity.stand.ShadowTheWorldEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * The {@link StandEntityRenderer} for {@link ShadowTheWorldEntity}.
 */

public class ShadowTheWorldRenderer extends StandEntityRenderer<ShadowTheWorldEntity> {
    private ShadowTheWorldRenderer(EntityRendererProvider.@NonNull Context context, @NonNull StandType type) {
        super(context, type);
    }

    public static StandEntityRenderer<ShadowTheWorldEntity> of(final @NonNull EntityRendererProvider.Context context) {
        final StandType type = JStandTypeRegistry.SHADOW_THE_WORLD.get();
        final var id = type.getId().getPath();
        final var animation = JCraft.id(ANIMATION_STR_TEMPLATE.formatted(id));
        final var model = type.getId().withPath(MODEL_STR_TEMPLATE.formatted(id));
        final var texture = getTextureLocation(type);

        return StandEntityRenderer.of(
                AzEntityRendererConfig
                        .<ShadowTheWorldEntity>builder(model, texture)
                        .addRenderLayer(new STWGlowLayer())
                        .setAnimatorProvider(() -> new STWAnimator(animation, false, false, -0.1745329251f, -0.1745329251f, 90f))
                        .setModelRenderer(StandEntityModelRenderer::new)
                        .setRenderType(renderType())
                        .setPrerenderEntry(preRenderEntry())
                        .build(),
                context,
                model,
                texture
        );
    }

    private static class STWAnimator extends StandAnimator<ShadowTheWorldEntity> {
        public STWAnimator(final @NonNull ResourceLocation animation, boolean flipBody, boolean flipHead, float torsoPitchOffset, float headPitchOffset, float velInfluence) {
            super(animation, flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence);
        }

        @Override
        public void registerControllers(@NonNull AzAnimationControllerContainer<ShadowTheWorldEntity> animationControllerContainer) {
            animationControllerContainer.add(AzAnimationController.builder(this, ShadowTheWorldEntity.DESUMMON_CONTROLLER).setTransitionLength(0).build());
            super.registerControllers(animationControllerContainer);
        }
    }
}
