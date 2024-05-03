package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.List;
import java.util.stream.IntStream;

public class HGNetGlowLayer extends GeoRenderLayer<HGNetEntity> {
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/hg_nets.geo.json");
    private static final List<Identifier> skins = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/hg_nets/glow_" + i + ".png")).toList();

    public HGNetGlowLayer(GeoRenderer<HGNetEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, HGNetEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTicks, int packedLightIn, int packedOverlay) {
        if (animatable.isCharged()) {
            RenderLayer cameo = RenderLayer.getEyes(skins.get(animatable.getSkin()));

            matrixStackIn.push();
            getRenderer().reRender(getDefaultBakedModel(animatable), matrixStackIn, bufferSource, animatable, cameo,
                    bufferSource.getBuffer(cameo), partialTicks, packedLightIn, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1f);
            matrixStackIn.pop();
        }
    }
}
