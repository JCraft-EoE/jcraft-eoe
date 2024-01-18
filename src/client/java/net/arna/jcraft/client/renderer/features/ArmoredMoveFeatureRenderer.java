package net.arna.jcraft.client.renderer.features;

import net.arna.jcraft.common.component.JComponents;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ArmoredMoveFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public ArmoredMoveFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible())
            return;

        float armoredHitTicks = (float)JComponents.getMiscData(entity).getArmoredHitTicks();
        float flashTime = MathHelper.lerp(1.0F - tickDelta, armoredHitTicks - 1, armoredHitTicks) / 10.0F;

        if (flashTime <= 0.0F)
            return;

        M model = getContextModel();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLightning());

        matrixStack.push();
        float s = 0.75F + flashTime - flashTime * flashTime * flashTime * flashTime;
        matrixStack.scale(s, s, s);
        model.render(matrixStack, vertexConsumer, 0xff, OverlayTexture.DEFAULT_UV, 1.0F, 0.5F + flashTime / 2.0F, flashTime, flashTime);
        matrixStack.pop();
    }
}
