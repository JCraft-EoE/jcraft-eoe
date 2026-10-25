package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderMan.class)
public abstract class EndermanEntityMixin {

    @WrapOperation(
            method = "isLookingAtMe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D"
            )
    )
    private double jcraft$isPlayerStaring_getEyeY_0(final Player instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().y;
    }

    @WrapOperation(
            method = "isLookingAtMe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getX()D"
            )
    )
    private double jcraft$isPlayerStaring_getX_0(final Player instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().x;
    }

    @WrapOperation(
            method = "isLookingAtMe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$isPlayerStaring_getZ_0(final Player instance, final Operation<Double> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().z;
    }

    @WrapOperation(
            method = "teleportTowards",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getEyeY()D"
            )
    )
    private double jcraft$teleportTo_getEyeY_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().y;
    }

    @WrapOperation(
            method = "teleportTowards",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D"
            )
    )
    private double jcraft$teleportTo_getX_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().x;
    }

    @WrapOperation(
            method = "teleportTowards",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$teleportTo_getZ_0(final Entity instance, final Operation<Double> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return instance.getEyePosition().z;
    }

}
