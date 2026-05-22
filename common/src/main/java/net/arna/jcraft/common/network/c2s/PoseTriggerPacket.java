package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PoseTriggerPacket {

    /** Server-side map of players currently holding a pose (for damage cancellation). */
    public static final Map<UUID, String> ACTIVE_POSES = new ConcurrentHashMap<>();

    /**
     * @param animId  the animation name to play
     * @param isIdle  whether this is the looping idle (vs. one-shot windup)
     */
    public static FriendlyByteBuf write(String animId, boolean isIdle) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(animId);
        buf.writeBoolean(isIdle);
        return buf;
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        String animId = buf.readUtf();
        // isIdle flag is informational — not needed server-side but kept for parity with write()
        buf.readBoolean();

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        MinecraftServer server = context.getPlayer().getServer();

        Objects.requireNonNull(server).execute(() -> {
            if (player == null) return;

            ACTIVE_POSES.put(player.getUUID(), animId);

            Iterable<ServerPlayer> nearby = JUtils.around(
                    (ServerLevel) player.level(), player.position(), JUtils.PLAYER_ANIMATION_DIST);

            for (ServerPlayer recipient : nearby) {
                PlayerAnimPacket.send(player, recipient, animId);
            }
        });
    }
}
