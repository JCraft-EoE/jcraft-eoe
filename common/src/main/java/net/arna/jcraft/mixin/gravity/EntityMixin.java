package net.arna.jcraft.mixin.gravity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.mixin_logic.EntityMixinLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = Entity.class)
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

    @Inject(
            method = "makeBoundingBox",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_calculateBoundingBox(CallbackInfoReturnable<AABB> cir) {
        EntityMixinLogic.jcraft$inject_calculateBoundingBox((Entity) (Object) this, cir);
    }

    @Inject(
            method = "getBoundingBoxForPose",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_calculateBoundsForPose(Pose pos, CallbackInfoReturnable<AABB> cir) {
        EntityMixinLogic.jcraft$inject_calculateBoundsForPose((Entity) (Object) this, cir);
    }

    @Inject(
            method = "calculateViewVector",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_getRotationVector(CallbackInfoReturnable<Vec3> cir) {
        EntityMixinLogic.jcraft$inject_getRotationVector((Entity) (Object) this, cir);
    }

    @Inject(
            method = "getBlockPosBelowThatAffectsMyMovement",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getVelocityAffectingPos(CallbackInfoReturnable<BlockPos> cir) {
        EntityMixinLogic.jcraft$inject_getVelocityAffectingPos((Entity) (Object) this, cir);
    }

    @Inject(
            method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getEyePos(CallbackInfoReturnable<Vec3> cir) {
        EntityMixinLogic.jcraft$inject_getEyePos((Entity) (Object) this, cir);
    }

    @Inject(
            method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getCameraPosVec(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        EntityMixinLogic.jcraft$inject_getCameraPosVec((Entity) (Object) this, tickDelta, cir);
    }

    @Inject(
            method = "getLightLevelDependentMagicValue",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getBrightnessAtFEyes(CallbackInfoReturnable<Float> cir) {
        EntityMixinLogic.jcraft$inject_getBrightnessAtFEyes((Entity) (Object) this, cir);
    }

    @ModifyVariable(
            method = "move",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private Vec3 modify_move_Vec3d_0_0(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
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
    private Vec3 modify_move_multiply_0(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
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
    private Vec3 modify_move_Vec3d_0_1(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
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
    private Vec3 modify_move_Vec3d_1(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    @Inject(
            method = "getOnPosLegacy",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getOnPosLegacy(CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return;
        }
        BlockPos blockPos = BlockPos.containing(RotationUtil.vecPlayerToWorld(0.0D, -0.2F, 0.0D, gravityDirection).add(this.position()));
        cir.setReturnValue(blockPos);
    }

    @Inject(
            method = "getOnPos()Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getOnPos(CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return;
        }
        BlockPos blockPos = BlockPos.containing(RotationUtil.vecPlayerToWorld(0.0D, -1.0E-5F, 0.0D, gravityDirection).add(this.position()));
        cir.setReturnValue(blockPos);
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
    private Vec3 modify_adjustMovementForCollisions_Vec3d_0(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    @Inject(
            method = "collide",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_adjustMovementForCollisions(CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
    }

    @ModifyArg(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 1
    )
    private double redirect_adjustMovementForCollisions_stretch_0(double x) {
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
    private double redirect_adjustMovementForCollisions_stretch_1(double y) {
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
    private double redirect_adjustMovementForCollisions_stretch_2(double z) {
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
    private Vec3 redirect_adjustMovementForCollisions_offset_0(Vec3 vec) {
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
    private Vec3 redirect_adjustMovementForCollisions_offset_1(Vec3 vec) {
        Vec3 rotate = vec;
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        return rotate;
    }

    @ModifyVariable(
            method = "collideBoundingBox",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private static Vec3 modify_adjustMovementForCollisions_Vec3d_0(Vec3 vec3d, Entity entity) {
        if (entity == null) {
            return vec3d;
        }

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }

    @Inject(
            method = "collideBoundingBox",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void inject_adjustMovementForCollisions(Entity entity, Vec3 movement, AABB entityBoundingBox, Level world, List<VoxelShape> collisions, CallbackInfoReturnable<Vec3> cir) {
        if (entity == null) {
            return;
        }

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        cir.setReturnValue(RotationUtil.vecWorldToPlayer(cir.getReturnValue(), gravityDirection));
    }

    /*
    LITHIUM ERROR -
    Expected (
    Lnet/minecraft/class_1297;
    Lnet/minecraft/class_243;
    Lnet/minecraft/class_238;
    Lnet/minecraft/class_1937;
    Ljava/util/List;
    Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V
    but found (
    Lnet/minecraft/class_1297;
    Lnet/minecraft/class_243;
    Lnet/minecraft/class_238;
    Lnet/minecraft/class_1937;
    Ljava/util/List;
    Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;
    Lcom/google/common/collect/ImmutableList$Builder;)V
     */
    @Inject(
            method = "collideBoundingBox",
            at = @At("RETURN"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(
            @Nullable Entity entity, Vec3 movement, AABB entityBoundingBox, Level world,
            List<VoxelShape> collisions, CallbackInfoReturnable<Vec3> cir) {
        EntityMixinLogic.jcraft$redirect_adjustMovementForCollisions_adjustMovementForCollisions_0(entity, movement, entityBoundingBox, world, collisions, cir);
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
    private double modify_isInsideWall_of_0(double dx) {
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
    private double modify_isInsideWall_of_1(double dy) {
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
    private double modify_isInsideWall_of_2(double dz) {
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
    private double redirect_getHorizontalFacing_getYaw_0(double rotation) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return rotation;
        }

        return RotationUtil.rotPlayerToWorld((float) rotation, this.getXRot(), gravityDirection).x;
    }

    @Inject(
            method = "spawnSprintParticle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_spawnSprintingParticles(CallbackInfo ci) {
        EntityMixinLogic.jcraft$inject_spawnSprintingParticles((Entity) (Object) this, this.random, this.dimensions, ci);
    }

    @Inject(
            method = "push(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_pushAwayFrom(Entity entity, CallbackInfo ci) {
        EntityMixinLogic.jcraft$inject_pushAwayFrom((Entity) (Object) this, entity, ci);
    }

    @Inject(
            method = "checkBelowWorld",
            at = @At("HEAD")
    )
    private void inject_attemptTickInVoid(CallbackInfo ci) {
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
    private double redirect_doesNotCollide_offset_0(double x) {
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
    private double redirect_doesNotCollide_offset_1(double y) {
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
    private double redirect_doesNotCollide_offset_2(double z) {
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
