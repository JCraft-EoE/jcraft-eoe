package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.SilverChariotModel;
import net.arna.jcraft.common.entity.SilverChariotEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SilverChariotRenderer extends GeoEntityRenderer<SilverChariotEntity> {

    public SilverChariotRenderer(EntityRendererFactory.Context context) {
        super(context, new SilverChariotModel());
    }

    @Override
    public RenderLayer getRenderType(SilverChariotEntity animatable, float partialTicks, MatrixStack stack,
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
    public void render(GeoModel model, SilverChariotEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if ( ((IEntityDataSaver)mcClient.player).getStand() == animatable )
                a = animatable.getAlpha();

        super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);

        if (animatable.getMode() == 2) {
            for (double i = 0; i <= 2; ++i) {
                renderAfter(
                        JUtils.deltaPos(animatable).multiply(i * 2.0),
                        1f,
                        model,
                        animatable,
                        partialTicks,
                        RenderLayer.getEntityNoOutline(getTextureLocation(animatable)),
                        matrixStack,
                        renderTypeBuffer,
                        vertexBuilder,
                        packedLightIn,
                        packedOverlayIn,
                        red,
                        green,
                        blue,
                        alpha
                );
            }
        }
    }

    private void renderAfter(Vec3d velocity, float a, GeoModel model, SilverChariotEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){
        matrixStack.push();

        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(animatable.getUser().bodyYaw));

        double y = velocity.y;
        if (-0.2 < -y && y < 0.2)
            y = 0;

        matrixStack.translate(velocity.x, y, velocity.z);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-animatable.getUser().bodyYaw));
        super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
        matrixStack.pop();
    }
}