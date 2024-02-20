package net.arna.jcraft.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    /*
    @Redirect(
            method = "onPlayerAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;dropSelectedItem(Z)Z",
                    ordinal = 0
            )
    )
    private boolean jcraft$dropSelectedItem(ServerPlayerEntity instance, boolean entireStack) {
        return false;
    }

    @Shadow
    public ServerPlayerEntity player;
    @Inject(cancellable = true, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;clampHorizontal(D)D",
            shift = At.Shift.BEFORE, ordinal = 0)
            , method = "onPlayerMove"
            )
    public void jcraft$onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if ( player.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
            stand.UpdateRemoveMovement(
                    player.prevX - packet.getX(player.getX()),
                    player.prevY - packet.getX(player.getY()),
                    player.prevZ - packet.getX(player.getZ())
                    );
            ci.cancel();
        }
    }
     */
}
