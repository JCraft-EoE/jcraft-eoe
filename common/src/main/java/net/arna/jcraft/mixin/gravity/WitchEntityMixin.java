package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Witch.class)
public abstract class WitchEntityMixin {

    @ModifyVariable(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 jcraft$modify_attack_Vec3d_0(final Vec3 value, final LivingEntity target, final float pullProgress) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }
        return RotationUtil.vecPlayerToWorld(value, gravityDirection);
    }


    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D"
            )
    )
    private double jcraft$attack_getX_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getEyeHeight() - 1.100000023841858, 0d, gravityDirection)).x;
    }

    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D"
            )
    )
    private double jcraft$attack_getEyeY_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getEyeHeight() - 1.100000023841858, 0d, gravityDirection)).y + 1.100000023841858;
    }

    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D"
            )
    )
    private double jcraft$attack_getZ_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getEyeHeight() - 1.100000023841858, 0.0, gravityDirection)).z;
    }

    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;sqrt(D)D"
            )
    )
    private double jcraft$attack_sqrt_0(final double a, final Operation<Double> original, final LivingEntity target, final float velocity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return original.call(a);
        }
        return Math.sqrt(original.call(a));
    }

}
