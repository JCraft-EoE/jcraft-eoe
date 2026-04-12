package net.arna.jcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.common.entity.TrainingDummyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer for the {@link TrainingDummyEntity} using AzureLib
 */
@Environment(EnvType.CLIENT)
public class TrainingDummyRenderer extends AbstractEntityRenderer<TrainingDummyEntity> {

    public static final String ID = "training_dummy";

    public TrainingDummyRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), ID);
    }

    @Override
    public void render(TrainingDummyEntity entity, float entityYaw, float partialTick, @NonNull PoseStack poseStack,
                       @NonNull MultiBufferSource bufferSource, int packedLight) {
        // Don't render if invisible
        if (entity.isInvisible()) {
            return;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRender(TrainingDummyEntity entity, @NonNull Frustum frustum,
                                double camX, double camY, double camZ) {
        return !entity.isInvisible() && super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    @Override
    public boolean shouldShowName(TrainingDummyEntity entity) {
        return false;
    }
}