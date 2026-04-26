package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.BaseModelRenderer;
import net.arna.jcraft.common.entity.projectile.FiredIcicleProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class FiredIcicleRenderer extends ProjectileRenderer<FiredIcicleProjectile> {

    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(
            JCraft.id(TEXTURE_STR_TEMPLATE.formatted(LargeIcicleRenderer.ID)));

    public FiredIcicleRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(LargeIcicleRenderer.ID), b -> b
                .setRenderType(RENDER_TYPE)
                .setRenderEntry(contextPipeline -> {
                    FiredIcicleProjectile.FIRE.sendForEntity(contextPipeline.animatable());
                    return contextPipeline;
                })
                .setModelRenderer((pc, layer) -> new BaseModelRenderer<>((AzEntityRendererPipeline<FiredIcicleProjectile>) pc, layer) {
                    @Override
                    protected void midRender(@NonNull AzRendererPipelineContext<UUID, FiredIcicleProjectile> pc) {
                        final var poseStack = pc.poseStack();
                        final var animatable = pc.animatable();
                        final var partialTick = pc.partialTick();

                        // +90 instead of -90: AbstractArrow sets yRot via atan2(x,z) which is 180° offset
                        // from Minecraft's player yaw convention, so +90 corrects for the model's -X forward axis
                        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) + 90.0f));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));

                        final float scale = animatable.getScale();
                        poseStack.scale(scale, scale, scale);
                    }
                }),
                LargeIcicleRenderer.ID);
    }
}
