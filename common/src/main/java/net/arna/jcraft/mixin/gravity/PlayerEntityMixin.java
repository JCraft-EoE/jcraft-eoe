package net.arna.jcraft.mixin.gravity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow
    @Final
    private Abilities abilities;

    @Shadow
    public abstract EntityDimensions getDimensions(Pose pose);

    @Shadow
    protected abstract boolean isStayingOnGroundSurface();

    @Shadow
    protected abstract boolean isAboveGround();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @WrapOperation(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    private Vec3 jcraft$travel_getLookAngle_0(final Player instance, final Operation<Vec3> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }

        return RotationUtil.vecWorldToPlayer(original.call(instance), gravityDirection);
    }


    @ModifyArg(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockPos jcraft$modify_move_multiply_0(final BlockPos pos) {
        Vec3 rotate = new Vec3(0d, 1d - 0.1, 0d);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection(this));
        return BlockPos.containing(pos.getX() - rotate.x, pos.getY() - rotate.y + (1d - 0.1), pos.getZ()- rotate.z);
    }


    @WrapOperation(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity jcraft$dropItem_new_0(final Level level, final double posX, final double posY, final double posZ, final ItemStack itemStack, final Operation<ItemEntity> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(level, posX, posY, posZ, itemStack);
        }
        Vec3 vec3d = this.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0d, 0.30000001192092896, 0d, gravityDirection));
        return original.call(level, vec3d.x, vec3d.y, vec3d.z, itemStack);
    }

    @WrapOperation(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(DDD)V"
            )
    )
    private void jcraft$dropItem_setVelocity(final ItemEntity instance, final double x, final double y, final double z, final Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            original.call(instance, x, y, z);
            return;
        }
        final Vec3 vec = RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection);
        original.call(instance, vec.x, vec.y, vec.z);
    }

    @WrapMethod(method = "maybeBackOffFromEdge")
    private Vec3 jcraft$adjustMovementForSneaking(final Vec3 vec, final MoverType mover, final Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(vec, mover);
        }

        Vec3 playerMovement = RotationUtil.vecWorldToPlayer(vec, gravityDirection);

        if (!this.abilities.flying && (mover == MoverType.SELF || mover == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround()) {
            double d = playerMovement.x;
            double e = playerMovement.z;

            while (d != 0.0D && this.level().noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(d, -this.maxUpStep(), 0.0D, gravityDirection)))) {
                if (d < 0.05D && d >= -0.05D) {
                    d = 0.0D;
                } else if (d > 0.0D) {
                    d -= 0.05D;
                } else {
                    d += 0.05D;
                }
            }

            while (e != 0.0D && this.level().noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(0.0D, -this.maxUpStep(), e, gravityDirection)))) {
                if (e < 0.05D && e >= -0.05D) {
                    e = 0.0D;
                } else if (e > 0.0D) {
                    e -= 0.05D;
                } else {
                    e += 0.05D;
                }
            }

            while (d != 0.0D && e != 0.0D && this.level().noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(d, -this.maxUpStep(), e, gravityDirection)))) {
                if (d < 0.05D && d >= -0.05D) {
                    d = 0.0D;
                } else if (d > 0.0D) {
                    d -= 0.05D;
                } else {
                    d += 0.05D;
                }

                if (e < 0.05D && e >= -0.05D) {
                    e = 0.0D;
                } else if (e > 0.0D) {
                    e -= 0.05D;
                } else {
                    e += 0.05D;
                }
            }
            return RotationUtil.vecPlayerToWorld(d, playerMovement.y, e, gravityDirection);
        }
        return vec;
    }

    @WrapOperation(
            method = "isAboveGround",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            )
    )
    private AABB jcraft$isAboveGround_offset_0(final AABB instance, final double x, final double y, final double z, final Operation<AABB> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, x, y, z);
        }
        final Vec3 vec = RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection);
        return original.call(instance, vec.x, vec.y, vec.z);
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
                    ordinal = 0
            )
    )
    private float jcraft$attack_getYaw_0(final Player instance, final Operation<Float> original, final Entity target) {
        final Direction targetGravityDirection = GravityChangerAPI.getGravityDirection(target);
        final Direction attackerGravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (targetGravityDirection == attackerGravityDirection) {
            return original.call(instance);
        }
        return RotationUtil.rotWorldToPlayer(RotationUtil.rotPlayerToWorld(original.call(instance), instance.getXRot(), attackerGravityDirection), targetGravityDirection).x;
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
                    ordinal = 1
            )
    )
    private float jcraft$attack_getYaw_1(final Player instance, final Operation<Float> original, final Entity target) {
        final Direction targetGravityDirection = GravityChangerAPI.getGravityDirection(target);
        final Direction attackerGravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (targetGravityDirection == attackerGravityDirection) {
            return original.call(instance);
        }
        return RotationUtil.rotWorldToPlayer(RotationUtil.rotPlayerToWorld(original.call(instance), instance.getXRot(), attackerGravityDirection), targetGravityDirection).x;
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
                    ordinal = 2
            )
    )
    private float jcraft$attack_getYaw_2(final Player instance, final Operation<Float> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }

        return RotationUtil.rotPlayerToWorld(original.call(instance), instance.getXRot(), gravityDirection).x;
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
                    ordinal = 3
            )
    )
    private float jcraft$attack_getYaw_3(final Player instance, final Operation<Float> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.rotPlayerToWorld(original.call(instance), instance.getXRot(), gravityDirection).x;
    }

    @ModifyArg(
            method = "addParticlesAroundSelf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_addDeathParticless_addParticle_0(final double x) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return x;
        }
        final Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0)), gravityDirection));
        return vec3d.x;
    }

    @ModifyArg(
            method = "addParticlesAroundSelf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_addDeathParticless_addParticle_1(double y) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return y;
        }
        final Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0)), gravityDirection));
        return vec3d.y;
    }

    @ModifyArg(
            method = "addParticlesAroundSelf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_addDeathParticless_addParticle_2(final double z) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return z;
        }
        final Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0)), gravityDirection));
        return vec3d.z;
    }

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_tickMovement_expand_0(final double x) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return x;
        }
        final Vec3 vec3d = RotationUtil.maskPlayerToWorld(1, 0, 1, gravityDirection);
        return vec3d.x;
    }

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_tickMovement_expand_1(final double y) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return y;
        }
        final Vec3 vec3d = RotationUtil.maskPlayerToWorld(1, 0, 1, gravityDirection);
        return vec3d.y;
    }

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            ),
            index = 1
    )
    private double jcraft$modify_tickMovement_expand_2(final double z) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return z;
        }
        final Vec3 vec3d = RotationUtil.maskPlayerToWorld(1, 0, 1, gravityDirection);
        return vec3d.z;
    }

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 1
            ),
            index = 1
    )
    private double jcraft$modify_tickMovement_expand_3(final double x) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return x;
        }
        final Vec3 vec3d = RotationUtil.maskPlayerToWorld(1, 0, 1, gravityDirection);
        return vec3d.x;
    }

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 1
            ),
            index = 1
    )
    private double jcraft$modify_tickMovement_expand_4(final double y) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return y;
        }
        final Vec3 vec3d = RotationUtil.maskPlayerToWorld(1, 0, 1, gravityDirection);
        return vec3d.y;
    }

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 1
            ),
            index = 1
    )
    private double jcraft$modify_tickMovement_expand_5(final double z) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return z;
        }
        final Vec3 vec3d = RotationUtil.maskPlayerToWorld(1, 0, 1, gravityDirection);
        return vec3d.z;
    }

}
