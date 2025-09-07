package net.arna.jcraft.mixin.client.gravity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(
            method = "handleGameEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D"
            )
    )
    private double jcraft$onGameStateChange_getEyeY_0(double original) {
        final Player player = minecraft.player;

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().y;
    }

    @ModifyExpressionValue(
            method = "handleGameEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getX()D"
            )
    )
    private double jcraft$onGameStateChange_getX_0(double original) {
        final Player player = minecraft.player;

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().x;
    }

    @ModifyExpressionValue(
            method = "handleGameEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getZ()D"
            )
    )
    private double jcraft$onGameStateChange_getZ_0(double original) {
        final Player player = minecraft.player;

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getEyePosition().z;
    }

    @ModifyExpressionValue(
            method = "handleExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 jcraft$onExplosion_add_0(Vec3 original, @Local(argsOnly = true, ordinal = 0) ClientboundExplodePacket packet) {
        final Player player = minecraft.player;

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return original;
        }

        return player.getDeltaMovement().add(
                RotationUtil.vecWorldToPlayer(
                        (double)packet.getKnockbackX(),
                        (double)packet.getKnockbackY(),
                        (double)packet.getKnockbackZ(),
                        gravityDirection)
        );
    }
}
