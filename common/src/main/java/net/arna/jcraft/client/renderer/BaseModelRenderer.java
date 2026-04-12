package net.arna.jcraft.client.renderer;

import lombok.NonNull;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityModelRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

// renderer class with no rotation pre-processing
public class BaseModelRenderer<T extends Entity> extends AzEntityModelRenderer<T> {

    public BaseModelRenderer(final @NonNull AzEntityRendererPipeline<T> pipeline, final @NonNull AzLayerRenderer<UUID, T> layerRenderer) {
        super(pipeline, layerRenderer);
    }

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, T> pc, final boolean isReRender) {
        var animatable = pc.animatable();
        var poseStack = pc.poseStack();

        poseStack.pushPose();

        midRender(pc);

        if (!isReRender) {
            var animator = entityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                handleAnimation(animator, animatable, pc.partialTick());
            }
        }

        entityRendererPipeline.modelRenderTranslations.set(poseStack.last().pose());

        if (pc.vertexConsumer() != null) {
            var model = pc.bakedModel();

            pc.rendererPipeline().updateAnimatedTextureFrame(animatable);

            for (var bone : model.getTopLevelBones()) {
                renderRecursively(pc, bone, isReRender);
            }

            var config = pc.rendererPipeline().config();
            config.renderEntry(pc);
        }

        poseStack.popPose();
    }

    /**
     * A hook to allow easy manipulation inside the render method.
     */
    protected void midRender(final @NonNull AzRendererPipelineContext<UUID,T> pc) {
        // Left empty on purpose
    }
}