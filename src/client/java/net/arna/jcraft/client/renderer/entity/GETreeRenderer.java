package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GETreeModel;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class GETreeRenderer extends GeoEntityRenderer<GETreeEntity> {

    public GETreeRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new GETreeModel());
        shadowRadius = 3f;
    }

    @Override
    public void renderEarly(GETreeEntity animatable, MatrixStack poseStack, float partialTick, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //poseStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(MathHelper.lerp(partialTick, animatable.prevYaw, animatable.getYaw()) - 90));
        //poseStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.lerp(partialTick, animatable.prevPitch, animatable.getPitch())));
        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
