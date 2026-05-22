package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class PoseCancelPacket {

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        Objects.requireNonNull(context.getPlayer().getServer()).execute(() -> {
            PoseTriggerPacket.ACTIVE_POSES.remove(player.getUUID());
            broadcastCancel(player);
        });
    }

    /** Broadcast animation clear (reuses channel feedback case 13) to all nearby players. */
    public static void broadcastCancel(ServerPlayer player) {
        FriendlyByteBuf cancelBuf = new FriendlyByteBuf(Unpooled.buffer());
        cancelBuf.writeShort(13);
        cancelBuf.writeInt(player.getId());

        ServerChannelFeedbackPacket.send(
                JUtils.around((ServerLevel) player.level(), player.position(), JUtils.PLAYER_ANIMATION_DIST),
                cancelBuf);
    }
}
