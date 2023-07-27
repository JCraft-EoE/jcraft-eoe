package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.StarPlatinumModel;
import net.arna.jcraft.common.entity.AbstractStarPlatinumEntity;
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

public class SPTWRenderer extends GeoEntityRenderer<AbstractStarPlatinumEntity<?, ?>> {

    public SPTWRenderer(EntityRendererFactory.Context context) {
        super(context, new StarPlatinumModel(true));
    }

    @Override
    public RenderLayer getRenderType(AbstractStarPlatinumEntity<?, ?> animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (mcClient.player.getFirstPassenger() == animatable)
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));

        return RenderLayer.getEntityCutout(this.getTextureLocation(animatable));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, AbstractStarPlatinumEntity<?, ?> animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;
        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if ( ((IEntityDataSaver)mcClient.player).getStand() == animatable )
                a = animatable.getAlpha();
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
    }
}
