package net.arna.jcraft.client.renderer.entity.projectiles;

import net.arna.jcraft.client.model.entity.PHCapsuleModel;
import net.arna.jcraft.common.entity.projectile.PHCapsuleProjectile;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class PHCapsuleRenderer extends GeoEntityRenderer<PHCapsuleProjectile> {

    public PHCapsuleRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new PHCapsuleModel());
    }

    @Override
    public void render(PHCapsuleProjectile animatable, float yaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        super.render(animatable, yaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
