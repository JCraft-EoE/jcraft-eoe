package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@SuppressWarnings("rawtypes")
public class KCTELayer extends GeoRenderLayer {
    private static final Identifier MODEL = JCraft.id("geo/box.geo.json");

    @SuppressWarnings("unchecked")
    public KCTELayer(GeoRenderer<?> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, GeoAnimatable animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTicks, int packedLightIn, int packedOverlay) {
        if (((KingCrimsonEntity) animatable).getTETime() < 1) {
            return;
        }
        //RenderLayer cameo = JCraftClient.TIMEERASE_RENDER_LAYER;
        RenderLayer cameo = RenderLayer.getCutout();

        matrixStackIn.push();
        matrixStackIn.scale(-4096.0f, -4096.0f, -4096.0f);
        getRenderer().reRender(getDefaultBakedModel(animatable), matrixStackIn, bufferSource, animatable, cameo,
                bufferSource.getBuffer(cameo), partialTicks, packedLightIn, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1f);
        matrixStackIn.pop();
    }
}
