package net.arna.jcraft.client.renderer.features;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.mixin.client.AnimalModelAccessor;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import java.util.stream.Stream;

public class StuckKnivesFeatureRenderer<T extends LivingEntity, M extends AgeableListModel<T>> extends RenderLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public StuckKnivesFeatureRenderer(final EntityRendererProvider.Context context, final LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    protected int getObjectCount(final T entity) {
        return JComponentPlatformUtils.getMiscData(entity).getStuckKnifeCount();
    }

    protected void renderObject(final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final Entity entity, final float directionX, final float directionY, final float directionZ, final float tickDelta) {
        final float f = Mth.sqrt(directionX * directionX + directionZ * directionZ);
        final KnifeProjectile knife = new KnifeProjectile(entity.level());
        knife.setPosRaw(entity.getX(), entity.getY(), entity.getZ());
        knife.setYRot((float) (Math.atan2(directionX, directionZ) * 57.2957763671875));
        knife.setXRot((float) (Math.atan2(directionY, f) * 57.2957763671875));
        knife.yRotO = knife.getYRot();
        knife.xRotO = knife.getXRot();
        dispatcher.render(knife, 0.0, 0.0, 0.0, 0.0f, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public void render(final PoseStack matrixStack, final MultiBufferSource vertexConsumerProvider, final int i, final T livingEntity, final float f, final float g, final float h, final float j, final float k, final float l) {
        final int m = this.getObjectCount(livingEntity);
        if (m <= 0) return;

        final RandomSource random = RandomSource.create(livingEntity.getId());
        final AnimalModelAccessor accessor = (AnimalModelAccessor) getParentModel();
        final List<ModelPart> parts = Stream.concat(Streams.stream(accessor.callHeadParts()), Streams.stream(accessor.callBodyParts())).toList();

        for (int n = 0; n < m; ++n) {
            matrixStack.pushPose();

            final ModelPart part = parts.get(random.nextInt(parts.size()));
            if (!part.isEmpty()) {
                final ModelPart.Cube cuboid = part.getRandomCube(random);
                part.translateAndRotate(matrixStack);

                float o = random.nextFloat();
                float p = random.nextFloat();
                float q = random.nextFloat();
                float r = Mth.lerp(o, cuboid.minX, cuboid.maxX) / 16.0f;
                float s = Mth.lerp(p, cuboid.minY, cuboid.maxY) / 16.0f;
                float t = Mth.lerp(q, cuboid.minZ, cuboid.maxZ) / 16.0f;
                matrixStack.translate(r, s, t);
                o = -1.0f * (o * 2.0f - 1.0f);
                p = -1.0f * (p * 2.0f - 1.0f);
                q = -1.0f * (q * 2.0f - 1.0f);
                this.renderObject(matrixStack, vertexConsumerProvider, i, livingEntity, o, p, q, h);
            }
            matrixStack.popPose();
        }
    }
}
