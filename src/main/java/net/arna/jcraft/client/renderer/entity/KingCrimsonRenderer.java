package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.KingCrimsonModel;
import net.arna.jcraft.client.renderer.entity.layer.KCTELayer;
import net.arna.jcraft.entity.KingCrimsonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import org.jetbrains.annotations.Nullable;

public class KingCrimsonRenderer extends GeoEntityRenderer<KingCrimsonEntity> {

    public KingCrimsonRenderer(EntityRendererFactory.Context context) {
        super(context, new KingCrimsonModel());
        //this.addLayer(new KCTELayer(this));
    }

    @Override
    public RenderLayer getRenderType(KingCrimsonEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null) {
            if (mcClient.player.getFirstPassenger() == animatable) {
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));
            }
        }

        return RenderLayer.getEntityTranslucent(this.getTextureLocation(animatable));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, KingCrimsonEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = (animatable.getTETime() > 0) ? 0f : 1f;

        // When in first person, draws translucently
        // When in third person, solid
        // When viewed by others, solid, except in time erase

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.player != null) {
            if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player.getFirstPassenger() == animatable) { a = animatable.getAlpha(); }
            else if (a == 0) { return; }
        }

        float gbR = 1.0f - a;
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green - gbR, blue - gbR, a);
    }
}