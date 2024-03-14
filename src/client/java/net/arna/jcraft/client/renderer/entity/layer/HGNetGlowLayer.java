package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class HGNetGlowLayer extends GeoLayerRenderer<HGNetEntity> {
    private static final Identifier LAYER = new Identifier(JCraft.MOD_ID, "textures/entity/hg_nets_glow.png");
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/hg_nets.geo.json");

    public HGNetGlowLayer(IGeoRenderer<HGNetEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, HGNetEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityLivingBaseIn.isCharged()) {
            RenderLayer cameo = RenderLayer.getEyes(LAYER);

            matrixStackIn.push();
            getRenderer().render(getEntityModel().getModel(MODEL), entityLivingBaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                    bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
            matrixStackIn.pop();
        }
    }
}
