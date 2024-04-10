package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.List;
import java.util.stream.IntStream;

public class SunGlowLayer extends GeoLayerRenderer<TheSunEntity> {
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/the_sun.geo.json");
    private static final List<Identifier> skins = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/stands/the_sun/glow_" + i + ".png")).toList();

    public SunGlowLayer(IGeoRenderer<TheSunEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, TheSunEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        RenderLayer cameo = RenderLayer.getEyes(skins.get(entityLivingBaseIn.getSkin()));

        matrixStackIn.push();
        getRenderer().render(getEntityModel().getModel(MODEL), entityLivingBaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrixStackIn.pop();
    }
}
