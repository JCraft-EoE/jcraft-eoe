package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.common.system.GunAiming;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class GunAimPacket {
    private GunAimPacket() {}

    public static FriendlyByteBuf write(boolean aiming) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(aiming);
        return buf;
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        boolean aiming = buf.readBoolean();
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        context.queue(() -> {
            if (aiming && GunAiming.isAimableGun(player.getMainHandItem())) {
                GunAiming.set(player, true);
            } else {
                GunAiming.set(player, false);
            }
        });
    }
}
