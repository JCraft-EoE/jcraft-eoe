package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.TheWorldOverHeavenEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.Map;

public class TWOHEyesLayer extends GeoLayerRenderer<TheWorldOverHeavenEntity> {
    private static final Identifier LAYER = new Identifier(JCraft.MOD_ID, "textures/entity/stands/the_world_over_heaven/eyes.png");
    private static final Identifier MODEL = new Identifier(JCraft.MOD_ID, "geo/the_world_over_heaven.geo.json");
    private static final Map<Integer, Vec3f> overwriteColors =
            Map.ofEntries(
              Map.entry(0, new Vec3f(1f, 1f, 1f)), // Default, WHITE

              Map.entry(1, new Vec3f(1f, 0.2f, 0.2f)),  // Unwatchable, RED
              Map.entry(2, new Vec3f(0.6f, 0.2f, 1f)),  // DoT, PURPLE
              Map.entry(3, new Vec3f(0.2f, 1f, 0.2f)),  // Heal, GREEN

              Map.entry(4, new Vec3f(1f, 0.8f, 0)) // Heavy, YELLOW
            );

    public TWOHEyesLayer(IGeoRenderer<TheWorldOverHeavenEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, TheWorldOverHeavenEntity twoh, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        Vec3f color = overwriteColors.get(twoh.getOverwriteType());
        RenderLayer cameo = RenderLayer.getEyes(LAYER);

        matrixStackIn.push();
        getRenderer().render(getEntityModel().getModel(MODEL), twoh, partialTicks, cameo, matrixStackIn, bufferIn,
                bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, color.getX(), color.getY(), color.getZ(), 1f);
        matrixStackIn.pop();
    }
}
