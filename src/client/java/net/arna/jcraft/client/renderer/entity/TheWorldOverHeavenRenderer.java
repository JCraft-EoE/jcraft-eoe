package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.TheWorldOverHeavenModel;
import net.arna.jcraft.client.renderer.entity.layer.TWOHEyesLayer;
import net.arna.jcraft.common.entity.TheWorldOverHeavenEntity;
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

public class TheWorldOverHeavenRenderer extends GeoEntityRenderer<TheWorldOverHeavenEntity> {

    public TheWorldOverHeavenRenderer(EntityRendererFactory.Context context) {
        super(context, new TheWorldOverHeavenModel());
        this.addLayer(new TWOHEyesLayer(this));
    }

    @Override
    public RenderLayer getRenderType(TheWorldOverHeavenEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (mcClient.player.getFirstPassenger() == animatable)
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));
        return RenderLayer.getEntityTranslucent(this.getTextureLocation(animatable));
    }


    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, TheWorldOverHeavenEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;
        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if ( ((IEntityDataSaver)mcClient.player).getStand() == animatable )
                a = animatable.getAlpha();
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
    }
}