package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.MagiciansRedModel;
import net.arna.jcraft.client.renderer.entity.layer.MRGlowLayer;
import net.arna.jcraft.common.entity.MagiciansRedEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MagiciansRedRenderer extends GeoEntityRenderer<MagiciansRedEntity> {

    public MagiciansRedRenderer(EntityRendererFactory.Context context) {
        super(context, new MagiciansRedModel());
        this.addLayer(new MRGlowLayer(this));
    }

    @Override
    public RenderLayer getRenderType(MagiciansRedEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null) {
            if (mcClient.player.getFirstPassenger() == animatable) {
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));
            }
        }

        return RenderLayer.getEntityCutout(this.getTextureLocation(animatable));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, MagiciansRedEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;
        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null) {
            if (mcClient.player.getFirstPassenger() == animatable) {
                a = animatable.getAlpha();
            }
        }
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
    }
}