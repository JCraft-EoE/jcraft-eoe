package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link ProjectileRenderer} for {@link BlockProjectile}.
 */
@Environment(EnvType.CLIENT)
public class BlockProjectileRenderer extends AbstractEntityRenderer<BlockProjectile> {
    private final ItemRenderer itemRenderer;

    public static final String ID = "block";
    private static final RenderType RENDER_TYPE = RenderType.eyes(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public BlockProjectileRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                        .setRenderType(RENDER_TYPE),
                ID);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public boolean shouldShowName(@NotNull BlockProjectile entity) {
        return false;
    }

    @Override
    public void render(final BlockProjectile animatable, final float yaw, final float partialTick, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight) {
        poseStack.pushPose();
        //poseStack.multiply(Quaternion.fromEulerXyz(3.1415f, 3.1415f, 0));
        itemRenderer.renderStatic(
                animatable,
                animatable.getMainHandItem(),
                ItemDisplayContext.HEAD,
                false, poseStack, bufferSource, null, packedLight,
                LivingEntityRenderer.getOverlayCoords(animatable, 0),
                animatable.getId());
        super.render(animatable, yaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
