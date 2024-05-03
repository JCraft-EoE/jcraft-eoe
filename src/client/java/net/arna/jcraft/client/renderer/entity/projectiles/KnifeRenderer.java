package net.arna.jcraft.client.renderer.entity.projectiles;

import net.arna.jcraft.client.model.entity.KnifeModel;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class KnifeRenderer extends GeoEntityRenderer<KnifeProjectile> {

    public KnifeRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new KnifeModel());
    }

    protected int getBlockLight(KnifeProjectile entityIn, BlockPos partialTicks) {
        return (entityIn.getLightning() || entityIn.isOnFire()) ? 15 : entityIn.world.getLightLevel(LightType.BLOCK, entityIn.getBlockPos());
    }

    @Override
    public RenderLayer getRenderType(KnifeProjectile animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
                                     Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
