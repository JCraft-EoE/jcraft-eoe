package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.MadeInHeavenModel;
import net.arna.jcraft.common.entity.MadeInHeavenEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MadeInHeavenRenderer extends GeoEntityRenderer<MadeInHeavenEntity> {

    public MadeInHeavenRenderer(EntityRendererFactory.Context context) {
        super(context, new MadeInHeavenModel());
    }

    @Override
    public RenderLayer getRenderType(MadeInHeavenEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (mcClient.player.getFirstPassenger() == animatable)
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));

        return RenderLayer.getEntityCutout(getTextureLocation(animatable));
    }

    @Override
    public void render(GeoModel model, MadeInHeavenEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if ( ((IEntityDataSaver)mcClient.player).getStand() == animatable )
                a = animatable.getAlpha();

        super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);

        if (animatable.getAfterimage()) {
            float aa = a - 0.5f;
            if (aa < 0) aa = 0;

            for (int i = 0; i <= 3; ++i) {

                Vec3d velocity = animatable.getUser().getVelocity().multiply(i);

                renderAfter(
                        velocity,
                        aa * (1f / i),
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

    private void renderAfter(Vec3d velocity, float aa, GeoModel model, MadeInHeavenEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){
        matrixStack.push();

        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(animatable.getUser().bodyYaw));
        matrixStack.translate(velocity.x, -velocity.y, velocity.z);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-animatable.getUser().bodyYaw));
        super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, aa);
        matrixStack.pop();
    }
}