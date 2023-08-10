package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.WhiteSnakeModel;
import net.arna.jcraft.common.entity.WhiteSnakeEntity;
import net.arna.jcraft.common.util.JUtils;
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

public class WhiteSnakeRenderer extends GeoEntityRenderer<WhiteSnakeEntity> {

    public WhiteSnakeRenderer(EntityRendererFactory.Context context) {
        super(context, new WhiteSnakeModel());
    }

    /*
    Cutout - no alpha
    CutoutNoCull - identical (copium)
    Alpha - no lighting
    Translucent - with alpha, nothing renders through
    Decal - invisible
    NoOutline - transparent, everything is visible through
    Shadow - inverted normals, no alpha
    SmoothCutout - Cutout
    Solid - no transparency
     */

    @Override
    public RenderLayer getRenderType(WhiteSnakeEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (JUtils.getStand(mcClient.player) == animatable )
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));

        return RenderLayer.getEntityTranslucent(this.getTextureLocation(animatable));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, WhiteSnakeEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (JUtils.getStand(mcClient.player) == animatable )
                a = animatable.getAlpha();

        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
    }
}
