package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityModelRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ProjectileModelRenderer<T extends AbstractArrow> extends AzEntityModelRenderer<T> {

    public ProjectileModelRenderer(final @NonNull AzEntityRendererPipeline<T> pipeline, final @NonNull AzLayerRenderer<UUID, T> layerRenderer) {
        super(pipeline, layerRenderer);
    }

    public static void lookAt(PoseStack poseStack, AbstractArrow animatable, float partialTick) {
        float yaw = Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot());
        float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());

        if (animatable.noPhysics) {
            yaw += 180.0F; // <- minecraft sucks
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
    }

    public static void faceRotationInverted(PoseStack poseStack, Entity animatable, float partialTick) {
        float yaw = Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90.0F;
        float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());

        poseStack.mulPose(Axis.YN.rotationDegrees(yaw)); // YN as opposed to YP
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
    }

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, T> pc, final boolean isReRender) {
        var animatable = pc.animatable();
        var partialTick = pc.partialTick();
        var poseStack = pc.poseStack();

        poseStack.pushPose();

        lookAt(poseStack, animatable, partialTick);

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
