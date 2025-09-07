package net.arna.jcraft.mixin.client.gravity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//method_26271 refers to a lambda which is why this class may cause mixin warnings/errors
@Mixin(BiomeAmbientSoundsHandler.class)
public abstract class BiomeEffectSoundPlayerMixin {
    @Shadow @Final private LocalPlayer player;
    
    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getEyeY()D",
                    ordinal = 0
            )
    )
    private double jcraft$method_26271_getEyeY_0(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().y;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getX()D",
                    ordinal = 0
            )
    )
    private double jcraft$method_26271_getX_0(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().x;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D",
                    ordinal = 0
            )
    )
    private double jcraft$method_26271_getZ_0(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().z;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getEyeY()D",
                    ordinal = 1
            )
    )
    private double jcraft$method_26271_getEyeY_1(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().y;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getX()D",
                    ordinal = 1
            )
    )
    private double jcraft$method_26271_getX_1(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().x;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D",
                    ordinal = 1
            )
    )
    private double jcraft$method_26271_getZ_1(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().z;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getEyeY()D",
                    ordinal = 2
            )
    )
    private double jcraft$method_26271_getEyeY_2(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().y;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getX()D",
                    ordinal = 2
            )
    )
    private double jcraft$method_26271_getX_2(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().x;
    }

    @ModifyExpressionValue(
            method = "method_26271", // lambda
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D",
                    ordinal = 2
            )
    )
    private double jcraft$method_26271_getZ_2(double original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().z;
    }
}
