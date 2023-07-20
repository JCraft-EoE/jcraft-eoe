package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.RapierModel;
import net.arna.jcraft.common.entity.RapierProjectile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class RapierRenderer extends GeoProjectilesRenderer<RapierProjectile> {

    public RapierRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new RapierModel());
    }

    @Override
    public RenderLayer getRenderType(RapierProjectile animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
                                     Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
