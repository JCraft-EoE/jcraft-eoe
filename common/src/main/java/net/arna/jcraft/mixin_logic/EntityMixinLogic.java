package net.arna.jcraft.mixin_logic;

import com.google.common.collect.ImmutableList;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class EntityMixinLogic {
    public static void jcraft$updatePassengerPosition(final Entity thisEntity, final Entity passenger, final Entity.MoveFunction positionUpdater, final CallbackInfo info) {
        if (passenger instanceof StandEntity<?, ?> stand) {
            if (stand.isFree() && !stand.isRemote()) {
                Vector3f freePos = stand.getFreePos();
                positionUpdater.accept(passenger, freePos.x(), freePos.y(), freePos.z());
                info.cancel();
                return;
            }

            final double dist = stand.getDistanceOffset();

            float y = thisEntity.getYRot() + stand.getRotationOffset();
            y *= (float) Math.PI / 180;

            final double heightOffset = stand.shouldOffsetHeight() ? Vec3.directionFromRotation(thisEntity.getXRot(), thisEntity.getYRot()).y : 0;
            final Vec3 adjustedOffset = RotationUtil.vecPlayerToWorld(
                    Mth.cos(y) * dist,
                    passenger.getMyRidingOffset() + heightOffset + stand.getYDistanceOffset(),
                    Mth.sin(y) * dist,
                    GravityChangerAPI.getGravityDirection(thisEntity)
            );
            positionUpdater.accept(passenger, thisEntity.getX() + adjustedOffset.x, thisEntity.getY() + adjustedOffset.y, thisEntity.getZ() + adjustedOffset.z);
            info.cancel();
        }
    }

    public static void doNotPlayDesummonSoundWhenMovingWorld(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        StandEntity<?, ?> stand = JUtils.getStand(living);
        if (stand == null) {
            return;
        }

        stand.setPlayDesummonSound(false);
    }

    public static void inject_calculateBoundingBox(Entity entity, CallbackInfoReturnable<AABB> cir) {
        if (entity instanceof Projectile) {
            return;
        }

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        AABB box = cir.getReturnValue().move(entity.position().reverse());
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0.0D, -1.0E-6D, 0.0D);
        }
        cir.setReturnValue(RotationUtil.boxPlayerToWorld(box, gravityDirection).move(entity.position()));
    }

    public static void inject_calculateBoundsForPose(Entity entity, CallbackInfoReturnable<AABB> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        AABB box = cir.getReturnValue().move(entity.position().reverse());
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0.0D, -1.0E-6D, 0.0D);
        }
        cir.setReturnValue(RotationUtil.boxPlayerToWorld(box, gravityDirection).move(entity.position()));
    }

    public static void inject_getRotationVector(Entity entity, CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
    }

    public static void inject_getVelocityAffectingPos(Entity entity, CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        cir.setReturnValue(BlockPos.containing(entity.position().add(Vec3.atLowerCornerOf(gravityDirection.getNormal()).scale(0.5000001D))));
    }

    public static void inject_getEyePos(Entity entity, CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(0.0D, entity.getEyeHeight(), 0.0D, gravityDirection).add(entity.position()));
    }

    public static void inject_getCameraPosVec(Entity entity, float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        Vec3 vec3d = RotationUtil.vecPlayerToWorld(0.0D, entity.getEyeHeight(), 0.0D, gravityDirection);

        double d = Mth.lerp(tickDelta, entity.xo, entity.getX()) + vec3d.x;
        double e = Mth.lerp(tickDelta, entity.yo, entity.getY()) + vec3d.y;
        double f = Mth.lerp(tickDelta, entity.zo, entity.getZ()) + vec3d.z;
        cir.setReturnValue(new Vec3(d, e, f));
    }

    public static void inject_getBrightnessAtFEyes(Entity entity, CallbackInfoReturnable<Float> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        cir.setReturnValue(entity.level().hasChunkAt(entity.getBlockX(), entity.getBlockZ()) ? entity.level().getLightLevelDependentMagicValue(BlockPos.containing(entity.getEyePosition())) : 0.0F);
    }

    public static void inject_pushAwayFrom(Entity thisEntity, Entity entity, CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(thisEntity);
        Direction otherGravityDirection = GravityChangerAPI.getGravityDirection(entity);

        if (gravityDirection == Direction.DOWN && otherGravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();

        if (!thisEntity.isPassengerOfSameVehicle(entity)) {
            if (!entity.noPhysics && !thisEntity.noPhysics) {
                Vec3 entityOffset = entity.getBoundingBox().getCenter().subtract(thisEntity.getBoundingBox().getCenter());

                {
                    Vec3 playerEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, gravityDirection);
                    double dx = playerEntityOffset.x;
                    double dz = playerEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582D) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1.0D / f;
                        if (g > 1.0D) {
                            g = 1.0D;
                        }

                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806D;
                        dz *= 0.05000000074505806D;
                        if (!thisEntity.isVehicle()) {
                            thisEntity.push(-dx, 0.0D, -dz);
                        }
                    }
                }

                {
                    Vec3 entityEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, otherGravityDirection);
                    double dx = entityEntityOffset.x;
                    double dz = entityEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582D) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1.0D / f;
                        if (g > 1.0D) {
                            g = 1.0D;
                        }

                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806D;
                        dz *= 0.05000000074505806D;
                        if (!entity.isVehicle()) {
                            entity.push(dx, 0.0D, dz);
                        }
                    }
                }
            }
        }
    }

    public static void redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(
            @Nullable Entity entity, Vec3 movement,
            AABB entityBoundingBox, Level world, ImmutableList.Builder<VoxelShape> shapeListBuilder, CallbackInfoReturnable<Vec3> cir
            ) {
        redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(entity, movement, entityBoundingBox, world, shapeListBuilder.build(), cir);
    }

    /**
     * Used in {@link net.arna.jcraft.mixin.gravity.EntityMixin#redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(Entity, Vec3, AABB, Level, List, CallbackInfoReturnable)}
     * Has a problem with Lithium, causing choppy movement. Related Lithium mixin can be found
     * <a href="https://github.com/CaffeineMC/lithium-fabric/blob/1.20.1/src/main/java/me/jellysquid/mods/lithium/mixin/entity/collisions/movement/EntityMixin.java">here</a>
     */
    public static void redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(
            @Nullable final Entity entity, final Vec3 movement, AABB entityBoundingBox,
            final Level world, final List<VoxelShape> collisions, final CallbackInfoReturnable<Vec3> cir
    ) {
        final Direction gravityDirection;
        if (entity == null || (gravityDirection = GravityChangerAPI.getGravityDirection(entity)) == Direction.DOWN) {
            return;
        }

        final Vec3 playerMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        double playerMovementX = playerMovement.x, playerMovementY = playerMovement.y, playerMovementZ = playerMovement.z;
        final Direction directionX = RotationUtil.dirPlayerToWorld(Direction.EAST, gravityDirection);
        final Direction directionY = RotationUtil.dirPlayerToWorld(Direction.UP, gravityDirection);
        final Direction directionZ = RotationUtil.dirPlayerToWorld(Direction.SOUTH, gravityDirection);
        if (playerMovementY != 0.0D) {
            playerMovementY = Shapes.collide(directionY.getAxis(), entityBoundingBox, collisions, playerMovementY * directionY.getAxisDirection().getStep()) * directionY.getAxisDirection().getStep();
            if (playerMovementY != 0.0D) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(0.0D, playerMovementY, 0.0D, gravityDirection));
            }
        }

        boolean isZLargerThanX = Math.abs(playerMovementX) < Math.abs(playerMovementZ);
        if (isZLargerThanX && playerMovementZ != 0.0D) {
            playerMovementZ = Shapes.collide(directionZ.getAxis(), entityBoundingBox, collisions, playerMovementZ * directionZ.getAxisDirection().getStep()) * directionZ.getAxisDirection().getStep();
            if (playerMovementZ != 0.0D) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(0.0D, 0.0D, playerMovementZ, gravityDirection));
            }
        }

        if (playerMovementX != 0.0D) {
            playerMovementX = Shapes.collide(directionX.getAxis(), entityBoundingBox, collisions, playerMovementX * directionX.getAxisDirection().getStep()) * directionX.getAxisDirection().getStep();
            if (!isZLargerThanX && playerMovementX != 0.0D) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(playerMovementX, 0.0D, 0.0D, gravityDirection));
            }
        }

        if (!isZLargerThanX && playerMovementZ != 0.0D) {
            playerMovementZ = Shapes.collide(directionZ.getAxis(), entityBoundingBox, collisions, playerMovementZ * directionZ.getAxisDirection().getStep()) * directionZ.getAxisDirection().getStep();
        }

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(playerMovementX, playerMovementY, playerMovementZ, gravityDirection));
    }

    public static void inject_spawnSprintingParticles(Entity entity, RandomSource random, EntityDimensions dimensions, CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();

        Vec3 floorPos = entity.position().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.20000000298023224D, 0.0D, gravityDirection));

        BlockPos blockPos = BlockPos.containing(floorPos);
        BlockState blockState = entity.level().getBlockState(blockPos);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 particlePos = entity.position().add(RotationUtil.vecPlayerToWorld((random.nextDouble() - 0.5D) * (double) dimensions.width, 0.1D, (random.nextDouble() - 0.5D) * (double) dimensions.width, gravityDirection));
            Vec3 playerVelocity = entity.getDeltaMovement();
            Vec3 particleVelocity = RotationUtil.vecPlayerToWorld(playerVelocity.x * -4.0D, 1.5D, playerVelocity.z * -4.0D, gravityDirection);
            entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
    }
}
