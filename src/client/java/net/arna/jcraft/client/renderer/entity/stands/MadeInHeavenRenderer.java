package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.MadeInHeavenModel;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class MadeInHeavenRenderer extends StandEntityRenderer<MadeInHeavenEntity> {

    public MadeInHeavenRenderer(EntityRendererFactory.Context context) {
        super(context, new MadeInHeavenModel());
    }

    @Override
    public void render(GeoModel model, MadeInHeavenEntity animatable, float tickDelta, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.render(model, animatable, tickDelta, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (!animatable.getAfterimage()) return;

        float aa = getAlpha(animatable, tickDelta) - 0.5f;
        if (aa < 0) aa = 0;

        Vec3d baseVel = Vec3d.ZERO;
        float bodyYaw = animatable.bodyYaw;
        if (animatable.hasUser()) {
            LivingEntity user = animatable.getUserOrThrow();
            baseVel = user.getVelocity();
            bodyYaw = user.bodyYaw;
        }

        for (int i = 0; i <= 3; ++i)
            renderAfter(baseVel.multiply(i), bodyYaw, aa * (1f / i), model, animatable, tickDelta,
                    RenderLayer.getEntityNoOutline(getTextureLocation(animatable)), matrixStack, renderTypeBuffer,
                    vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    private void renderAfter(Vec3d velocity, float bodyYaw, float aa, GeoModel model, MadeInHeavenEntity animatable,
                             float partialTicks, RenderLayer type, MatrixStack matrixStack,
                             VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
                             int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStack.push();

        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(bodyYaw));
        matrixStack.translate(velocity.x, -velocity.y, velocity.z);
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-bodyYaw));
        super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, aa);
        matrixStack.pop();
    }
}
