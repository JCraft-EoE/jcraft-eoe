package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The {@link StandEntityRenderer} for {@link MetallicaEntity}.
 */
@Environment(EnvType.CLIENT)
public class MetallicaRenderer extends StandEntityRenderer<MetallicaEntity> {

    private final ItemInHandRenderer heldItemRenderer;

    public MetallicaRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.METALLICA.get());
        this.heldItemRenderer = context.getItemInHandRenderer();
    }

    private static final ItemStack IRON_NUGGET = Items.IRON_NUGGET.getDefaultInstance();

    /*
    @Override
    public void actuallyRender(final PoseStack matrixStack, final MetallicaEntity animatable, final BakedGeoModel model,
                               final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer,
                               final boolean isReRender, final float partialTick, final int packedLight, final int packedOverlay,
                               final float red, final float green, final float blue, final float alpha) {
        final float a = StandEntityRenderer.getAlpha(animatable, partialTick);
        super.actuallyRender(matrixStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, a);

        if (!animatable.hasUser()) return;
        if (animatable.getState() == MetallicaEntity.State.HARVEST) {
            final BlockPos siphonPos = animatable.getSiphonPos().orElse(null);
            if (siphonPos == null) return;

            final LivingEntity user = animatable.getUserOrThrow();
            final Vec3 eyeOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);
            final Vec3 midPos = new Vec3(
                    Mth.lerp(partialTick, user.xOld, user.getX()),
                    Mth.lerp(partialTick, user.yOld, user.getY()),
                    Mth.lerp(partialTick, user.zOld, user.getZ())
            ).add(GravityChangerAPI.getEyeOffset(user));
            final Vec3 toUser = siphonPos.getCenter().subtract(midPos).scale(0.2);
            final Vec3 standToUser = user.position().subtract(animatable.position()).add(eyeOffset);
            final double time = (RenderUtils.getCurrentTick() / 5.0) % 1.0;
            matrixStack.pushPose();
            matrixStack.translate(standToUser.x, standToUser.y, standToUser.z);
            matrixStack.translate(-toUser.x * time, -toUser.y * time, -toUser.z * time);
            for (int i = 0; i < 5; i++) {
                matrixStack.translate(toUser.x, toUser.y, toUser.z);
                matrixStack.pushPose();
                matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, user.xRotO, user.getXRot())));
                matrixStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTick, user.yRotO, user.getYRot()) + 90));
                this.heldItemRenderer.renderItem(animatable, IRON_NUGGET, ItemDisplayContext.GROUND, false, matrixStack, bufferSource, packedLight);
                matrixStack.popPose();
            }
            matrixStack.popPose();
        }
    }*/
}
