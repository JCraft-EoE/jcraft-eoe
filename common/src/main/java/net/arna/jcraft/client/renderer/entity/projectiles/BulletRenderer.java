package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.client.model.JProjectileModel;
import net.arna.jcraft.common.entity.projectile.BulletProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * The {@link ProjectileRenderer} for {@link BulletProjectile}.
 */
@Environment(EnvType.CLIENT)
public class BulletRenderer extends ProjectileRenderer<BulletProjectile> {

    public BulletRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, "bullet"); // 3x1x1 px cuboid model
    }

    /*
    @Override
    public RenderType getRenderType(final BulletProjectile animatable, final ResourceLocation texture, final MultiBufferSource bufferSource, final float partialTick) {
        return RenderType.entitySolid(texture);
    }
    */

    @Override
    public void render(final BulletProjectile animatable, final float yaw, final float partialTick, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight) {
        poseStack.pushPose();
        final float scale = animatable.getCaliber() * 0.016f; // 62.5mm/px
        poseStack.scale(scale, scale, scale);
        super.render(animatable, yaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
