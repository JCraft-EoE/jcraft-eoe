package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.SilverChariotModel;
import net.arna.jcraft.client.renderer.entity.layer.SCRapierLayer;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class SilverChariotRenderer extends StandEntityRenderer<SilverChariotEntity> {

    public SilverChariotRenderer(EntityRendererFactory.Context context) {
        super(context, new SilverChariotModel());
        addLayer(new SCRapierLayer(this));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, SilverChariotEntity animatable, float tickDelta, RenderLayer type, MatrixStack matrixStack,
                       VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn,
                       float red, float green, float blue, float alpha) {
        super.render(model, animatable, tickDelta, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (animatable.getMode() == SilverChariotEntity.Mode.ARMORLESS && animatable.hasUser()) for (double i = 0; i < 3; i++)
            renderAfter(animatable.getUserOrThrow(), JUtils.deltaPos(animatable).multiply(i * 2.0), 1f,
                    model, animatable, tickDelta, RenderLayer.getEntityNoOutline(getTextureLocation(animatable)),
                    matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue,
                    alpha);
    }

    private void renderAfter(LivingEntity user, Vec3d velocity, float a, GeoModel model, SilverChariotEntity animatable,
                             float partialTicks, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer,
                             VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStack.push();

        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(user.bodyYaw));

        double y = velocity.y;
        if (-0.2 < -y && y < 0.2)
            y = 0;

        matrixStack.translate(velocity.x, y, velocity.z);
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-user.bodyYaw));
        super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
        matrixStack.pop();
    }
}
