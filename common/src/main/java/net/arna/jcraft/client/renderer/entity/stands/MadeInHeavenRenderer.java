package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The {@link StandEntityRenderer} for {@link MadeInHeavenEntity}.
 */
@Environment(EnvType.CLIENT)
public class MadeInHeavenRenderer extends StandEntityRenderer<MadeInHeavenEntity> {

    public MadeInHeavenRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.MADE_IN_HEAVEN.get(), -0.1745329251f, -0.1745329251f);
    }

    /*
    @Override
    public void actuallyRender(final PoseStack poseStack, final MadeInHeavenEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, final boolean isReRender, final float partialTick, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        final float a = getAlpha(animatable, partialTick);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, a);

        if (!animatable.getAfterimage()) {
            return;
        }

        float aa = a - 0.5f;
        if (aa < 0) {
            aa = 0;
        }

        Vec3 baseVel = Vec3.ZERO;
        float bodyYaw = animatable.yBodyRot;
        if (animatable.hasUser()) {
            LivingEntity user = animatable.getUserOrThrow();
            baseVel = user.getDeltaMovement();
            bodyYaw = user.yBodyRot;
        }

        for (int i = 0; i <= 3; ++i) {
            renderAfterImage(baseVel.scale(i), bodyYaw, aa * (1f / i), model, animatable, partialTick,
                    RenderType.entityNoOutline(getTextureLocation(animatable)), poseStack, bufferSource,
                    buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    private void renderAfterImage(final Vec3 velocity, final float bodyYaw, final float aa, final BakedGeoModel model, final MadeInHeavenEntity animatable,
                                  final float partialTicks, final RenderType type, final PoseStack matrixStack,
                                  final MultiBufferSource renderTypeBuffer, final VertexConsumer vertexBuilder, final int packedLightIn,
                                  final int packedOverlayIn, final float red, final float green, final float blue, final float alpha) {
        matrixStack.pushPose();

        matrixStack.mulPose(Axis.YP.rotationDegrees(bodyYaw));
        matrixStack.translate(velocity.x, -velocity.y, velocity.z);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        super.actuallyRender(matrixStack, animatable, model, type, renderTypeBuffer, vertexBuilder, false, partialTicks, packedLightIn, packedOverlayIn, red, green, blue, aa);
        matrixStack.popPose();
    }
    */
}
