package net.arna.jcraft.common.util;

import lombok.NonNull;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public interface JNetworking {
    static void sendToPlayers(@NonNull final Iterable<ServerPlayer> to, @NonNull Packet<?> packet) {
        for (ServerPlayer recv : to) {
            recv.connection.send(packet);
        }
    }
}
