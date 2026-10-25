package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(WitherBoss.class)
public abstract class WitherEntityMixin {

    @Shadow public static Predicate<LivingEntity> LIVING_ENTITY_SELECTOR;

    @WrapOperation(
            method = "performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D"
            )
    )
    private double jcraft$shootSkullAt_getX_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getEyeHeight() * 0.5, 0d, gravityDirection)).x;
    }

    @WrapOperation(
            method = "performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getY()D",
                    ordinal = 0
            )
    )
    private double jcraft$shootSkullAt_getY_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getEyeHeight() * 0.5, 0d, gravityDirection)).y - instance.getEyeHeight() * 0.5;
    }

    @WrapOperation(
            method = "performRangedAttack(ILnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$shootSkullAt_getZ_0(final LivingEntity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.position().add(RotationUtil.vecPlayerToWorld(0d, instance.getEyeHeight() * 0.5, 0d, gravityDirection)).z;
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getEyeY()D"
            )
    )
    private double jcraft$tickMovement_getEyeY_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().y;
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D"
            )
    )
    private double jcraft$tickMovement_getX_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().x;
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$tickMovement_getZ_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().z;
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void dont_attack_player_stands_in_creative(CallbackInfo ci) {
        final Predicate<LivingEntity> oldSelector = LIVING_ENTITY_SELECTOR;
        LIVING_ENTITY_SELECTOR = oldSelector.and((arg) -> {
            final LivingEntity maybeUser = JUtils.getUserIfStand(arg);
            if (maybeUser instanceof Player player) {
                return !player.isCreative() && !player.isSpectator();
            }
            else if (maybeUser != arg) {
                return oldSelector.test(maybeUser);
            }
            return true;
        });
        WitherBoss.TARGETING_CONDITIONS.selector(LIVING_ENTITY_SELECTOR);
    }

}
