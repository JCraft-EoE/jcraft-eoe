package net.arna.jcraft.mixin.gravity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(
            method = "moveToWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void inject_moveToWorld_sendPacket_1(CallbackInfoReturnable<ServerPlayerEntity> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((ServerPlayerEntity)(Object)this);
        if(gravityDirection != GravityChangerAPI.getDefaultGravityDirection((ServerPlayerEntity)(Object)this) && JCraft.gravityConfig.resetGravityOnDimensionChange) {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayerEntity)(Object)this, Direction.DOWN, new RotationParameters().rotationTime(0));
        } else {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayerEntity)(Object)this, GravityChangerAPI.getDefaultGravityDirection((ServerPlayerEntity)(Object)this), new RotationParameters().rotationTime(0));
        }
    }

    @Inject(
            method = "teleport",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void inject_teleport_sendPacket_0(CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((ServerPlayerEntity)(Object)this);
        if(gravityDirection != GravityChangerAPI.getDefaultGravityDirection((ServerPlayerEntity)(Object)this) && JCraft.gravityConfig.resetGravityOnDimensionChange) {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayerEntity)(Object)this, Direction.DOWN, new RotationParameters().rotationTime(0));
        } else {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayerEntity)(Object)this, GravityChangerAPI.getDefaultGravityDirection((ServerPlayerEntity)(Object)this), new RotationParameters().rotationTime(0));
        }
    }

    @Inject(
            method = "copyFrom",
            at = @At("TAIL")
    )
    private void inject_copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if(JCraft.gravityConfig.resetGravityOnRespawn) {
        } else {
            GravityChangerAPI.setDefaultGravityDirection((ServerPlayerEntity)(Object)this, GravityChangerAPI.getDefaultGravityDirection(oldPlayer), new RotationParameters().rotationTime(0));
        }
    }
}
