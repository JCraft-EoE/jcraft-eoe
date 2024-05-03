package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;

public class TWOHEyesLayer extends GeoRenderLayer<TheWorldOverHeavenEntity> {
    private static final Identifier LAYER = new Identifier(JCraft.MOD_ID, "textures/entity/stands/the_world_over_heaven/eyes.png");
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/the_world_over_heaven.geo.json");
    private static final Map<Integer, Vector3f> overwriteColors =
            Map.ofEntries(
              Map.entry(0, new Vector3f(1f, 1f, 1f)), // Default, WHITE

              Map.entry(1, new Vector3f(1f, 0.2f, 0.2f)),  // Unwatchable, RED
              Map.entry(2, new Vector3f(0.6f, 0.2f, 1f)),  // DoT, PURPLE
              Map.entry(3, new Vector3f(0.2f, 1f, 0.2f)),  // Heal, GREEN

              Map.entry(4, new Vector3f(1f, 0.8f, 0)) // Heavy, YELLOW
            );

    public TWOHEyesLayer(GeoRenderer<TheWorldOverHeavenEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, TheWorldOverHeavenEntity twoh, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer bufferIn, float partialTick, int packedLightIn, int packedOverlay) {

        Vector3f color = overwriteColors.get(twoh.getOverwriteType());
        RenderLayer cameo = RenderLayer.getEyes(LAYER);

        matrixStackIn.push();
        getRenderer().reRender(getDefaultBakedModel(twoh), matrixStackIn, bufferSource, twoh, cameo,
                bufferSource.getBuffer(cameo), partialTick, packedLightIn, OverlayTexture.DEFAULT_UV, color.x(), color.y(), color.z(), 1f);
        matrixStackIn.pop();
    }
}
