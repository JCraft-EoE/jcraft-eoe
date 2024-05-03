package net.arna.jcraft.client.renderer.entity.projectiles;

import net.arna.jcraft.client.model.entity.BlockProjectileModel;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class BlockProjectileRenderer extends GeoEntityRenderer<BlockProjectile> {
    private final ItemRenderer itemRenderer;

    public BlockProjectileRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BlockProjectileModel());
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public RenderLayer getRenderType(BlockProjectile animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
                                     Identifier textureLocation) {
        return RenderLayer.getEyes(getTextureLocation(animatable));
    }

    @Override
    public void render(BlockProjectile animatable, float yaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        //poseStack.multiply(Quaternion.fromEulerXyz(3.1415f, 3.1415f, 0));
        itemRenderer.renderItem(
                animatable,
                animatable.getMainHandStack(),
                ModelTransformationMode.HEAD,
                false, poseStack, bufferSource, null, packedLight,
                LivingEntityRenderer.getOverlay(animatable, 0),
                animatable.getId());
        super.render(animatable, yaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}
