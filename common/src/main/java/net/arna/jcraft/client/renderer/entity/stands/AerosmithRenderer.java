package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.renderer.entity.StandEntityModelRenderer;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer.ANIMATION_STR_TEMPLATE;
import static net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer.MODEL_STR_TEMPLATE;
import static net.arna.jcraft.client.renderer.entity.stands.StandEntityRenderer.*;

/**
 * The {@link StandEntityRenderer} for {@link AerosmithEntity}.
 */
@Environment(EnvType.CLIENT)
public class AerosmithRenderer {
    public static StandEntityRenderer<AerosmithEntity> of(final @NonNull EntityRendererProvider.Context context) {
        final StandType type = JStandTypeRegistry.AEROSMITH.get();
        final var id = type.getId().getPath();
        final var animation = JCraft.id(ANIMATION_STR_TEMPLATE.formatted(id));
        final var model = type.getId().withPath(MODEL_STR_TEMPLATE.formatted(id));
        final var texture = getTextureLocation(type);

        return StandEntityRenderer.of(
                AzEntityRendererConfig
                        .<AerosmithEntity>builder(model, texture)
                        .setAnimatorProvider(() -> new AerosmithAnimator(animation))
                        .setModelRenderer((ctx, layerRenderer) -> new AerosmithModelRenderer(ctx, layerRenderer, context.getItemRenderer()))
                        .setRenderType(renderType())
                        .setPrerenderEntry(preRenderEntry())
                        .build(),
                context,
                model,
                texture
        );
    }

    public static class AerosmithAnimator extends StandAnimator<AerosmithEntity> {
        public AerosmithAnimator(final @NonNull ResourceLocation animation) {
            super(animation, false, false, 0f, 0f, 0f);
        }
    }

    public static class AerosmithModelRenderer extends StandEntityModelRenderer<AerosmithEntity> {
        private final ItemRenderer itemRenderer;

        public AerosmithModelRenderer(
                final @NonNull AzRendererPipeline<UUID, AerosmithEntity> entityRendererPipeline,
                final @NonNull AzLayerRenderer<UUID, AerosmithEntity> layerRenderer,
                final @NonNull ItemRenderer itemRenderer) {
            super(entityRendererPipeline, layerRenderer);
            this.itemRenderer = itemRenderer;
        }

        @Override
        public void render(@NonNull AzRendererPipelineContext<UUID, AerosmithEntity> ctx, boolean isReRender) {
            final var animatable = ctx.animatable();
            final var partialTick = ctx.partialTick();

            if (StandEntityRenderer.getAlpha(animatable, partialTick) <= 0.0f) {
                return;
            }

            if (!isReRender) {
                final var entity = ctx.animatable();

                if (entity != null) {
                    final var poseStack = ctx.poseStack();
                    final var item = entity.getHeldItem();
                    if (!item.isEmpty()) {
                        poseStack.pushPose();
                        poseStack.translate(0f, -1f, 0f);
                        itemRenderer.renderStatic(
                                item,
                                ItemDisplayContext.GROUND,
                                ctx.packedLight(),
                                OverlayTexture.NO_OVERLAY,
                                poseStack,
                                ctx.multiBufferSource(),
                                entity.level(),
                                entity.getId()
                        );
                        poseStack.popPose();
                    }
                }
            }

            final var poseStack = ctx.poseStack();

            poseStack.pushPose();
            final float lerpBodyRot = getStandLerpRot(animatable, partialTick);
            final float nativeScale = animatable.getScale();
            final float ageInTicks = animatable.tickCount + partialTick;

            poseStack.scale(nativeScale, nativeScale, nativeScale);

            if (animatable.isRemote()) {
                final Vec3 vel = animatable.getDeltaMovement();

                final double horizontalDistance = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                final float targetYaw = (float) (Math.atan2(-vel.x, vel.z) * (180D / Math.PI)) + 180.0f;
                final float targetPitch = (float) (Math.atan2(-vel.y, horizontalDistance) * (180D / Math.PI));

                final float lerpSpeed = 0.15f;

                animatable.yaw = Mth.rotLerp(lerpSpeed, animatable.yaw, targetYaw);
                animatable.pitch = Mth.rotLerp(lerpSpeed, animatable.pitch, targetPitch);

                poseStack.mulPose(Axis.YN.rotationDegrees(Mth.rotLerp(partialTick, animatable.oldYaw, animatable.yaw)));
                poseStack.mulPose(Axis.XN.rotationDegrees(Mth.rotLerp(partialTick, animatable.oldPitch, animatable.pitch)));
                poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(partialTick, animatable.oldRoll, animatable.roll)));

                animatable.oldPitch = animatable.pitch;
                animatable.oldYaw = animatable.yaw;
                animatable.oldRoll = animatable.roll;
            } else {
                applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);
            }

            if (!isReRender) {
                final var animator = entityRendererPipeline.getRenderer().getAnimator();

                if (animator != null) {
                    handleAnimation(animator, animatable, ctx.partialTick());
                }
            }

            entityRendererPipeline.modelRenderTranslations.set(poseStack.last().pose());

            if (ctx.vertexConsumer() != null) { // actually render
                ctx.rendererPipeline().updateAnimatedTextureFrame(animatable);

                for (var bone : ctx.bakedModel().getTopLevelBones()) {
                    renderRecursively(ctx, bone, isReRender);
                }

                var config = ctx.rendererPipeline().config();
                config.renderEntry(ctx);
            }

            poseStack.popPose();
        }
    }
}
