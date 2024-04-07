package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.TheSunModel;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SunRenderer extends GeoEntityRenderer<TheSunEntity> {
    public SunRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TheSunModel());
    }

    @Override
    public RenderLayer getRenderType(TheSunEntity stand, float partialTicks, MatrixStack stack, @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(textureLocation);
    }

    @Override
    public void render(GeoModel model, TheSunEntity animatable, float partialTick, RenderLayer type, MatrixStack poseStack, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.render(model, animatable, partialTick, type, poseStack, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, 1.0f);
    }

    @Override
    protected int getBlockLight(TheSunEntity stand, BlockPos pos) {
        return 16;
    }
}
