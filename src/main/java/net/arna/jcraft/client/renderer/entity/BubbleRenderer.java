package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.BubbleModel;
import net.arna.jcraft.entity.BubbleProjectile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class BubbleRenderer extends GeoProjectilesRenderer<BubbleProjectile> {

    public BubbleRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new BubbleModel());
    }

    protected int getBlockLight(BubbleProjectile entityIn, BlockPos partialTicks) { return entityIn.world.getLightLevel(LightType.BLOCK, entityIn.getBlockPos()); }

    @Override
    public RenderLayer getRenderType(BubbleProjectile animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
                                     Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
