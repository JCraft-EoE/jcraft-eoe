package net.arna.jcraft.mixin;

import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    /**
     * Swapping hands bypasses the inventory slots entirely, so guns have to be turned away here
     * as well to keep them out of the offhand.
     */
    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void jcraft$noGunsInOffhand(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return;
        }

        if (player.getMainHandItem().getItem() instanceof Peacemaker
                || player.getOffhandItem().getItem() instanceof Peacemaker) {
            ci.cancel();
        }
    }
}
