package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.KQBTDEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class KQBTDEyesLayer extends GeoLayerRenderer<KQBTDEntity> {
    private static final Identifier LAYER = new Identifier(JCraft.MOD_ID, "textures/entity/kqbtd_eyes.png");
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/kqbtd.geo.json");

    public KQBTDEyesLayer(IGeoRenderer<KQBTDEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, KQBTDEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        RenderLayer cameo = RenderLayer.getEyes(LAYER);
        matrixStackIn.push();

        this.getRenderer().render(this.getEntityModel().getModel(MODEL), entityLivingBaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrixStackIn.pop();
    }
}
