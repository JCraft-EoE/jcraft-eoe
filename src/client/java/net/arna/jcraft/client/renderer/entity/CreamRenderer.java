package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.CreamModel;
import net.arna.jcraft.common.entity.CreamEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
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

public class CreamRenderer extends GeoEntityRenderer<CreamEntity> {

    public CreamRenderer(EntityRendererFactory.Context context) {
        super(context, new CreamModel());
    }

    @Override
    public RenderLayer getRenderType(CreamEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null) {
            if (mcClient.player.getFirstPassenger() == animatable) {
                return animatable.getVoidTime() > 0 ? RenderLayer.getEntityTranslucent(this.getTextureLocation(animatable)) : RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));
            }
        }

        return RenderLayer.getEntityCutout(this.getTextureLocation(animatable));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, CreamEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;
        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if ( ((IEntityDataSaver)mcClient.player).getStand() == animatable )
                a = animatable.getAlpha();
        float rgR = (1.0f - a) / 2f;
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red - rgR, green - rgR, blue, a);
    }
}