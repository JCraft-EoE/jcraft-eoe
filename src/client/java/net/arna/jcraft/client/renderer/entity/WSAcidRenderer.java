package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.WSAcidModel;
import net.arna.jcraft.common.entity.WSAcidProjectile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class WSAcidRenderer extends GeoProjectilesRenderer<WSAcidProjectile> {
    public WSAcidRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new WSAcidModel());
    }

    @Override
    public RenderLayer getRenderType(WSAcidProjectile animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
                                     Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
