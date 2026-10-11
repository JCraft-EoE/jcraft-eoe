package net.arna.jcraft.mixin.gravity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    @Final
    protected RandomSource random;

    // @Shadow public abstract void lerpTo(double x, double y, double z, float yRot, float xRot, int lerpSteps, boolean teleport);

    @Shadow public abstract Vec3 position();

    @Shadow public abstract float getXRot();

    @Shadow private EntityDimensions dimensions;

    @Shadow private Level level;

    @Shadow public abstract double getY();

    @Shadow protected abstract void onBelowWorld();

    @Shadow public abstract Vec3 getEyePosition();

    @ModifyReturnValue(method = "makeBoundingBox", at = @At("RETURN"))
    private AABB jcraft$calculateBoundingBox(final AABB original) {
        if ((Object)this instanceof Projectile) {
            return original;
        }
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }
        AABB box = original.move((entity).position().reverse());
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0d, -1.0e-6, 0d);
        }
        return RotationUtil.boxPlayerToWorld(box, gravityDirection).move((entity).position());
    }

    @ModifyReturnValue(method = "getBoundingBoxForPose", at = @At("RETURN"))
    private AABB jcraft$calculateBoundsForPose(final AABB original) {
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }
        AABB box = original.move(entity.position().reverse());
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0d, -1.0e-6, 0d);
        }
        return RotationUtil.boxPlayerToWorld(box, gravityDirection).move(entity.position());
    }

    @ModifyReturnValue(method = "calculateViewVector", at = @At("RETURN"))
    private Vec3 jcraft$getRotationVector(final Vec3 original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity)(Object)this);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }
        return RotationUtil.vecPlayerToWorld(original, gravityDirection);
    }

    @WrapMethod(method = "getBlockPosBelowThatAffectsMyMovement")
    private BlockPos jcraft$getVelocityAffectingPos(final Operation<BlockPos> original) {
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            return original.call();
        }
        return BlockPos.containing(entity.position().add(Vec3.atLowerCornerOf(gravityDirection.getNormal()).scale(0.5000001d)));
    }

    @WrapMethod(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;")
    private Vec3 jcraft$getEyePos(final Operation<Vec3> original) {
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original.call();
        }
        return RotationUtil.vecPlayerToWorld(0d, entity.getEyeHeight(), 0d, gravityDirection).add(entity.position());
    }

    @WrapMethod(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;")
    private Vec3 jcraft$getCameraPosVec(final float partialTicks, final Operation<Vec3> original) {
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            Vec3 vec3d = RotationUtil.vecPlayerToWorld(0.0D, entity.getEyeHeight(), 0.0D, gravityDirection);
            double d = Mth.lerp(partialTicks, entity.xo, entity.getX()) + vec3d.x;
            double e = Mth.lerp(partialTicks, entity.yo, entity.getY()) + vec3d.y;
            double f = Mth.lerp(partialTicks, entity.zo, entity.getZ()) + vec3d.z;
            return new Vec3(d, e, f);
        }
        return original.call(partialTicks);
    }

    @WrapMethod(method = "getLightLevelDependentMagicValue")
    private float jcraft$getBrightnessAtFEyes(final Operation<Float> original) {
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original.call();
        }
        return entity.level().hasChunkAt(entity.getBlockX(), entity.getBlockZ()) ? entity.level().getLightLevelDependentMagicValue(BlockPos.containing(entity.getEyePosition())) : 0f;
    }

    @ModifyVariable(
            method = "move",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private Vec3 jcraft$modify_move_Vec3d_0_0(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }

    @ModifyArg(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
            ),
            index = 0
    )
    private Vec3 jcraft$modify_move_multiply_0(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.maskPlayerToWorld(vec3d, gravityDirection);
    }

    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"
            ),
            ordinal = 0,
            argsOnly = true)
    private Vec3 jcraft$modify_move_Vec3d_0_1(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"
            ),
            ordinal = 1
    )
    private Vec3 jcraft$modify_move_Vec3d_1(final Vec3 vec3) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3;
        }
        return RotationUtil.vecWorldToPlayer(vec3, gravityDirection);
    }

    @WrapMethod(method = "getOnPosLegacy")
    private BlockPos jcraft$getOnPosLegacy(final Operation<BlockPos> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call();
        }
        return BlockPos.containing(RotationUtil.vecPlayerToWorld(0.0D, -0.2F, 0.0D, gravityDirection).add(this.position()));
    }

    @WrapMethod(method = "getOnPos()Lnet/minecraft/core/BlockPos;")
    private BlockPos jcraft$getOnPos(final Operation<BlockPos> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call();
        }
        return BlockPos.containing(RotationUtil.vecPlayerToWorld(0.0D, -1.0E-5F, 0.0D, gravityDirection).add(this.position()));
    }

    //todo: copy getOnPos(F) inject from actual gravityapi

    @ModifyVariable(
            method = "collide",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            ),
            ordinal = 0,
            argsOnly = true)
    private Vec3 jcraft$modify_adjustMovementForCollisions_Vec3d_0(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    @ModifyReturnValue(method = "collide", at = @At("RETURN"))
    private Vec3 inject_adjustMovementForCollisions(final Vec3 original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }
        return RotationUtil.vecPlayerToWorld(original, gravityDirection);
    }

    @ModifyArg(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 1
    )
    private double jcraft$adjustMovementForCollisions_stretch_0(final double x) {
        Vec3 rotate = new Vec3(x, 0, 0);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.x;
    }

    @ModifyArg(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 1
    )
    private double jcraft$adjustMovementForCollisions_stretch_1(final double y) {
        Vec3 rotate = new Vec3(0, y, 0);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.y;
    }

    @ModifyArg(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 1
    )
    private double jcraft$adjustMovementForCollisions_stretch_2(final double z) {
        Vec3 rotate = new Vec3(0, 0, z);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.z;
    }

    @ModifyArg(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 0
    )
    private Vec3 jcraft$adjustMovementForCollisions_offset_0(final Vec3 vec) {
        Vec3 rotate = vec;
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate;
    }

    @ModifyArg(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 1
            )
    )
    private Vec3 jcraft$adjustMovementForCollisions_offset_1(final Vec3 vec) {
        Vec3 rotate = vec;
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate;
    }

    @ModifyVariable(
            method = "collideBoundingBox",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private static Vec3 jcraft$modify_adjustMovementForCollisions_Vec3d_0(final Vec3 vec3d, final Entity entity) {
        if (entity == null) {
            return vec3d;
        }
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }

    @ModifyReturnValue(method = "collideBoundingBox", at = @At("RETURN"))
    private static Vec3 inject_adjustMovementForCollisions(final Vec3 original, final @Nullable Entity entity, final Vec3 vec, final AABB collisionBox, final Level level, final List<VoxelShape> potentialHits) {
        if (entity == null) {
            return original;
        }
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }
        return RotationUtil.vecWorldToPlayer(original, gravityDirection);
    }

    @ModifyReturnValue(method = "collideBoundingBox", at = @At("RETURN"))
    private static Vec3 jcraft$adjustMovementForCollisions_adjustMovementForCollisions_0(
            final Vec3 original, final @Nullable Entity entity, final Vec3 vec, final AABB collisionBox, final Level level, final List<VoxelShape> potentialHits) {
        final Direction gravityDirection;
        if (entity == null || (gravityDirection = GravityChangerAPI.getGravityDirection(entity)) == Direction.DOWN) {
            return original;
        }
        AABB entityBoundingBox = collisionBox;

        final Vec3 playerMovement = RotationUtil.vecWorldToPlayer(vec, gravityDirection);
        double playerMovementX = playerMovement.x, playerMovementY = playerMovement.y, playerMovementZ = playerMovement.z;
        final Direction directionX = RotationUtil.dirPlayerToWorld(Direction.EAST, gravityDirection);
        final Direction directionY = RotationUtil.dirPlayerToWorld(Direction.UP, gravityDirection);
        final Direction directionZ = RotationUtil.dirPlayerToWorld(Direction.SOUTH, gravityDirection);
        if (playerMovementY != 0d) {
            playerMovementY = Shapes.collide(directionY.getAxis(), entityBoundingBox, potentialHits, playerMovementY * directionY.getAxisDirection().getStep()) * directionY.getAxisDirection().getStep();
            if (playerMovementY != 0d) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(0d, playerMovementY, 0d, gravityDirection));
            }
        }

        boolean isZLargerThanX = Math.abs(playerMovementX) < Math.abs(playerMovementZ);
        if (isZLargerThanX && playerMovementZ != 0d) {
            playerMovementZ = Shapes.collide(directionZ.getAxis(), entityBoundingBox, potentialHits, playerMovementZ * directionZ.getAxisDirection().getStep()) * directionZ.getAxisDirection().getStep();
            if (playerMovementZ != 0d) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(0d, 0d, playerMovementZ, gravityDirection));
            }
        }

        if (playerMovementX != 0d) {
            playerMovementX = Shapes.collide(directionX.getAxis(), entityBoundingBox, potentialHits, playerMovementX * directionX.getAxisDirection().getStep()) * directionX.getAxisDirection().getStep();
            if (!isZLargerThanX && playerMovementX != 0d) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(playerMovementX, 0d, 0d, gravityDirection));
            }
        }

        if (!isZLargerThanX && playerMovementZ != 0d) {
            playerMovementZ = Shapes.collide(directionZ.getAxis(), entityBoundingBox, potentialHits, playerMovementZ * directionZ.getAxisDirection().getStep()) * directionZ.getAxisDirection().getStep();
        }

        return RotationUtil.vecPlayerToWorld(playerMovementX, playerMovementY, playerMovementZ, gravityDirection);
    }

    /*
    @Inject(
            method = "collideBoundingBox",
            at = @At("RETURN"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(
            @Nullable Entity entity, Vec3 movement, AABB entityBoundingBox, Level world,
            List<VoxelShape> collisions, CallbackInfoReturnable<Vec3> cir, ImmutableList.Builder<VoxelShape> shapeListBuilder) {
        EntityMixinLogic.redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(entity, movement, entityBoundingBox, world, shapeListBuilder, cir);
    }
     */


    @ModifyArg(
            method = "isInWall",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;ofSize(Lnet/minecraft/world/phys/Vec3;DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_isInsideWall_of_0(final double dx) {
        Vec3 rotate = new Vec3(dx, 0,0);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.x;
    }

    @ModifyArg(
            method = "isInWall",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;ofSize(Lnet/minecraft/world/phys/Vec3;DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 2
    )
    private double jcraft$modify_isInsideWall_of_1(final double dy) {
        Vec3 rotate = new Vec3(0, dy,0);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.y;
    }

    @ModifyArg(
            method = "isInWall",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;ofSize(Lnet/minecraft/world/phys/Vec3;DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 3
    )
    private double jcraft$modify_isInsideWall_of_2(final double dz) {
        Vec3 rotate = new Vec3(0, 0, dz);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.z;
    }

    @ModifyArg(
            method = "getDirection",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction;fromYRot(D)Lnet/minecraft/core/Direction;"
            )
    )
    private double jcraft$getHorizontalFacing_getYaw_0(final double rotation) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return rotation;
        }

        return RotationUtil.rotPlayerToWorld((float) rotation, this.getXRot(), gravityDirection).x;
    }

    @WrapMethod(method = "spawnSprintParticle")
    private void jcraft$spawnSprintingParticles(final Operation<Void> original) {
        final Entity entity = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            original.call();
            return;
        }

        Vec3 floorPos = entity.position().subtract(RotationUtil.vecPlayerToWorld(0d, 0.20000000298023224, 0d, gravityDirection));

        BlockPos blockPos = BlockPos.containing(floorPos);
        BlockState blockState = entity.level().getBlockState(blockPos);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 particlePos = entity.position().add(RotationUtil.vecPlayerToWorld((this.random.nextDouble() - 0.5) * (double) this.dimensions.width, 0.1, (this.random.nextDouble() - 0.5) * (double) this.dimensions.width, gravityDirection));
            Vec3 playerVelocity = entity.getDeltaMovement();
            Vec3 particleVelocity = RotationUtil.vecPlayerToWorld(playerVelocity.x * -4d, 1.5, playerVelocity.z * -4d, gravityDirection);
            entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
    }

    @WrapMethod(method = "push(Lnet/minecraft/world/entity/Entity;)V")
    private void inject_pushAwayFrom(final Entity entity, final Operation<Void> original) {
        final Entity thiz = (Entity)(Object)this;
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(thiz);
        final Direction otherGravityDirection = GravityChangerAPI.getGravityDirection(entity);

        if (gravityDirection == Direction.DOWN && otherGravityDirection == Direction.DOWN) {
            original.call(entity);
            return;
        }

        if (!thiz.isPassengerOfSameVehicle(entity)) {
            if (!entity.noPhysics && !thiz.noPhysics) {
                Vec3 entityOffset = entity.getBoundingBox().getCenter().subtract(thiz.getBoundingBox().getCenter());

                {
                    Vec3 playerEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, gravityDirection);
                    double dx = playerEntityOffset.x;
                    double dz = playerEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1d / f;
                        if (g > 1d) {
                            g = 1d;
                        }

                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806;
                        dz *= 0.05000000074505806;
                        if (!thiz.isVehicle()) {
                            thiz.push(-dx, 0d, -dz);
                        }
                    }
                }

                {
                    Vec3 entityEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, otherGravityDirection);
                    double dx = entityEntityOffset.x;
                    double dz = entityEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1d / f;
                        if (g > 1d) {
                            g = 1d;
                        }

                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806;
                        dz *= 0.05000000074505806;
                        if (!entity.isVehicle()) {
                            entity.push(dx, 0d, dz);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "checkBelowWorld", at = @At("HEAD"))
    private void jcraft$attemptTickInVoid(final CallbackInfo ci) {
        if (JCraft.gravityConfig.voidDamageAboveWorld && this.getY() > (double) (this.level.getMaxBuildHeight() + 256)) {
            this.onBelowWorld();
        }
    }

    @ModifyArg(
            method = "isFree(DDD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 0
    )
    private double jcraft$doesNotCollide_offset_0(final double x) {
        Vec3 rotate = new Vec3(x, 0, 0);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.x;
    }

    @ModifyArg(
            method = "isFree(DDD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 0
    )
    private double jcraft$doesNotCollide_offset_1(final double y) {
        Vec3 rotate = new Vec3(0, y, 0);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.y;
    }

    @ModifyArg(
            method = "isFree(DDD)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 0
    )
    private double jcraft$doesNotCollide_offset_2(final double z) {
        Vec3 rotate = new Vec3(0, 0, z);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate.z;
    }

    @SuppressWarnings("ParameterCanBeLocal")
    @ModifyVariable(
            method = "updateFluidOnEyes",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private double submergedInWaterEyeFix(double d) {
        d = this.getEyePosition().y();
        return d;
    }

    @SuppressWarnings("ParameterCanBeLocal")
    @ModifyVariable(
            method = "updateFluidOnEyes",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private BlockPos submergedInWaterPosFix(BlockPos blockpos) {
        blockpos = BlockPos.containing(this.getEyePosition());
        return blockpos;
    }

}
