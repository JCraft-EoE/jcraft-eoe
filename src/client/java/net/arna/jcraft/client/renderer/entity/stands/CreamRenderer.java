package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.CreamModel;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CreamRenderer extends StandEntityRenderer<CreamEntity> {

    public CreamRenderer(EntityRendererFactory.Context context) {
        super(context, new CreamModel());
    }

    @Override
    public RenderLayer getRenderType(CreamEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        return animatable.getVoidTime() > 0 ? RenderLayer.getEntitySolid(textureLocation) :
                super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }

    @Override
    protected float getRed(CreamEntity stand, float red, float alpha) {
        return red - (1f - alpha) / 2f;
    }

    @Override
    protected float getGreen(CreamEntity stand, float green, float alpha) {
        return green - (1f - alpha) / 2f;
    }
}
