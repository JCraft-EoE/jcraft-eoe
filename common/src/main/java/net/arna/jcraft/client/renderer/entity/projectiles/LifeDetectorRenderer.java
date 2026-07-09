package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.BaseModelRenderer;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.projectile.LifeDetectorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The {@link ProjectileRenderer} for {@link LifeDetectorEntity}.
 */
@Environment(EnvType.CLIENT)
public class LifeDetectorRenderer extends AbstractEntityRenderer<LifeDetectorEntity> {

    public static final String ID = "detector";
    private static final RenderType RENDER_TYPE = RenderType.eyes(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public LifeDetectorRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE)
                .setModelRenderer((pc, layer) -> new BaseModelRenderer<>((AzEntityRendererPipeline<LifeDetectorEntity>) pc, layer) {
                    @Override
                    protected void midRender(@NonNull AzRendererPipelineContext<UUID, LifeDetectorEntity> pc) {
                        ProjectileModelRenderer.faceRotationInverted(pc.poseStack(), pc.animatable(), pc.partialTick());
                    }
                }),
                ID);
    }

    @Override
    public boolean shouldShowName(@NotNull LifeDetectorEntity entity) {
        return false;
    }

    @Override
    public int getBlockLightLevel(final @NonNull LifeDetectorEntity entity, final @NonNull BlockPos pos) {
        return 15;
    }
}
