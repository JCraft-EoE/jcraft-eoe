package net.arna.jcraft.client.renderer.entity;

import lombok.NonNull;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityModelRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.client.model.entity.stand.StandEntityModel;
import net.arna.jcraft.client.renderer.entity.stands.StandEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class StandEntityModelRenderer<T extends StandEntity<?, ?>> extends AzEntityModelRenderer<T> {

    public StandEntityModelRenderer(final @NonNull AzRendererPipeline<UUID,T> entityRendererPipeline, final @NonNull AzLayerRenderer<UUID, T> layerRenderer) {
        super((AzEntityRendererPipeline<T>)entityRendererPipeline, layerRenderer);
    }

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, T> pc, final boolean isReRender) {
        final var animatable = pc.animatable();
        final var partialTick = pc.partialTick();

        if (StandEntityRenderer.getAlpha(animatable, partialTick) <= 0.0f) {
            return;
        }

        final var poseStack = pc.poseStack();

        poseStack.pushPose();
        final float lerpBodyRot = getStandLerpRot(animatable, partialTick);

        if (animatable.getPose() == Pose.SLEEPING) {
            final Direction bedDirection = animatable.getBedOrientation();

            if (bedDirection != null) {
                float eyePosOffset = animatable.getEyeHeight(Pose.STANDING) - 0.1F;

                poseStack.translate(
                        -bedDirection.getStepX() * eyePosOffset,
                        0,
                        -bedDirection.getStepZ() * eyePosOffset
                );
            }
        }

        final float nativeScale = animatable.getScale();
        final float ageInTicks = animatable.tickCount + partialTick;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);

        if (!isReRender) {
            final var animator = entityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                handleAnimation(animator, animatable, pc.partialTick());
            }
        }

        entityRendererPipeline.modelRenderTranslations.set(poseStack.last().pose());

        if (pc.vertexConsumer() != null) { // actually render
            pc.rendererPipeline().updateAnimatedTextureFrame(animatable);

            for (var bone : pc.bakedModel().getTopLevelBones()) {
                renderRecursively(pc, bone, isReRender);
            }

            var config = pc.rendererPipeline().config();
            config.renderEntry(pc);
        }

        poseStack.popPose();
    }

    private static <T extends StandEntity<?, ?>> float getStandLerpRot(final @NonNull T animatable, final float partialTick) {
        final LivingEntity user = animatable.getUser();
        final boolean hasUser = user != null;
        if (hasUser && !animatable.isRemote()) {
            return Mth.rotLerp(
                    partialTick,
                    user.yHeadRotO,
                    user.yHeadRot
            );
        }
        return Mth.rotLerp(
                partialTick,
                animatable.yBodyRotO,
                animatable.yBodyRot
        );
    }
}
