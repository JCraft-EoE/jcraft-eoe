package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link ProjectileRenderer} for {@link NailProjectile}.
 */
@Environment(EnvType.CLIENT)
public class NailProjectileRenderer extends ProjectileRenderer<NailProjectile> {

    public NailProjectileRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, "fingernail"); // Uses nail.geo.json model
    }

    @Override
    public void render(final NailProjectile animatable, final float yaw, final float partialTick, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight) {
        poseStack.pushPose();

        // Scale the nail projectile
        final float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        super.render(animatable, yaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}