package net.arna.jcraft.mixin.client.gravity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.gravity.RotationAnimation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.EntityTags;
import net.arna.jcraft.common.gravity.util.QuaternionUtil;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    @Final
    private static RenderType SHADOW_RENDER_TYPE;

    @Shadow
    private boolean shouldRenderShadow;

    @Shadow
    private static void shadowVertex(PoseStack.Pose entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void jcraft$render_0(final Entity entity, final double x, final double y, final double z, final float yaw, final float tickDelta, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final CallbackInfo ci) {
        if (!(entity instanceof Projectile) && !(entity instanceof ExperienceOrb) && !entity.getType().builtInRegistryHolder().is(EntityTags.FORBIDDEN_ENTITY_RENDERING)) {
            Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
            if (!this.shouldRenderShadow) {
                return;
            }
            matrices.pushPose();
            Optional<RotationAnimation> animationOptional = GravityChangerAPI.getGravityAnimation(entity);
            if (animationOptional.isEmpty()) {
                return;
            }
            RotationAnimation animation = animationOptional.get();
            long timeMs = entity.level().getGameTime() * 50 + (long) (tickDelta * 50);
            matrices.mulPose(QuaternionUtil.inversed(animation.getCurrentGravityRotation(gravityDirection, timeMs)));
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 1
            )
    )
    private void jcraft$render_1(final Entity entity, final double x, final double y, final double z, final float yaw, final float tickDelta, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final CallbackInfo ci) {
        if (!(entity instanceof Projectile) && !(entity instanceof ExperienceOrb) && !entity.getType().builtInRegistryHolder().is(EntityTags.FORBIDDEN_ENTITY_RENDERING)) {
            // Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
            if (!this.shouldRenderShadow) {
                return;
            }

            matrices.popPose();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void jcraft$render_2(final Entity entity, final double x, final double y, final double z, final float yaw, final float tickDelta, final PoseStack matrices, final MultiBufferSource vertexConsumers, final int light, final CallbackInfo ci) {
        if (!(entity instanceof Projectile) && !(entity instanceof ExperienceOrb) && !entity.getType().builtInRegistryHolder().is(EntityTags.FORBIDDEN_ENTITY_RENDERING)) {
            final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
            if (gravityDirection == Direction.DOWN) {
                return;
            }
            if (!this.shouldRenderShadow) {
                return;
            }
            matrices.mulPose(RotationUtil.getCameraRotationQuaternion(gravityDirection));
        }
    }

    @WrapMethod(method = "renderShadow")
    private static void jcraft$renderShadow(final PoseStack poseStack, final MultiBufferSource buffer, final Entity entity, final float weight, final float partialTicks, final LevelReader level, final float size, final Operation<Void> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            original.call(poseStack, buffer, entity, weight, partialTicks, level, size);
            return;
        }

        double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        Vec3 minShadowPos = RotationUtil.vecPlayerToWorld(-size, -size, (double) -size, gravityDirection).add(x, y, z);
        Vec3 maxShadowPos = RotationUtil.vecPlayerToWorld(size, 0d, size, gravityDirection).add(x, y, z);
        PoseStack.Pose entry = poseStack.last();
        VertexConsumer vertexConsumer = buffer.getBuffer(SHADOW_RENDER_TYPE);

        for (BlockPos blockPos : BlockPos.betweenClosed(BlockPos.containing(minShadowPos), BlockPos.containing(maxShadowPos))) {
            jcraft$renderShadowPartPlayer(entry, vertexConsumer, level, blockPos, x, y, z, size, weight, gravityDirection);
        }
    }

    @Unique
    private static void jcraft$renderShadowPartPlayer(PoseStack.Pose entry, VertexConsumer vertices, LevelReader world, BlockPos pos, double x, double y, double z, float radius, float opacity, Direction gravityDirection) {
        BlockPos posBelow = pos.relative(gravityDirection);
        BlockState blockStateBelow = world.getBlockState(posBelow);
        if (blockStateBelow.getRenderShape() != RenderShape.INVISIBLE && world.getMaxLocalRawBrightness(pos) > 3) {
            if (blockStateBelow.isCollisionShapeFullBlock(world, posBelow)) {
                VoxelShape voxelShape = blockStateBelow.getShape(world, posBelow);
                if (!voxelShape.isEmpty()) {
                    Vec3 playerPos = RotationUtil.vecWorldToPlayer(x, y, z, gravityDirection);
                    float alpha = (float) (((double) opacity - (playerPos.y - (RotationUtil.vecWorldToPlayer(Vec3.atCenterOf(pos), gravityDirection).y - 0.5D)) / 2.0D) * 0.5D * (double) world.getLightLevelDependentMagicValue(pos));
                    if (alpha >= 0.0F) {
                        if (alpha > 1.0F) {
                            alpha = 1.0F;
                        }

                        Vec3 centerPos = Vec3.atCenterOf(pos);
                        Vec3 playerCenterPos = RotationUtil.vecWorldToPlayer(centerPos, gravityDirection);

                        Vec3 playerRelNN = playerCenterPos.add(-0.5D, -0.5D, -0.5D).subtract(playerPos);
                        Vec3 playerRelPP = playerCenterPos.add(0.5D, -0.5D, 0.5D).subtract(playerPos);

                        Vec3 relNN = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(-0.5D, -0.5D, -0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);
                        Vec3 relNP = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(-0.5D, -0.5D, 0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);
                        Vec3 relPN = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(0.5D, -0.5D, -0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);
                        Vec3 relPP = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(0.5D, -0.5D, 0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);

                        float minU = -(float) playerRelNN.x / 2.0F / radius + 0.5F;
                        float maxU = -(float) playerRelPP.x / 2.0F / radius + 0.5F;
                        float minV = -(float) playerRelNN.z / 2.0F / radius + 0.5F;
                        float maxV = -(float) playerRelPP.z / 2.0F / radius + 0.5F;

                        shadowVertex(entry, vertices, alpha, (float) relNN.x, (float) relNN.y, (float) relNN.z, minU, minV);
                        shadowVertex(entry, vertices, alpha, (float) relNP.x, (float) relNP.y, (float) relNP.z, minU, maxV);
                        shadowVertex(entry, vertices, alpha, (float) relPP.x, (float) relPP.y, (float) relPP.z, maxU, maxV);
                        shadowVertex(entry, vertices, alpha, (float) relPN.x, (float) relPN.y, (float) relPN.z, maxU, minV);
                    }
                }
            }
        }
    }

    @ModifyVariable(
            method = "renderHitbox",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;"
            )
    )
    private static AABB jcraft$modify_renderHitbox_Box_0(final AABB box, final PoseStack matrices, final VertexConsumer vertices, final Entity entity, final float tickDelta) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return box;
        }
        return RotationUtil.boxWorldToPlayer(box, gravityDirection);
    }

    @ModifyVariable(
            method = "renderHitbox",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private static Vec3 jcraft$modify_renderHitbox_Vec3d_0(final Vec3 vec3d, final PoseStack matrices, final VertexConsumer vertices, final Entity entity, final float tickDelta) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

}
