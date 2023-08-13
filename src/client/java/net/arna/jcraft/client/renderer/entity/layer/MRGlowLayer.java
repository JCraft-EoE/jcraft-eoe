package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class MRGlowLayer extends GeoLayerRenderer<MagiciansRedEntity> {
    private static final Identifier LAYER = new Identifier(JCraft.MOD_ID, "textures/entity/stands/magicians_red/glow.png");
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/magicians_red.geo.json");

    public MRGlowLayer(IGeoRenderer<MagiciansRedEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, MagiciansRedEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        RenderLayer cameo = RenderLayer.getEyes(LAYER);
        matrixStackIn.push();
        //new Identifier("minecraft", "textures/block/fire_1.png")

        this.getRenderer().render(this.getEntityModel().getModel(MODEL), entityLivingBaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrixStackIn.pop();
    }
}
