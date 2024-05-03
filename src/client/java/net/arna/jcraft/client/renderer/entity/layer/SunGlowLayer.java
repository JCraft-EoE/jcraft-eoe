package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
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

public class SunGlowLayer extends GeoRenderLayer<TheSunEntity> {
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/the_sun.geo.json");
    private static final List<Identifier> skins = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/stands/the_sun/glow_" + i + ".png")).toList();

    public SunGlowLayer(GeoRenderer<TheSunEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, TheSunEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderLayer cameo = RenderLayer.getEyes(skins.get(animatable.getSkin()));

        matrixStackIn.push();
        getRenderer().reRender(getDefaultBakedModel(animatable), matrixStackIn, bufferSource, animatable, cameo,
                bufferSource.getBuffer(cameo), partialTick, packedLight, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1f);
        matrixStackIn.pop();
    }
}
