package net.arna.jcraft.client.renderer.features;

import com.google.common.collect.Streams;
import net.arna.jcraft.client.mixin.AnimalModelAccessor;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.stream.Stream;

public class StuckKnivesFeatureRenderer<T extends LivingEntity, M extends AnimalModel<T>> extends FeatureRenderer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public StuckKnivesFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
        this.dispatcher = context.getRenderDispatcher();
    }

    protected int getObjectCount(T entity) {
        return JComponents.getMiscData(entity).getStuckKnifeCount();
    }

    protected void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta) {
        float f = MathHelper.sqrt(directionX * directionX + directionZ * directionZ);
        KnifeProjectile knife = new KnifeProjectile(entity.world);
        knife.setPos(entity.getX(), entity.getY(), entity.getZ());
        knife.setYaw((float)(Math.atan2(directionX, directionZ) * 57.2957763671875));
        knife.setPitch((float)(Math.atan2(directionY, f) * 57.2957763671875));
        knife.prevYaw = knife.getYaw();
        knife.prevPitch = knife.getPitch();
        dispatcher.render(knife, 0.0, 0.0, 0.0, 0.0f, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        int m = this.getObjectCount(livingEntity);
        Random random = Random.create(livingEntity.getId());
        if (m <= 0) {
            return;
        }
        for (int n = 0; n < m; ++n) {
            matrixStack.push();

            AnimalModelAccessor accessor = (AnimalModelAccessor) getContextModel();
            List<ModelPart> parts = Stream.concat(Streams.stream(accessor.callGetHeadParts()), Streams.stream(accessor.callGetBodyParts())).toList();
            ModelPart part = parts.get(random.nextInt(parts.size()));
            if (!part.isEmpty()) {
                ModelPart.Cuboid cuboid = part.getRandomCuboid(random);
                part.rotate(matrixStack);

                float o = random.nextFloat();
                float p = random.nextFloat();
                float q = random.nextFloat();
                float r = MathHelper.lerp(o, cuboid.minX, cuboid.maxX) / 16.0f;
                float s = MathHelper.lerp(p, cuboid.minY, cuboid.maxY) / 16.0f;
                float t = MathHelper.lerp(q, cuboid.minZ, cuboid.maxZ) / 16.0f;
                matrixStack.translate(r, s, t);
                o = -1.0f * (o * 2.0f - 1.0f);
                p = -1.0f * (p * 2.0f - 1.0f);
                q = -1.0f * (q * 2.0f - 1.0f);
                this.renderObject(matrixStack, vertexConsumerProvider, i, livingEntity, o, p, q, h);
            }
            matrixStack.pop();
        }
    }
}
