package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Drowned.class)
public abstract class DrownedEntityMixin {

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

        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getBbHeight() * 0.3333333333333333, 0d, gravityDirection)).x;
    }

    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getY(D)D"
            )
    )
    private double jcraft$attack_getBodyY_0(final LivingEntity instance, final double v, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, v);
        }

        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getBbHeight() * 0.3333333333333333, 0d, gravityDirection)).y;
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
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getBbHeight() * 0.3333333333333333, 0d, gravityDirection)).z;
    }

    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;sqrt(D)D"
            )
    )
    private double jcraft$attack_sqrt_0(final double a, final Operation<Double> original, final LivingEntity target, final float velocity) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return original.call(a);
        }
        return Math.sqrt(original.call(a));
    }

}
