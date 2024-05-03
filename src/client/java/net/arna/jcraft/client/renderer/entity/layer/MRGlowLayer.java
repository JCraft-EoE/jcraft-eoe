package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MRGlowLayer extends GeoRenderLayer<MagiciansRedEntity> {
    private static final Identifier LAYER = new Identifier(JCraft.MOD_ID, "textures/entity/stands/magicians_red/glow.png");
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/magicians_red.geo.json");

    public MRGlowLayer(GeoRenderer<MagiciansRedEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, MagiciansRedEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer bufferIn, float partialTicks, int packedLightIn, int packedOverlay) {
        RenderLayer cameo = RenderLayer.getEyes(LAYER);
        matrixStackIn.push();
        //new Identifier("minecraft", "textures/block/fire_1.png")

        getRenderer().reRender(getDefaultBakedModel(animatable), matrixStackIn, bufferSource, animatable, cameo,
                bufferSource.getBuffer(cameo), partialTicks, packedLightIn, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1f);
        matrixStackIn.pop();
    }
}
