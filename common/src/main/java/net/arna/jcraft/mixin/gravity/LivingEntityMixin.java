package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.mixin_logic.LivingEntityMixinLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract void readAdditionalSaveData(CompoundTag nbt);

    @Shadow
    public abstract EntityDimensions getDimensions(Pose pose);

    @Shadow
    public abstract float getViewYRot(float tickDelta);

    @Shadow public abstract void calculateEntityAnimation(boolean flutter);

    @Shadow protected abstract void updateWalkAnimation(float partialTick);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
                    ordinal = 0
            )
    )
    private double jcraft$travel_getY_0(final LivingEntity instance, final Operation<Double> original) {
        return LivingEntityMixinLogic.redirect_travel_getY_0(instance, original);
    }

    @WrapOperation(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
                    ordinal = 1
            )
    )
    private double jcraft$travel_getY_1(final LivingEntity instance, final Operation<Double> original) {
        return LivingEntityMixinLogic.redirect_travel_getY_0(instance, original);
    }

    @WrapOperation(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
                    ordinal = 2
            )
    )
    private double jcraft$travel_getY_2(final LivingEntity instance, final Operation<Double> original) {
        return LivingEntityMixinLogic.redirect_travel_getY_0(instance, original);
    }

    @WrapOperation(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
                    ordinal = 3
            )
    )
    private double jcraft$travel_getY_3(final LivingEntity instance, final Operation<Double> original) {
        return LivingEntityMixinLogic.redirect_travel_getY_0(instance, original);
    }

    @ModifyVariable(
            method = "travel",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
            ),
            argsOnly = true)
    private Vec3 jcraft$modify_travel_Vec3d_2(final Vec3 vec3d) {
        return LivingEntityMixinLogic.modify_travel_Vec3d_2(this, vec3d);
    }

    @ModifyArg(
            method = "playBlockFallSound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 0
            ),
            index = 0
    )
    private BlockPos jcraft$modify_playBlockFallSound_getBlockState_0(final BlockPos blockPos) {
        return LivingEntityMixinLogic.modify_playBlockFallSound_getBlockState_0(this, blockPos, this.position());
    }

    @WrapOperation(
            method = "hasLineOfSight",
            at = @At(
                    value = "NEW",
                    target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    private Vec3 jcraft$canSee_new_0(final double x, final double y, final double z, final Operation<Vec3> original) {
        return LivingEntityMixinLogic.redirect_canSee_new_0(this, x, y, z, this.getEyePosition(), original);
    }

    @WrapOperation(
            method = "hasLineOfSight",
            at = @At(
                    value = "NEW",
                    target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 1
            )
    )
    private Vec3 jcraft$canSee_new_1(final double x, final double y, final double z, final Operation<Vec3> original) {
        return LivingEntityMixinLogic.redirect_canSee_new_0(this, x, y, z, this.getEyePosition(), original);
    }

    @ModifyReturnValue(method = "getLocalBoundsForPose", at = @At("RETURN"))
    private AABB jcraft$getBoundingBox(final AABB original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }
        AABB box = original;
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0d, -1.0e-6, 0d);
        }
        return RotationUtil.boxPlayerToWorld(box, gravityDirection);
    }

    @WrapMethod(method = "calculateEntityAnimation")
    private void jcraft$updateLimbs(final boolean includeHeight, final Operation<Void> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) {
            original.call(includeHeight);
            return;
        }
        final Vec3 playerPosDelta = RotationUtil.vecWorldToPlayer(this.getX() - this.xo, this.getY() - this.yOld, this.getZ() - this.zOld, gravityDirection);
        final float mag = (float) Mth.length(playerPosDelta.x,includeHeight ? playerPosDelta.y : 0.0D,playerPosDelta.z);
        this.updateWalkAnimation(mag);
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
                    ordinal = 0
            )
    )
    private double jcraft$tick_getX_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.vecWorldToPlayer(original.call(instance) - instance.xo, instance.getY() - instance.yo, instance.getZ() - instance.zo, gravityDirection).x + instance.xo;
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$tick_getZ_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.vecWorldToPlayer(instance.getX() - instance.xo, instance.getY() - instance.yo, original.call(instance) - instance.zo, gravityDirection).z + instance.zo;
    }

    @WrapOperation(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = 0
            )
    )
    private double jcraft$damage_getX_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            if (GravityChangerAPI.getGravityDirection(instance) == Direction.DOWN) {
                return original.call(instance);
            } else {
                return instance.getEyePosition().x;
            }
        }
        return RotationUtil.vecWorldToPlayer(instance.getEyePosition(), gravityDirection).x;
    }

    @WrapOperation(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$damage_getZ_0(final Entity instance, final Operation<Double> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            if (GravityChangerAPI.getGravityDirection(instance) == Direction.DOWN) {
                return original.call(instance);
            } else {
                return instance.getEyePosition().z;
            }
        }

        return RotationUtil.vecWorldToPlayer(instance.getEyePosition(), gravityDirection).z;
    }

    @WrapOperation(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
                    ordinal = 0
            )
    )
    private double jcraft$damage_getX_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.vecWorldToPlayer(instance.position(), gravityDirection).x;
    }

    @WrapOperation(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$damage_getZ_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.vecWorldToPlayer(instance.position(), gravityDirection).z;
    }

    @WrapOperation(
            method = "blockedByShield",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
                    ordinal = 0
            )
    )
    private double jcraft$knockback_getX_0(final LivingEntity instance, final Operation<Double> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }

        return RotationUtil.vecWorldToPlayer(instance.position(), gravityDirection).x;
    }

    @WrapOperation(
            method = "blockedByShield",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$knockback_getZ_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.vecWorldToPlayer(instance.position(), gravityDirection).z;
    }

    @WrapOperation(
            method = "blockedByShield",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
                    ordinal = 1
            )
    )
    private double jcraft$knockback_getX_1(final LivingEntity instance, final Operation<Double> original, final LivingEntity defender) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(defender);
        if (gravityDirection == Direction.DOWN) {
            if (GravityChangerAPI.getGravityDirection(instance) == Direction.DOWN) {
                return original.call(instance);
            } else {
                return instance.getEyePosition().x;
            }
        }
        return RotationUtil.vecWorldToPlayer(instance.getEyePosition(), gravityDirection).x;
    }

    @WrapOperation(
            method = "blockedByShield",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
                    ordinal = 1
            )
    )
    private double jcraft$knockback_getZ_1(final LivingEntity instance, final Operation<Double> original, final LivingEntity defender) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(defender);
        if (gravityDirection == Direction.DOWN) {
            if (GravityChangerAPI.getGravityDirection(instance) == Direction.DOWN) {
                return original.call(instance);
            } else {
                return instance.getEyePosition().z;
            }
        }

        return RotationUtil.vecWorldToPlayer(instance.getEyePosition(), gravityDirection).z;
    }

    @WrapOperation(
            method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;blockPosition()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos jcraft$baseTick_new_0(LivingEntity instance, Operation<BlockPos> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return BlockPos.containing(instance.getX(), instance.getY(), instance.getZ());
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "spawnItemParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    private Vec3 jcraft$spawnItemParticles_add_0(final Vec3 instance, final double x, final double y, final double z, final Operation<Vec3> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, x, y, z);
        }

        return this.getEyePosition().add(RotationUtil.vecPlayerToWorld(instance, gravityDirection));
    }

    @ModifyVariable(
            method = "spawnItemParticles",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private Vec3 jcraft$modify_spawnItemParticles_Vec3d_0(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }

    @ModifyArg(
            method = "tickEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_tickStatusEffects_addParticle_0(final double x) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return x;
        }
        Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(x, 0, 0), gravityDirection));
        return vec3d.x;
    }

    @ModifyArg(
            method = "tickEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 2
    )
    private double jcraft$modify_tickStatusEffects_addParticle_1(final double y) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return y;
        }
        Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(0, y, 0), gravityDirection));
        return vec3d.y;
    }

    @ModifyArg(
            method = "tickEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 3
    )
    private double jcraft$modify_tickStatusEffects_addParticle_2(final double z) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return z;
        }
        Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(0, 0, z), gravityDirection));
        return vec3d.z;
    }

    @ModifyArg(
            method = "makePoofParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_addDeathParticless_addParticle_0(final double x) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return x;
        }

        Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(x, 0, 0), gravityDirection));
        return vec3d.x;
    }

    @ModifyArg(
            method = "makePoofParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 2
    )
    private double jcraft$modify_addDeathParticless_addParticle_1(final double y) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return y;
        }
        final Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(0, y, 0), gravityDirection));
        return vec3d.y;
    }

    @ModifyArg(
            method = "makePoofParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 3
    )
    private double jcraft$modify_addDeathParticless_addParticle_2(final double z) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return z;
        }
        final Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(0, 0, z), gravityDirection));
        return vec3d.z;
    }

    @ModifyVariable(
            method = "isDamageSourceBlocked",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private Vec3 jcraft$modify_blockedByShield_Vec3d_1(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    @ModifyArg(
            method = "isDamageSourceBlocked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;vectorTo(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            ),
            index = 0
    )
    private Vec3 jcraft$modify_blockedByShield_relativize_0(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return this.getEyePosition();
    }

    @ModifyVariable(
            method = "isDamageSourceBlocked",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            ),
            ordinal = 2
    )
    private Vec3 jcraft$modify_blockedByShield_Vec3d_2(final Vec3 vec3d) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

}
