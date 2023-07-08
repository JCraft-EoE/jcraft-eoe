package net.arna.jcraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.client.model.entity.MadeInHeavenModel;
import net.arna.jcraft.common.entity.MadeInHeavenEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
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

        //todo: sterner
        if (animatable.getAfterimage()) {
            float aa = a - 0.5f;
            if (aa < 0) aa = 0;

            matrixStack.push();
            /*
            matrixStack.multiplyPositionMatrix(
                    mcClient.gameRenderer.getBasicProjectionMatrix(
                            mcClient.options.getFov().getValue()
                    )
            );
             */
            Vec3d velocity = animatable.getUser().getVelocity();
            matrixStack.translate(velocity.x, velocity.y, velocity.z);
            super.render(
                    model,
                    animatable,
                    partialTicks,
                    RenderLayer.getEntityNoOutline( getTextureLocation(animatable) ),
                    matrixStack,
                    renderTypeBuffer,
                    vertexBuilder,
                    packedLightIn,
                    packedOverlayIn, red, green, blue, aa);
            matrixStack.pop();
        }
    }
}