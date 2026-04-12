package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.BaseModelRenderer;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.projectile.LargeIcicleProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

import java.util.UUID;

/**
 * The {@link AbstractEntityRenderer} for {@link LargeIcicleProjectile}.
 */
@Environment(EnvType.CLIENT)
public class LargeIcicleRenderer extends ProjectileRenderer<LargeIcicleProjectile> {

    public static final String ID = "large_icicle";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public LargeIcicleRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE)
                .setRenderEntry(contextPipeline -> {
                    final var animatable = contextPipeline.animatable();

                    if (animatable.isInstant()) {
                        LargeIcicleProjectile.FIRE_INSTANT.sendForEntity(animatable);
                    } else {
                        LargeIcicleProjectile.FIRE.sendForEntity(animatable);
                    }

                    return contextPipeline;
                })
                .setModelRenderer((pc, layer) -> new BaseModelRenderer<>((AzEntityRendererPipeline<LargeIcicleProjectile>) pc, layer) {
                    @Override
                    protected void midRender(@NonNull AzRendererPipelineContext<UUID, LargeIcicleProjectile> pc) {
                        var poseStack = pc.poseStack();
                        var animatable = pc.animatable();
                        var partialTick = pc.partialTick();

                        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90.0f));
                        poseStack.mulPose(Axis.ZN.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));

                        final float scale = animatable.getScale();
                        poseStack.scale(scale, scale, scale);
                    }
                }),
                ID);
    }
}
