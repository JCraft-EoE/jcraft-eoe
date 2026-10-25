package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayer player;

    /**
     * Swapping hands bypasses the inventory slots entirely, so guns have to be turned away here
     * as well to keep them out of the offhand.
     */
    @WrapMethod(method = "handlePlayerAction")
    private void jcraft$noGunsInOffhand(final ServerboundPlayerActionPacket packet, final Operation<Void> original) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND || !(
                player.getMainHandItem().getItem() instanceof Peacemaker ||
                        player.getOffhandItem().getItem() instanceof Peacemaker)
        ) {
            original.call(packet);
        }
    }

}
